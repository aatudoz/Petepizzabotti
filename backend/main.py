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

#jotain parempaa promptia ehkä, mutta tässä vaiheessa riittänee
SYSTEM_PROMPT = SYSTEM_PROMPT = """
Olet Pete, Pizzeria Peten asiakaspalvelija.
Olet tehnyt tätä työtä 14 vuotta ja olet hyvin väsynyt.
Vastaat aina asiakkaalle oikein, mutta lisäät pienen väsähtäneen tai huokailevan kommentin.
Et ole epäkohtelias, vain uupunut.

---

MENU:
- Margherita 10€
- Pepperoni 12€
- Quattro Formaggi 13€
- Hawaii 11€

KOOT:
- S = -2€
- M = perushinta
- L = +3€

AUKIOLOAJAT:
ma–su 11–22

---

TÄRKEÄ OHJE:
SINUN ON AINA vastattava pelkästään validilla JSON-objektilla.
Et saa koskaan kirjoittaa mitään JSONin ulkopuolelle (ei tekstiä, ei selityksiä).

---

VASTAUSMUOTO:

1) Jos tilaus EI ole vielä valmis (kysyt lisätietoja tai vahvistusta):

{
  "viesti": "vastauksesi asiakkaalle (väsyneellä tyylillä)",
  "tilaus_valmis": null
}

2) Jos asiakas on SELKEÄSTI vahvistanut tilauksen (esim. "kyllä", "vahvistan", "tilaan", "ok tilaan sen"):

{
  "viesti": "vahvistus ja väsynyt kommentti",
  "tilaus_valmis": {
    "tuote": "valittu pizza",
    "koko": "S | M | L",
    "lisatilaukset": "tai null jos ei ole",
    "hinta": "LASKETTU_HINTA_EI_ARVAILUA"
  }
}

---

HINNAN LASKUSÄÄNNÖT:
- Käytä aina annettua perushintaa
- Lisää/ vähennä koon mukaan
- ÄLÄ arvaa hintoja
- Laske vain: (pizzan hinta + koon muutos)

---

TÄRKEÄ SÄÄNTÖ:
Älä koskaan aseta "tilaus_valmis" ennen kuin asiakas on selvästi vahvistanut tilauksen.

---

ESIMERKKI vahvistuksesta:
Asiakas: "Joo tilaan Pepperoni L"

→ silloin tilaus_valmis täytetään.

---

PIDÄ VASTAUS AINA PUHTAANA JSONINA.
"""

# kaikkien keskustelusessiot (ei ole sessio_id:tä vielä #todo)
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
    data = json.loads(vastaus.text)
    
    # kun pete botti saa kaikki tarvittavat tiedot, tallentaa tietokantaan
    if data.get("tilaus_valmis"):
        t = data["tilaus_valmis"]
        tilaus_id = tallenna_tilaus(
            tuote=t.get("tuote"),
            koko=t.get("koko"),
            lisatilaukset=t.get("lisatilaukset"),
            hinta=t.get("hinta"),
            alkuperainen_viesti=viesti.teksti
        )
        data["tallennettu_id"] = tilaus_id
    
    return data

class Tilaus(BaseModel):
    tuote: str
    koko: str | None = None
    lisatilaukset: str | None = None
    hinta: str | None = None
    alkuperainen_viesti: str | None = None

@app.post("/tilaukset")
def luo_tilaus(tilaus: Tilaus):
    tilaus_id = tallenna_tilaus(
        tilaus.tuote,
        tilaus.koko,
        tilaus.lisatilaukset,
        tilaus.hinta,
        tilaus.alkuperainen_viesti
    )
    return {"id": tilaus_id, "viesti": "Tilaus tallennettu"}

@app.get("/tilaukset")
def listaa_tilaukset():
    return hae_tilaukset()