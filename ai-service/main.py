import uvicorn
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from datetime import datetime
import re

app = FastAPI(title="ATM Security AI Service", description="Real-time threat and anomaly analysis service")

class AlertAnalysisRequest(BaseModel):
    stationCode: str
    alertType: str
    message: str
    timestamp: str

class AlertAnalysisResponse(BaseModel):
    anomalyScore: float
    isAnomaly: bool

@app.post("/api/ai/analyze", response_model=AlertAnalysisResponse)
async def analyze_alert(request: AlertAnalysisRequest):
    anomaly_score = 0.0
    is_anomaly = False

    # Check for unregistered station
    if request.stationCode == "UNKNOWN":
        anomaly_score = 0.95
        is_anomaly = True

    # Check alert hour
    hour = datetime.now().hour
    try:
        # Try parsing timestamp
        # e.g. "2026-06-16T16:36:13"
        dt = datetime.fromisoformat(request.timestamp.split(".")[0])
        hour = dt.hour
    except Exception:
        pass

    # High risk alert types at night are highly anomalous
    is_night_time = hour < 6 or hour >= 22
    if is_night_time:
        if request.alertType in ["DOOR_OPEN", "PHYSICAL_TAMPERING"]:
            anomaly_score = max(anomaly_score, 0.90)
            is_anomaly = True
        elif request.alertType == "POWER_FAILURE":
            anomaly_score = max(anomaly_score, 0.70)
            is_anomaly = True
    else:
        # Day time check
        if request.alertType == "PHYSICAL_TAMPERING":
            anomaly_score = max(anomaly_score, 0.80)
            is_anomaly = True
        elif request.alertType == "DOOR_OPEN":
            # Door opening during the day is warning unless unauthorized
            anomaly_score = max(anomaly_score, 0.30)
        elif request.alertType == "FIRE_ALARM":
            anomaly_score = max(anomaly_score, 0.85)
            is_anomaly = True

    # Analyze message pattern (repetitive keywords, warning levels)
    # Check if there are multiple warnings in a single message
    alert_keywords = ["unauthorized", "failed", "breach", "smoke", "tamper", "alarm"]
    matches = sum(1 for kw in alert_keywords if kw in request.message.lower())
    if matches >= 2:
        anomaly_score = max(anomaly_score, 0.75)
        is_anomaly = True

    # Final logic cap
    if anomaly_score >= 0.70:
        is_anomaly = True

    return AlertAnalysisResponse(
        anomalyScore=round(anomaly_score, 4),
        isAnomaly=is_anomaly
    )

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)
