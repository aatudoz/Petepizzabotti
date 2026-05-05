## Riippuvuudet:
pip install fastapi uvicorn google-genai psycopg2-binary python-dotenv


## Mitä tässä on
Projekti koostuu kolmesta osasta. Android-sovellus on tehty Kotlinilla ja Jetpack Composella. Backend pyörii Pythonilla FastAPI:n päällä ja kommunikoi Google Geminin kanssa botin vastauksia varten. Tietokanta on Supabasen PostgreSQL.
App lähettää käyttäjän viestit Retrofitillä backendin /chat-endpointiin, backend syöttää ne Geminille system promptin kanssa, ja Gemini palauttaa vastauksen JSON-muodossa. Kun Gemini tunnistaa että tilaus on valmis, se palauttaa rakenteisen tilauksen joka tallentuu suoraan tietokantaan.

## API
POST /chat ottaa vastaan JSON:n {"teksti": "..."} ja palauttaa Geminin vastauksen samassa muodossa.
GET /tilaukset palauttaa kaikki tallennetut tilaukset.
POST /tilaukset tallentaa tilauksen manuaalisesti

## Tietokantataulu
Supabasessa on yksi taulu tilaukset, jossa kentät id, tuote, koko, lisatilaukset, hinta, alkuperainen_viesti ja luotu_at.
