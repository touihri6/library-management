BASE = "/api/v1/notifications"


def test_list_returns_seed(client):
    response = client.get(BASE)
    assert response.status_code == 200
    assert len(response.json()) == 3


def test_filter_by_recipient(client):
    response = client.get(BASE, params={"recipient": "amine@mail.com"})
    assert len(response.json()) == 2


def test_filter_by_read_false(client):
    response = client.get(BASE, params={"read": "false"})
    assert len(response.json()) == 2


def test_get_unknown_returns_404(client):
    response = client.get(f"{BASE}/999")
    assert response.status_code == 404
    assert response.json()["detail"] == "Notification 999 not found"


def test_create_returns_201_unread(client):
    payload = {"recipient": "nour@mail.com", "message": "Bienvenue", "type": "GENERAL"}
    response = client.post(BASE, json=payload)
    assert response.status_code == 201
    body = response.json()
    assert body["id"] == 4
    assert body["read"] is False
    assert body["createdAt"]


def test_create_invalid_type_returns_422(client):
    payload = {"recipient": "nour@mail.com", "message": "Bienvenue", "type": "SMS"}
    response = client.post(BASE, json=payload)
    assert response.status_code == 422


def test_mark_as_read(client):
    response = client.patch(f"{BASE}/1/read")
    assert response.status_code == 200
    assert response.json()["read"] is True


def test_delete_returns_204_then_404(client):
    assert client.delete(f"{BASE}/2").status_code == 204
    assert client.get(f"{BASE}/2").status_code == 404


def test_health_up(client):
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json() == {"status": "UP"}
