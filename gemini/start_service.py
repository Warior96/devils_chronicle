# python start_service.py
#!/usr/bin/env python3
"""
Script di avvio per il microservizio AI del chatbot.

- Verifica che tutte le dipendenze siano installate
- Controlla la presenza e la correttezza del file .env
- Avvia il servizio in modalità sviluppo (con python) o produzione (con gunicorn)
"""

import sys
import subprocess
from pathlib import Path


def check_requirements():
    """Verifica che tutte le dipendenze richieste siano installate.
    Ritorna True se tutte sono presenti, False altrimenti."""
    try:
        import flask
        import google.generativeai
        import mysql.connector
        import dotenv
        print("✓ Tutte le dipendenze sono installate")
        return True
    except ImportError as e:
        print(f"✗ Dipendenza mancante: {e}")
        return False


def check_env_file():
    """Verifica che il file .env esista e contenga le variabili d'ambiente richieste.
    Ritorna True se è tutto corretto, False altrimenti."""
    env_path = Path('.env')
    if not env_path.exists():
        print("✗ File .env non trovato")
        return False

    required_vars = ['GOOGLE_API_KEY', 'DB_HOST', 'DB_USER', 'DB_PASSWORD', 'DB_NAME']

    with open(env_path, 'r') as f:
        content = f.read()

    missing_vars = [var for var in required_vars if var not in content]

    if missing_vars:
        print(f"✗ Variabili d'ambiente mancanti: {', '.join(missing_vars)}")
        return False

    print("✓ File .env configurato correttamente")
    return True


def start_service(mode='development'):
    """Avvia il microservizio.

    In modalità produzione usa gunicorn,
    in modalità sviluppo avvia app.py con python dell'ambiente virtuale."""
    python_executable = sys.executable  # Interprete Python attivo (virtualenv)

    if mode == 'production':
        print("🚀 Avvio del servizio in modalità produzione con Gunicorn...")
        cmd = [
            'gunicorn',
            '--bind', '0.0.0.0:5006',
            '--workers', '4',
            '--timeout', '120',
            '--log-level', 'info',
            'app:app'
        ]
    else:
        print("🚀 Avvio del servizio in modalità sviluppo...")
        cmd = [python_executable, 'app.py']

    try:
        subprocess.run(cmd, check=True)
    except subprocess.CalledProcessError as e:
        print(f"✗ Errore nell'avvio del servizio: {e}")
        sys.exit(1)
    except KeyboardInterrupt:
        print("\n🛑 Servizio interrotto dall'utente")
        sys.exit(0)


def main():
    """Funzione principale: controlla prerequisiti, legge argomenti e avvia il servizio."""
    print("🤖 Chatbot AI Microservice - Sistema di avvio")
    print("=" * 50)

    # Controllo dipendenze
    if not check_requirements():
        print("\n💡 Per installare le dipendenze, esegui: pip install -r requirements.txt")
        sys.exit(1)

    # Controllo file .env
    if not check_env_file():
        print("\n💡 Crea il file .env basandoti su .env.example")
        sys.exit(1)

    # Modalità: produzione se presente '--production', altrimenti sviluppo
    mode = 'production' if '--production' in sys.argv else 'development'

    print(f"\n📊 Modalità: {mode}")
    print("📍 Porta: 5006")
    print("🔗 URL: http://localhost:5006")
    print("🏥 Health check: http://localhost:5006/")
    print("📝 Riassunto: POST http://localhost:5006/summarize")
    print("❓ Domande:  POST http://localhost:5006/answer")
    print("-" * 50)


    # Avvio del servizio
    start_service(mode)


if __name__ == "__main__":
    main()
