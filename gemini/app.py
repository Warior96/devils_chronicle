# python start_service.py
from flask import Flask, request, jsonify  # Importa Flask e moduli per richieste e risposte JSON
import google.generativeai as genai  # SDK per usare il modello Gemini
import os  # Per accedere alle variabili di ambiente
from dotenv import load_dotenv  # Per caricare le variabili da un file .env
import logging  # Per log degli errori o info
from flask_cors import CORS  # Per abilitare richieste CORS (necessario per frontend)

# Configura il logging base (stampa a console gli eventi importanti)
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Carica le variabili di ambiente da .env
load_dotenv()

# Configura l'accesso al modello Gemini usando la chiave API
genai.configure(api_key=os.getenv("GOOGLE_API_KEY"))

# Specifica il modello da usare
model = genai.GenerativeModel(model_name="models/gemma-3-1b-it")

# Inizializza l'app Flask
app = Flask(__name__)

# Abilita CORS
CORS(app)


# -------------------------
# ENDPOINT: / (GET)
# Verifica che il servizio sia attivo
@app.route("/", methods=["GET"])
def health_check():
    return jsonify({
        "status": "active",
        "service": "Article AI Assistant",
        "version": "1.0"
    })


# -------------------------
# ENDPOINT: /summarize (POST)
# Riassume un articolo passato nel body della richiesta
@app.route("/summarize", methods=["POST"])
def summarize_article():
    data = request.get_json()
    article_title = data.get("title", "").strip()
    article_content = data.get("content", "").strip()

    if not article_content:
        return jsonify({"error": "Content is required"}), 400

    # Prompt rigido per impedire allucinazioni
    prompt = f"""
Sei un assistente AI che legge articoli di giornale e fornisce un riassunto conciso e informativo basato esclusivamente sul testo fornito.

TITOLO: {article_title}

TESTO:
{article_content}

🔒 ATTENZIONE: Devi basarti ESCLUSIVAMENTE sul contenuto dell'articolo. NON inventare né aggiungere informazioni esterne.

Genera ora un riassunto professionale e informativo:
"""

    try:
        response = model.generate_content(prompt)
        return jsonify({"response": response.text})
    except Exception as e:
        logger.error(f"Errore nella generazione AI (riassunto): {e}")
        return jsonify({"error": "Errore durante la generazione del riassunto"}), 500


# -------------------------
# ENDPOINT: /answer (POST)
# Risponde a una domanda sul contenuto dell'articolo
@app.route("/answer", methods=["POST"])
def answer_question():
    data = request.get_json()
    article_title = data.get("title", "").strip()
    article_content = data.get("content", "").strip()
    user_question = data.get("question", "").strip()

    # Controllo campi obbligatori
    if not article_content or not user_question:
        return jsonify({"error": "Sia content che question sono richiesti"}), 400

    # Prompt rigido per evitare risposte inventate
    prompt = f"""
Sei un assistente AI che risponde a domande basate su articoli giornalistici. Utilizza solo il contenuto dell'articolo per rispondere.

TITOLO: {article_title}

TESTO:
{article_content}

DOMANDA: {user_question}

🔒 ATTENZIONE: Rispondi solo in base all'articolo. NON usare conoscenze esterne o inventare fatti.

Risposta:
"""

    try:
        response = model.generate_content(prompt)
        return jsonify({"response": response.text})
    except Exception as e:
        logger.error(f"Errore nella generazione AI (risposta): {e}")
        return jsonify({"error": "Errore durante la generazione della risposta"}), 500


# -------------------------
# HANDLER: 404 - Not Found
@app.errorhandler(404)
def not_found(error):
    return jsonify({"error": "Endpoint non trovato"}), 404


# -------------------------
# HANDLER: 500 - Internal Server Error
@app.errorhandler(500)
def internal_error(error):
    return jsonify({"error": "Errore interno del server"}), 500


# -------------------------
# Avvia l'app se il file viene eseguito direttamente
if __name__ == "__main__":
    app.run(debug=True, host="0.0.0.0", port=5006)
