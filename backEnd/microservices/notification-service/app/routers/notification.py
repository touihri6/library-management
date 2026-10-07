from fastapi import APIRouter, Depends, HTTPException, Response, status

from app.schemas import Notification, NotificationCreate
from app.services.notification_service import NotificationService, notification_service

router = APIRouter(prefix="/api/v1/notifications", tags=["Notifications"])


def get_service() -> NotificationService:
    return notification_service


def not_found(id: int) -> HTTPException:
    return HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail=f"Notification {id} not found")


@router.get("", response_model=list[Notification], summary="List notifications, optionally filtered by recipient and/or read")
def find_all(recipient: str | None = None, read: bool | None = None,
             service: NotificationService = Depends(get_service)):
    return service.find_all(recipient, read)


@router.get("/{id}", response_model=Notification, summary="Get a notification by id",
            responses={404: {"description": "Notification not found"}})
def find_by_id(id: int, service: NotificationService = Depends(get_service)):
    notification = service.find_by_id(id)
    if notification is None:
        raise not_found(id)
    return notification


@router.post("", response_model=Notification, status_code=status.HTTP_201_CREATED, summary="Create a notification")
def create(data: NotificationCreate, service: NotificationService = Depends(get_service)):
    return service.create(data)


@router.patch("/{id}/read", response_model=Notification, summary="Mark a notification as read",
              responses={404: {"description": "Notification not found"}})
def mark_as_read(id: int, service: NotificationService = Depends(get_service)):
    notification = service.mark_as_read(id)
    if notification is None:
        raise not_found(id)
    return notification


@router.delete("/{id}", status_code=status.HTTP_204_NO_CONTENT, summary="Delete a notification",
               responses={404: {"description": "Notification not found"}})
def delete(id: int, service: NotificationService = Depends(get_service)):
    if not service.delete(id):
        raise not_found(id)
    return Response(status_code=status.HTTP_204_NO_CONTENT)
