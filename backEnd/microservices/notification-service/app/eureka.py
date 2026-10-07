import logging
import os

from py_eureka_client import eureka_client

APP_NAME = "notification-service"
INSTANCE_HOST = "localhost"

logger = logging.getLogger("uvicorn.error")


def eureka_enabled() -> bool:
    return os.getenv("EUREKA_ENABLED", "true").lower() == "true"


async def start_eureka(port: int) -> None:
    if not eureka_enabled():
        return
    base_url = f"http://{INSTANCE_HOST}:{port}"
    try:
        await eureka_client.init_async(
            eureka_server=os.getenv("EUREKA_SERVER", "http://localhost:8761/eureka"),
            app_name=APP_NAME,
            instance_id=f"{APP_NAME}:{port}",
            instance_host=INSTANCE_HOST,
            instance_ip="127.0.0.1",
            instance_port=port,
            home_page_url=f"{base_url}/",
            status_page_url=f"{base_url}/health",
            health_check_url=f"{base_url}/health",
        )
        logger.info("%s registered in Eureka on port %s", APP_NAME, port)
    except Exception as error:
        logger.error("Eureka registration failed: %s", error)


async def stop_eureka() -> None:
    if not eureka_enabled() or eureka_client.get_client() is None:
        return
    try:
        await eureka_client.stop_async()
        logger.info("%s deregistered from Eureka", APP_NAME)
    except Exception as error:
        logger.error("Eureka deregistration failed: %s", error)
