import React, { useState } from 'react';
import axios from 'axios'; // axios 설치가 안 되어 있다면: npm install axios

function PredictionForm() {
  // 1. 입력 상태 관리: 예측에 필요한 모든 독립 변수를 여기에 정의
  const [inputs, setInputs] = useState({
    총_점포_수_합계: 0,
    // 2024년_총_유동인구_합계: 0,
    // 2024년_총_상주인구_합계 : 0,
    영역_면적 : 0,
    월_평균_소득_금액 : 0,
    지출_총금액 : 0,
    집객_지수 : 0,
    식료품_지출_총금액:0,
    음식_지출_총금액 : 0,
    요식업_총지출 : 0,
    요식업_지출비율 : 0
    // TODO: 여기에 모든 독립 변수 (X 컬럼)를 추가해야 합니다.
  });

  // 2. 예측 결과 상태 관리
  const [prediction, setPrediction] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  // 3. 입력값 변경 핸들러
  const handleChange = (e) => {
    const { name, value } = e.target;
    setInputs(prev => ({
      ...prev,
      [name]: parseFloat(value) || 0, // 숫자로 변환
    }));
  };

  // 4. API 호출 (Spring Boot의 /api/predict-from-ml 호출)
  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setPrediction(null);

    //  Spring Boot 서버 주소 및 포트 확인
    const SPRING_API_URL = 'http://localhost:8090/api/predict-from-ml'; 

    try {
      // Spring Boot는 JSON 객체 하나를 받기를 기대합니다.
      const response = await axios.post(SPRING_API_URL, inputs);
      
      // Spring Boot에서 Double 타입(단일 예측값)을 응답으로 보냈다고 가정
      setPrediction(response.data);

    } catch (err) {
      console.error("API 호출 오류:", err);
      // 서버 응답이 오류일 경우 처리
      setError('예측에 실패했습니다. 서버 로그를 확인하세요.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ padding: '20px', maxWidth: '600px', margin: 'auto' }}>
      <h2>상권 매력도 지수 예측</h2>
      <form onSubmit={handleSubmit}>
        
        {/* ----------------- 입력 필드 ----------------- */}
        <div style={inputGroupStyle}>
          <label style={labelStyle}>총_점포_수_합계:</label>
          <input
            type="number"
            name="총_점포_수_합계"
            value={inputs.총_점포_수_합계}
            onChange={handleChange}
            style={inputStyle}
            required
          />
        </div>

        <div style={inputGroupStyle}>
          <label style={labelStyle}>유동인구_비율:</label>
          <input
            type="number"
            name="유동인구_비율"
            value={inputs.유동인구_비율}
            onChange={handleChange}
            style={inputStyle}
            required
          />
        </div>

        <div style={inputGroupStyle}>
          <label style={labelStyle}>소득_수준:</label>
          <input
            type="number"
            name="소득_수준"
            value={inputs.소득_수준}
            onChange={handleChange}
            style={inputStyle}
            required
          />
        </div>
        
        {/* TODO: 여기에 나머지 독립 변수에 대한 입력 필드를 추가하세요 */}
        
        {/* ----------------- 버튼 및 결과 ----------------- */}
        <button type="submit" disabled={loading} style={buttonStyle}>
          {loading ? '예측 중...' : '상권 매력도 예측 시작'}
        </button>
      </form>

      {/* 결과 표시 */}
      {prediction !== null && (
        <div style={resultStyle}>
          <h3>✨ 예측 결과:</h3>
          <p style={{ fontWeight: 'bold', fontSize: '1.2em' }}>
            상권 매력도 지수: {prediction.toFixed(4)}
          </p>
          <p> (0.0에 가까울수록 낮고, 1.0에 가까울수록 높습니다.) </p>
        </div>
      )}

      {error && <p style={{ color: 'red', marginTop: '10px' }}>오류: {error}</p>}
    </div>
  );
}

// 간단한 인라인 스타일 정의 (CSS 대신)
const inputGroupStyle = {
    marginBottom: '15px',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
};

const labelStyle = {
    flex: 1,
    marginRight: '10px',
    fontWeight: 'bold',
};

const inputStyle = {
    flex: 2,
    padding: '8px',
    borderRadius: '4px',
    border: '1px solid #ccc',
    textAlign: 'right',
};

const buttonStyle = {
    padding: '10px 20px',
    backgroundColor: '#007bff',
    color: 'white',
    border: 'none',
    borderRadius: '5px',
    cursor: 'pointer',
    marginTop: '20px',
};

const resultStyle = {
    marginTop: '30px',
    padding: '15px',
    border: '2px solid #28a745',
    borderRadius: '5px',
    backgroundColor: '#e9f7ee',
};

export default PredictionForm;