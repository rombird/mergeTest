import React, { useState, useEffect, useCallback } from 'react';
// 외부 모듈 및 헬퍼 함수 import
import { fetchSalesSummary } from '../services/salesApi';
import { formatSalesData, getChangeStyle, formatQtrCode, getPreviousQtrCode, getPreviousYearQtrCode } from '../utils/formatters';
import "../css/SalesAnalysis.css";
// 차트 컴포넌트와 로컬 데이터 헬퍼 함수 import
import StoreRatioPieChart from './StoreRatioPieChart';
// 🚨 비동기 로드 함수 import
import { fetchStoreData, formatStoreRatioData } from '../utils/storeDataHelper';

// --- 상수 정의 ---
const GU_OPTIONS = [
  '강남구', '강동구', '강북구', '강서구', '관악구', '광진구', '구로구', '금천구',
  '노원구', '도봉구', '동대문구', '동작구', '마포구', '서대문구', '서초구', '성동구',
  '성북구', '송파구', '양천구', '영등포구', '용산구', '은평구', '종로구', '중구', '중랑구',
];
const QTR_OPTIONS = [
  '20252', '20251', '20244', '20243', '20242', '20241', '20234', '20233', '20232', '20231',
  '20224', '20223', '20222', '20221', '20214', '20213', '20212', '20211', '20204', '20203',
  '20202', '20201', '20194', '20193', '20192', '20191'
];

const SalesAnalysis = () => {
  // --- State 정의 ---
  const [guName, setGuName] = useState(GU_OPTIONS[0]); // '강남구'
  const [qtrCode, setQtrCode] = useState(QTR_OPTIONS[0]); // '20252'
  const [data, setData] = useState(null); // API 데이터
  const [chartData, setChartData] = useState(null); // 차트 데이터 (null로 시작)
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  // API 데이터 로드 함수
  const loadSalesData = useCallback(async (selectedGu, selectedQtr) => {
    if (!selectedGu || !selectedQtr) return;
    setIsLoading(true);
    setError(null);
    setData(null);

    try {
      const result = await fetchSalesSummary(selectedGu, selectedQtr);
      setData(result);
    } catch (err) {
      console.warn("API 호출 실패:", err.message);
      setError(null); // 차트가 API 에러에 의해 가려지는 것을 방지
      setData(null);
    } finally {
      setIsLoading(false);
    }
  }, []);

  // --- useEffect: 데이터 호출 및 차트 데이터 로드 ---
  useEffect(() => {
    // 1. API 호출 (매출액 데이터)
    loadSalesData(guName, qtrCode);

    // 2. 로컬 데이터 비동기 호출 및 포맷팅 (차트 데이터)
    (async () => {
      const rawData = await fetchStoreData(); // JSON 데이터 비동기 로드
      const formattedData = formatStoreRatioData(rawData, guName, qtrCode);
      setChartData(formattedData);
      console.log(`Final Chart Data for ${guName}-${qtrCode}:`, formattedData); // 🚨 최종 데이터 확인!
    })();

  }, [guName, qtrCode, loadSalesData]);

  // --- JSX 렌더링을 위한 변수 포매팅 ---
  const currentQtrText = formatQtrCode(qtrCode);
  const currentSales = data && data.monthlyAverageSales ? formatSalesData(data.monthlyAverageSales, false) : '-';
  const qoqChangeText = data && data.qoqChange ? formatSalesData(data.qoqChange, true) : '-';
  const yoyChangeText = data && data.yoyChange ? formatSalesData(data.yoyChange, true) : '-';

  const previousQtrText = formatQtrCode(getPreviousQtrCode(qtrCode));
  const previousYearQtrText = formatQtrCode(getPreviousYearQtrCode(qtrCode));

  const previousQtrSales = data && data.previousQtrSales ? formatSalesData(data.previousQtrSales, false) : '-';
  const previousYearSales = data && data.previousYearSales ? formatSalesData(data.previousYearSales, false) : '-';


  // --- 컴포넌트 렌더링 ---
  return (
    <div className="analysis-widget-container">
      <div className="container-below-contents">
        <h2>{guName} 외식업 매출액 및 점포 분석</h2>

        {/* 드롭다운 */}
        <div className="dropdown-row">
          <select value={guName} onChange={(e) => setGuName(e.target.value)}>
            {GU_OPTIONS.map(gu => (
              <option key={gu} value={gu}>{gu}</option>
            ))}
          </select>

          <select value={qtrCode} onChange={(e) => setQtrCode(e.target.value)}>
            {QTR_OPTIONS.map(qtr => (
              <option key={qtr} value={qtr}>{formatQtrCode(qtr)}</option>
            ))}
          </select>
        </div>

        {/* 분석 결과 구역 */}
        <div className="analysis-content-area">
          {isLoading ? (
            <div className="loading-state">데이터를 불러오는 중입니다...</div>
          ) : (
            <div className="flex-charts-container"
              style={{
                display: "flex",
                gap: "80px",
                alignItems: "center",
                //                 flexWrap: "wrap"
                justifyContent: "center",
                // minWidth: "820px",
                // minHeight: "350px"
                
              }}
            >
              {/* 1. 매출 패널 박스 (data가 있거나 error가 없어야 표시) */}
              {data && !error ? (
                <div className="sales-card-box" style={{ width: '500px' }}>
                  <div className="card-header-info">
                    <h3>매출액</h3>
                    <p>{currentQtrText} 월평균 매출액</p>
                  </div>
                  {/* ... (매출액 카드 내용) ... */}
                  <div className="card-content">
                    <div className={`change-card ${data.qoqChange ? getChangeStyle(data.qoqChange) : 'change-neutral'}`}>
                      <p>전분기 대비</p>
                      <h2>{qoqChangeText}</h2>
                    </div>
                    <div className="current-sales-card">
                      <p>{currentQtrText}</p>
                      <h1>{currentSales}</h1>
                    </div>
                    <div className={`change-card ${data.yoyChange ? getChangeStyle(data.yoyChange) : 'change-neutral'}`}>
                      <p>전년 동분기 대비</p>
                      <h2>{yoyChangeText}</h2>
                    </div>
                  </div>
                  <div className="card-footer-info">
                    <div className="footer-data-row">
                      <span className="footer-label">{previousQtrText}</span>
                      <span className="footer-value">{previousQtrSales}</span>
                    </div>
                    <div className="footer-data-row">
                      <span className="footer-label">{previousYearQtrText}</span>
                      <span className="footer-value">{previousYearSales}</span>
                    </div>
                  </div>
                </div>
              ) : (
                // API 데이터 로드 실패 시 대체 메시지
                <div className="sales-card-box no-data-state" style={{ width: '400px', height: '300px', padding: '20px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                  <p style={{ textAlign: 'center', color: '#888' }}>매출액 데이터를 불러오지 못했습니다.</p>
                </div>
              )}


              {/* 2. 차트 렌더링: chartData가 있을 때만 렌더링 */}
              {chartData && chartData.length > 0 ? (
                <StoreRatioPieChart data={chartData}  />
              ) : (
                // 데이터 로딩 실패 시 StoreRatioPieChart 내부의 대체 UI가 렌더링됨
                <StoreRatioPieChart data={chartData} />
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default SalesAnalysis;