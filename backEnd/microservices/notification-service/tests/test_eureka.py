import asyncio

from app import eureka


def test_start_eureka_registers_with_localhost(monkeypatch):
    captured = {}

    async def fake_init_async(**kwargs):
        captured.update(kwargs)

    monkeypatch.setenv("EUREKA_ENABLED", "true")
    monkeypatch.setattr(eureka.eureka_client, "init_async", fake_init_async)

    asyncio.run(eureka.start_eureka(8087))

    assert captured["app_name"] == "notification-service"
    assert captured["instance_host"] == "localhost"
    assert captured["instance_port"] == 8087
    assert captured["health_check_url"] == "http://localhost:8087/health"


def test_start_eureka_disabled_does_nothing(monkeypatch):
    called = []

    async def fake_init_async(**kwargs):
        called.append(kwargs)

    monkeypatch.setenv("EUREKA_ENABLED", "false")
    monkeypatch.setattr(eureka.eureka_client, "init_async", fake_init_async)

    asyncio.run(eureka.start_eureka(8087))

    assert called == []
