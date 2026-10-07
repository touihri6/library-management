from datetime import datetime
from enum import Enum

from pydantic import BaseModel, Field


class NotificationType(str, Enum):
    LOAN_REMINDER = "LOAN_REMINDER"
    EVENT_ANNOUNCEMENT = "EVENT_ANNOUNCEMENT"
    GENERAL = "GENERAL"


class NotificationCreate(BaseModel):
    recipient: str = Field(min_length=1, max_length=120, examples=["amine@mail.com"])
    message: str = Field(min_length=1, max_length=500, examples=["Votre emprunt arrive à échéance demain"])
    type: NotificationType = Field(examples=["LOAN_REMINDER"])


class Notification(BaseModel):
    id: int
    recipient: str
    message: str
    type: NotificationType
    read: bool
    createdAt: datetime
