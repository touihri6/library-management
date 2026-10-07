import os
from contextlib import asynccontextmanager

from fastapi import FastAPI

from app.eureka import start_eureka, stop_eureka
from app.routers import health, notification

PORT = int(os.getenv("PORT", "8087"))


@asynccontextmanager
async def lifespan(app: FastAPI):
    await start_eureka(PORT)
    yield
    await stop_eureka()


app = FastAPI(
    title="Notification Service API",
    version="v1",
    description="Notification Service - in-memory notifications (Python / FastAPI)",
    docs_url="/swagger-ui",
    openapi_url="/v3/api-docs",
    lifespan=lifespan,
)

app.include_router(health.router)
app.include_router(notification.router)
