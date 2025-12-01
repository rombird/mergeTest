import joblib
from pathlib import Path
import numpy as np
import pandas as pd

BASE = Path(__file__).resolve().parents[1]
MODELS_DIR = BASE / 'models'

_model = None
_scaler = None
_features = None

def load_artifacts():
    global _model, _scaler, _features
    if _model is None:
        _model = joblib.load(MODELS_DIR / 'model.pkl')
    if _scaler is None:
        _scaler = joblib.load(MODELS_DIR / 'scaler.pkl')
    if _features is None:
        _features = joblib.load(MODELS_DIR / 'features.pkl')
    return _model, _scaler, _features


def prepare_dataframe(df: pd.DataFrame) -> np.ndarray:
    """입력 df를 feature 순서로 정렬하고 스케일링합니다.
    필요한 컬럼이 누락되면 KeyError를 발생시킵니다.
    """
    _, scaler, features = load_artifacts()
    missing = [c for c in features if c not in df.columns]
    if missing:
        raise KeyError(f"Missing columns for prediction: {missing}")

    X = df[features].copy()
    Xs = scaler.transform(X)
    return Xs

def predict_from_df(df: pd.DataFrame):
    model, _, _ = load_artifacts()
    Xs = prepare_dataframe(df)
    preds = model.predict(Xs)
    return preds