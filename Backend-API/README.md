# Andy AI Gateway

FastAPI gateway for routing requests to an OpenAI-compatible local model first, then a configured cloud model.

## Run locally
```bash
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
cp .env.example .env
uvicorn app.main:app --host 127.0.0.1 --port 8000
```

The current endpoint is a development scaffold. Configure provider variables in the process environment before use. Do not expose this gateway publicly until proper user authentication, HTTPS termination, rate limiting, request logging hygiene, and secret management are configured.

## Local model
Set `LOCAL_LLM_BASE_URL` to an OpenAI-compatible endpoint reachable by the gateway. For example, Ollama commonly exposes an OpenAI-compatible API at `/v1` when enabled/configured.

## Hybrid routing
The gateway tries the local provider first. If that request fails, it tries the cloud provider. If neither is configured, it returns a clear unconfigured response.

## Security
- Never commit real API keys.
- Keep the gateway bound to localhost during development.
- For production, replace the optional shared client key with per-user authentication and short-lived tokens.
- Do not connect untrusted clients directly to a local model port.
