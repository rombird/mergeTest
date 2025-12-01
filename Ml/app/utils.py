import pandas as pd
from io import StringIO

def csv_bytes_to_df(b: bytes) -> pd.DataFrame:
    s = b.decode('utf-8')
    return pd.read_csv(StringIO(s))