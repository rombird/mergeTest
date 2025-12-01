// 데이터 포멧팅 및 스타일 로직 담당

/**
 * 원 단위를 억 원 단위로 포맷팅하고 부호를 붙여 문자열로 변환
 */
export const formatSalesData = (value, isChange = false) => {
  if (value === null || value === undefined) return isChange ? '0' : '-';

  const unit = 100000000;
  const unitValue = value / unit;

   let sign = '';
  if (isChange) {
      if (value > 0) {
          sign = '+'; // 증가 시 + 부호
      } else if (value < 0) {
          sign = '-'; // 감소 시 - 부호 (Math.abs()로 절대값을 취하기 전에 부호를 결정)
      }
  }
  const absValue = Math.abs(unitValue);

  // 정수로 반올림하여 '억' 단위로 깔끔하게 표시
  const formattedValue = Math.round(absValue).toLocaleString();

  return `${sign} ${formattedValue} 억`;
};

/**
 * 증감 값에 따라 스타일 클래스를 반환
 */
export const getChangeStyle = (value) => {
  if (value > 0) return 'change-positive'; // 증가 시 (핑크/빨간색)
  if (value < 0) return 'change-negative'; // 감소 시 (파란색)
  return 'change-neutral';
};

// 현재 분기 코드를 연도와 분기로 변환 (예: 20224 -> 2022년 4분기)
export const formatQtrCode = (qtrCode) => {
    if (!qtrCode || qtrCode.length !== 5) return '선택 분기';
    const year = qtrCode.substring(0, 4);
    const quarter = qtrCode.substring(4, 5);
    return `${year}년 ${quarter}분기`;
}

/**
 * 이전 분기 코드 계산 (예: 20252 -> 20251, 20251 -> 20244)
 */
export const getPreviousQtrCode = (qtrCode) => {
  if (!qtrCode || qtrCode.length !== 5) return '';
  
  const year = parseInt(qtrCode.substring(0, 4));
  const quarter = parseInt(qtrCode.substring(4, 5));
  
  if (quarter === 1) {
    return `${year - 1}4`;
  } else {
    return `${year}${quarter - 1}`;
  }
};

/**
 * 전년 동분기 코드 계산 (예: 20252 -> 20242)
 */
export const getPreviousYearQtrCode = (qtrCode) => {
  if (!qtrCode || qtrCode.length !== 5) return '';
  
  const year = parseInt(qtrCode.substring(0, 4));
  const quarter = qtrCode.substring(4, 5);
  
  return `${year - 1}${quarter}`;
};