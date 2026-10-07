import os

os.environ["EUREKA_ENABLED"] = "false"

import pytest
from fastapi.testclient import TestClient

from app.main import app
from app.routers.notification import get_service
from app.services.notification_service import NotificationService


@pytest.fixture
def client():
    service = NotificationService()
    app.dependency_overrides[get_service] = lambda: service
    with TestClient(app) as test_client:
        yield test_client
    app.dependency_overrides.clear()
