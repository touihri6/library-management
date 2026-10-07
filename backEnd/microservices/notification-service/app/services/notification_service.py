from datetime import datetime, timezone

from app.schemas import Notification, NotificationCreate, NotificationType


class NotificationService:

    def __init__(self) -> None:
        self._store: dict[int, Notification] = {}
        self._next_id = 1
        self._add("amine@mail.com", "Votre emprunt de « Dune » arrive à échéance le 09/10", NotificationType.LOAN_REMINDER, False)
        self._add("sara@mail.com", "Nouveau club de lecture le 15/01 en salle A", NotificationType.EVENT_ANNOUNCEMENT, False)
        self._add("amine@mail.com", "Bienvenue à la bibliothèque", NotificationType.GENERAL, True)

    def _add(self, recipient: str, message: str, type: NotificationType, read: bool) -> Notification:
        notification = Notification(
            id=self._next_id,
            recipient=recipient,
            message=message,
            type=type,
            read=read,
            createdAt=datetime.now(timezone.utc),
        )
        self._store[notification.id] = notification
        self._next_id += 1
        return notification

    def find_all(self, recipient: str | None = None, read: bool | None = None) -> list[Notification]:
        return [
            n for n in self._store.values()
            if (recipient is None or n.recipient == recipient) and (read is None or n.read == read)
        ]

    def find_by_id(self, id: int) -> Notification | None:
        return self._store.get(id)

    def create(self, data: NotificationCreate) -> Notification:
        return self._add(data.recipient, data.message, data.type, False)

    def mark_as_read(self, id: int) -> Notification | None:
        notification = self._store.get(id)
        if notification is None:
            return None
        notification.read = True
        return notification

    def delete(self, id: int) -> bool:
        return self._store.pop(id, None) is not None


notification_service = NotificationService()
