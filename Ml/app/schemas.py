from pydantic import BaseModel, RootModel
from typing import List, Dict

class PredictRow(RootModel):
# 자유형: 각 feature에 대한 맵. 예: {"총_점포_수_합계": 12, ...}
    root: Dict[str, float]

class PredictRequest(BaseModel):
    data: List[PredictRow]

class PredictResponse(BaseModel):
    predictions: List[float]