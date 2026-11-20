from typing import Literal
import os
import httpx
from fastapi import FastAPI, Header, HTTPException
from pydantic import BaseModel, Field

app = FastAPI(title="Andy AI Gateway", version="0.1.0")

class ChatRequest(BaseModel):
    message: str = Field(min_length=1, max_length=12000)
    conversation_id: str | None = Field(default=None, max_length=128)

class ChatResponse(BaseModel):
    reply: str
    provider: Literal["local", "cloud", "unconfigured"]

async def ask_openai_compatible(base_url: str, api_key: str, model: str, message: str) -> str:
    headers = {"Authorization": f"Bearer {api_key}"} if api_key else {}
    async with httpx.AsyncClient(timeout=60) as client:
        response = await client.post(
            f"{base_url.rstrip('/')}/chat/completions",
            headers=headers,
            json={"model": model, "messages": [
                {"role": "system", "content": "You are Andy, a helpful assistant. Be honest about uncertainty. For security work, only support explicitly authorized defensive testing."},
                {"role": "user", "content": message}
            ]}
        )
        response.raise_for_status()
        data = response.json()
        return data["choices"][0]["message"]["content"]

@app.get("/health")
async def health():
    return {"status": "ok", "service": "andy-gateway"}

@app.post("/v1/chat", response_model=ChatResponse)
async def chat(body: ChatRequest, x_andy_client_key: str | None = Header(default=None)):
    expected = os.getenv("ANDY_CLIENT_KEY")
    if expected and x_andy_client_key != expected:
        raise HTTPException(status_code=401, detail="Unauthorized client")

    local_url = os.getenv("LOCAL_LLM_BASE_URL", "")
    local_model = os.getenv("LOCAL_LLM_MODEL", "local-model")
    cloud_url = os.getenv("CLOUD_LLM_BASE_URL", "")
    cloud_key = os.getenv("CLOUD_LLM_API_KEY", "")
    cloud_model = os.getenv("CLOUD_LLM_MODEL", "")

    # Hybrid policy: prefer configured local model; fall back to cloud if local is unavailable.
    if local_url:
        try:
            reply = await ask_openai_compatible(local_url, os.getenv("LOCAL_LLM_API_KEY", ""), local_model, body.message)
            return ChatResponse(reply=reply, provider="local")
        except (httpx.HTTPError, KeyError, ValueError):
            pass

    if cloud_url and cloud_model:
        if not cloud_key:
            raise HTTPException(status_code=503, detail="Cloud provider configured without API key")
        try:
            reply = await ask_openai_compatible(cloud_url, cloud_key, cloud_model, body.message)
            return ChatResponse(reply=reply, provider="cloud")
        except (httpx.HTTPError, KeyError, ValueError):
            raise HTTPException(status_code=502, detail="Configured AI providers are unavailable")

    return ChatResponse(
        reply="Andy gateway is running, but no AI provider is configured yet. Add a local OpenAI-compatible endpoint or cloud provider in the server environment.",
        provider="unconfigured"
    )
