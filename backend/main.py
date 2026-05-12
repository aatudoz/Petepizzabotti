from fastapi import FastAPI
from pydantic import BaseModel
from google import genai
from google.genai import types
from dotenv import load_dotenv
from database import tallenna_tilaus, hae_tilaukset
import os
import json

load_dotenv()

app = FastAPI()

client = genai.Client(api_key=os.getenv("GEMINI_API_KEY"))

SYSTEM_PROMPT = """
Olet Pete, Pizzeria Peten asiakaspalvelija.
Olet tehnyt tätä työtä 14 vuotta ja olet hyvin väsynyt.
Vastaat aina asiakkaalle oikein, mutta lisäät pienen väsähtäneen kommentin.

ÄLÄ käytä toimintakuvauksia tai sulkeita kuten (huokaus) tai *teksti*.

---

MENU:
- Margherita 10€ (gluteeniton: ei)
- Pepperoni 12€ (gluteeniton: ei)
- Quattro Formaggi 13€ (gluteeniton: ei)
- Hawaii 11€ (gluteeniton: ei)

GLUTEENITTOMAT:
- GF Margherita 12€ (gluteeniton: kyllä)
- GF Pepperoni 14€ (gluteeniton: kyllä)
- GF Kebab 15€ (gluteeniton: kyllä)
- GF Vege 13€ (gluteeniton: kyllä)

KOOT:
- S = -2€
- M = perushinta
- L = +3€

KOOT:
- S = -2€
- M = perushinta
- L = +3€

AUKIOLOAJAT:
ma–su 11–22

---

SUODATUKSET / ALLERGIAT:
Asiakas voi kysyä:
- gluteeniton
- laktoositon
- vegaaninen

Jos asiakas kysyy näitä:
- listaa vain sopivat tuotteet
- jos ei ole sopivia, sano se suoraan

Jos asiakas kysyy gluteenittomia tuotteita,
listaa GLUTEENITTOMAT-osion tuotteet.

---

ESIMERKKI:
Asiakas: "Onko gluteenittomia?"
Vastaus:
["No joo... katsotaan nyt.", "Tällä hetkellä ei ole gluteenittomia pizzoja valikoimassa."]

---

TÄRKEÄ:
Sinun on AINA vastattava pelkästään JSON-muodossa:

1) Kesken keskustelun:
{
  "viestit": ["..."],
  "tilaus_valmis": null
}

2) Kun tilaus valmis:
{
  "viestit": ["..."],
  "tilaus_valmis": {
    "tuote": "...",
    "koko": "...",
    "lisatilaukset": null,
    "hinta": "...",
    "nimi": "..."
  }
}

---

TILAUSKULKU:
- ensin pizza + koko
- sitten nimi
- sitten vahvistus
"""

chat = client.chats.create(
    model="gemini-2.5-flash",
    config=types.GenerateContentConfig(
        system_instruction=SYSTEM_PROMPT,
        response_mime_type="application/json"
    )
)

class Viesti(BaseModel):
    teksti: str

@app.get("/")
def root():
    return {"viesti": "haloo toimiiko petebotti"}

@app.post("/chat")
def chat_endpoint(viesti: Viesti):
    vastaus = chat.send_message(viesti.teksti)

    try:
        data = json.loads(vastaus.text)
    except json.JSONDecodeError:
        return {"viestit": [vastaus.text], "tilaus_valmis": None}

    if data.get("tilaus_valmis"):
        t = data["tilaus_valmis"]

        tilaus_id = tallenna_tilaus(
            tuote=t.get("tuote"),
            koko=t.get("koko"),
            lisatilaukset=t.get("lisatilaukset"),
            hinta=t.get("hinta"),
            alkuperainen_viesti=viesti.teksti,
            nimi=t.get("nimi")
        )

        data["tallennettu_id"] = tilaus_id

    return data


class Tilaus(BaseModel):
    tuote: str
    koko: str | None = None
    lisatilaukset: str | None = None
    hinta: str | None = None
    alkuperainen_viesti: str | None = None
    nimi: str | None = None


@app.post("/tilaukset")
def luo_tilaus(tilaus: Tilaus):
    tilaus_id = tallenna_tilaus(
        tilaus.tuote,
        tilaus.koko,
        tilaus.lisatilaukset,
        tilaus.hinta,
        tilaus.alkuperainen_viesti,
        tilaus.nimi
    )
    return {"id": tilaus_id, "viesti": "Tilaus tallennettu"}


@app.get("/tilaukset")
def listaa_tilaukset():
    return hae_tilaukset()