# train_and_save.py
import joblib
import pandas as pd
import numpy as np
from pathlib import Path
from sklearn.preprocessing import StandardScaler
from sklearn.linear_model import Lasso
from sklearn.model_selection import train_test_split
from sklearn.metrics import r2_score, mean_squared_error


MODELS_DIR = Path(__file__).resolve().parent / 'models'
MODELS_DIR.mkdir(exist_ok=True)


# 사용자 설정 부분: 데이터 경로와 타깃/제외 컬럼
DATA_PATH = Path('data/x_y_df.csv') # 사용자의 데이터 경로로 변경
TARGET_COL = '상권매력도지수' # 이미 생성된 Y
EXCLUDE_COLS = ['행정동_코드','행정동_코드_명', '유동상주비율', '수요공급비율', '밀집도',
                '소비잠재력비율', TARGET_COL]


def train_and_save():
    df = pd.read_csv(DATA_PATH)

    features = [c for c in df.columns if c not in EXCLUDE_COLS]
    X = df[features]
    y = df[TARGET_COL]

    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

    scaler = StandardScaler()
    X_train_s = scaler.fit_transform(X_train)
    X_test_s = scaler.transform(X_test)

    # Lasso 하이퍼파라미터는 필요 시 변경
    lasso = Lasso(alpha=0.0001, max_iter=10000)
    lasso.fit(X_train_s, y_train)

    # 저장
    joblib.dump(lasso, MODELS_DIR / 'model.pkl')
    joblib.dump(scaler, MODELS_DIR / 'scaler.pkl')
    joblib.dump(features, MODELS_DIR / 'features.pkl')

    # 성능 출력
    pred = lasso.predict(X_test_s)
    print('R2:', r2_score(y_test, pred))
    print('RMSE:', np.sqrt(mean_squared_error(y_test, pred)))

if __name__ == '__main__':
    train_and_save()