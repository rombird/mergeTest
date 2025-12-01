from fastapi import FastAPI, HTTPException, UploadFile, File
from fastapi.middleware.cors import CORSMiddleware
import pandas as pd


from .model_ops import predict_from_df
from .utils import csv_bytes_to_df
from .schemas import PredictRequest, PredictResponse


app = FastAPI(title='ML Prediction API')


# 개발환경: React(3000)과 Spring(예시)에 대한 CORS 허용
app.add_middleware(
CORSMiddleware,
allow_origins=['http://localhost:3000', 'http://localhost:8090'],
allow_credentials=True,
allow_methods=['*'],
allow_headers=['*']
)

@app.get('/health')
def health():
    return {'status': 'ok'}


@app.post('/predict', response_model=PredictResponse)
async def predict(req: PredictRequest):
    try:
        df = pd.DataFrame(req.data)
        preds = predict_from_df(df)
        return {'predictions': preds.tolist()}
    except KeyError as e:
        raise HTTPException(status_code=400, detail=str(e))
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.post('/predict_csv', response_model=PredictResponse)
async def predict_csv(file: UploadFile = File(...)):
    content = await file.read()
    df = csv_bytes_to_df(content)
    try:
        preds = predict_from_df(df)
        return {'predictions': preds.tolist()}
    except KeyError as e:
        raise HTTPException(status_code=400, detail=str(e))
