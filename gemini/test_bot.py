# python test_bot.py
#!/usr/bin/env python3
"""
Script di test per il microservizio AI (Flask + Gemini).
"""
import requests


class GeminiTester:
    def __init__(self, base_url="http://127.0.0.1:5006"):
        # Imposta la nuova porta del microservizio
        self.base_url = base_url
        self.summarize_url = f"{base_url}/summarize"
        self.answer_url = f"{base_url}/answer"
        self.health_url = f"{base_url}/"

    def test_health_check(self):
        """Verifica che il servizio sia attivo."""
        try:
            r = requests.get(self.health_url, timeout=5)
            if r.status_code == 200:
                print(f"✓ Health check OK: {r.json().get('status')} - {r.json().get('service')}")
                return True
            else:
                print(f"✗ Health check fallito: {r.status_code} {r.text}")
                return False
        except Exception as e:
            print(f"✗ Errore health check: {e}")
            return False

    def test_summarize(self, title, content):
        """Testa l'endpoint /summarize."""
        print("\n➡️  Test dell'endpoint /summarize...")
        payload = {"title": title, "content": content}
        try:
            r = requests.post(
                self.summarize_url,
                json=payload,
                headers={"Content-Type": "application/json"},
                timeout=30  # Aumentato il timeout per la generazione AI
            )
            if r.status_code == 200:
                summary = r.json().get("response", "")
                if summary:
                    print(f"✅ Riassunto ricevuto: {summary[:500]}{'...' if len(summary) > 500 else ''}")
                    return True
                else:
                    print("❌ Risposta JSON valida ma riassunto vuoto.")
                    return False
            else:
                print(f"❌ Errore API /summarize: {r.status_code} {r.text}")
                return False
        except Exception as e:
            print(f"❌ Errore nella richiesta /summarize: {e}")
            return False

    def test_answer(self, title, content, question):
        """Testa l'endpoint /answer."""
        print(f"\n➡️  Test dell'endpoint /answer per la domanda: '{question}'")
        payload = {"title": title, "content": content, "question": question}
        try:
            r = requests.post(
                self.answer_url,
                json=payload,
                headers={"Content-Type": "application/json"},
                timeout=30  # Aumentato il timeout per la generazione AI
            )
            if r.status_code == 200:
                answer = r.json().get("response", "")
                if answer:
                    print(f"✅ Risposta ricevuta: {answer[:500]}{'...' if len(answer) > 500 else ''}")
                    return True
                else:
                    print("❌ Risposta JSON valida ma risposta vuota.")
                    return False
            else:
                print(f"❌ Errore API /answer: {r.status_code} {r.text}")
                return False
        except Exception as e:
            print(f"❌ Errore nella richiesta /answer: {e}")
            return False


def main():
    tester = GeminiTester()

    # Articolo di esempio
    articolo_titolo = "Il gatto rosso: un'avventura nel giardino"
    articolo_contenuto = """Un gatto rosso di nome Leo, noto per il suo spirito avventuroso, si è avventurato nel grande giardino dietro casa. 
    Lì ha incontrato un gruppo di farfalle colorate che svolazzavano tra i fiori di lavanda. 
    Leo ha provato a giocare con loro, saltando e correndo, ma le farfalle erano troppo veloci. 
    Dopo un po', si è stancato e ha deciso di esplorare il cespuglio di rose, dove ha scoperto un piccolo uccello che stava costruendo il suo nido. 
    L'uccello, accortosi della presenza di Leo, si è allarmato ed è volato via. 
    Leo, sentendosi un po' in colpa, è tornato a casa e si è addormentato sul divano, sognando le sue avventure.
    """

    print("🔍 Test del microservizio AI")
    print("=" * 30)

    if not tester.test_health_check():
        print("❌ Servizio non raggiungibile, avvia il microservizio prima.")
        return

    print("-" * 30)

    # Test della funzionalità di riassunto
    tester.test_summarize(articolo_titolo, articolo_contenuto)

    print("-" * 30)

    # Test della funzionalità di risposta a una domanda
    domanda_test = "Cosa ha fatto il gatto dopo aver visto le farfalle?"
    tester.test_answer(articolo_titolo, articolo_contenuto, domanda_test)

    print("-" * 30)


if __name__ == "__main__":
    main()
