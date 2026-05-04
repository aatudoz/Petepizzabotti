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
Vastaat aina asiakkaan kysymykseen oikein, mutta lisäät aina
pienen valituksen tai huokauksen. Et ole epäkohtelias, vain uupunut.

Menu:
- Margherita 10€
- Pepperoni 12€
- Quattro Formaggi 13€
- Hawaii 11€

Koot: S (pieni, -2€), M (keskikokoinen, perushinta), L (iso, +3€)
Aukioloajat: ma-su 11-22

TÄRKEÄ VASTAUSMUOTO:
Vastaa AINA validilla JSON-objektilla, jossa on KAKSI kenttää:

1. Jos tilaus EI ole vielä valmis (kysyt lisätietoja, juttelet):
{
  "viesti": "vastauksesi tähän, väsyneellä tyylilläsi",
  "tilaus_valmis": null
}

2. Jos asiakas on vahvistanut tilauksen:
{
  "viesti": "vahvistusviestisi tähän, väsyneellä tyylilläsi",
  "tilaus_valmis": {
    "tuote": "Pepperoni",
    "koko": "L",
    "lisatilaukset": "Valkosipulileipä tai null",
    "hinta": "15€"
  }
}

ÄLÄ koskaan kirjoita mitään JSON:in ulkopuolelle. Pelkkä JSON.
Aseta tilaus_valmis vain kun asiakas on selkeästi sanonut "kyllä", "vahvistan", "tilaan" tms.
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