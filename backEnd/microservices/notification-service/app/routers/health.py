from fastapi import APIRouter

router = APIRouter(tags=["Health"])


@router.get("/health", summary="Health check", description="Declared as healthCheckUrl and statusPageUrl in Eureka.")
def health():
    return {"status": "UP"}
