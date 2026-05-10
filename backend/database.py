import psycopg2
from psycopg2.extras import RealDictCursor
import os
from dotenv import load_dotenv

load_dotenv()

def hae_yhteys():
    return psycopg2.connect(os.getenv("DATABASE_URL"))

def tallenna_tilaus(tuote, koko, lisatilaukset, hinta, alkuperainen_viesti, nimi):
    yhteys = hae_yhteys()
    kursori = yhteys.cursor()
    kursori.execute("""
        INSERT INTO tilaukset (tuote, koko, lisatilaukset, hinta, alkuperainen_viesti, nimi)
        VALUES (%s, %s, %s, %s, %s, %s)
        RETURNING id
    """, (tuote, koko, lisatilaukset, hinta, alkuperainen_viesti, nimi))
    tilaus_id = kursori.fetchone()[0]
    yhteys.commit()
    kursori.close()
    yhteys.close()
    return tilaus_id

def hae_tilaukset():
    yhteys = hae_yhteys()
    kursori = yhteys.cursor(cursor_factory=RealDictCursor)
    kursori.execute("SELECT * FROM tilaukset ORDER BY luotu_at DESC")
    tilaukset = kursori.fetchall()
    kursori.close()
    yhteys.close()
    return tilaukset