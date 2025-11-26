/**
 * 매출 요약 API를 호출하여 데이터 가져오기
 */
export const fetchSalesSummary = async (guName, qtrCode) => {
  // Spring Boot API 엔드포인트: /api/sales/summary
  // 프록시 설정 덕분에 'http://localhost:8090'을 생략하고 상대 경로를 사용
  const url = `/api/sales/summary?guName=${guName}&qtrCode=${qtrCode}`;

  const response = await fetch(url);

  if (!response.ok) {
    // API 호출 실패 시 에러 메시지 처리
    const errorText = await response.text();
    // 오류 응답을 짧게 잘라 메시지에 포함
    const briefError = errorText.includes('500') ? 'Internal Server Error' : errorText.substring(0, 50) + '...';
    throw new Error(`HTTP ${response.status} 오류 발생: ${briefError}`);
  }

  return response.json();
};