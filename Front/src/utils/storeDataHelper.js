//fetch를 사용하여 public 폴더의 데이터를 비동기적으로 로드하고 포맷하는 코드

/**
 * 
 * @returns {Promise<Array<Object>>} 전체 데이터 배열 또는 빈 배열
 */
export const fetchStoreData = async () => {
    try {
        // public 폴더의 stores_num.json 파일에 접근
        const response = await fetch('/stores_num.json'); 
        
        if (!response.ok) {
            // 파일을 찾지 못하거나(404) 서버 오류 발생 시
            console.error(`🚨 Fetch error! HTTP status: ${response.status} (JSON 파일 로드 실패)`);
            return [];
        }
        
        const data = await response.json();
        console.log("✅ JSON Data Loaded Successfully:", data.slice(0, 2)); // 데이터 로드 성공 확인용 로그
        return data;
    } catch (error) {
        console.error("🚫 Failed to fetch store data:", error);
        return [];
    }
};

/**
 * 로드된 데이터와 조건을 받아 Recharts 형식으로 변환
 */
export const formatStoreRatioData = (storeData, district, quarter) => {
    if (!storeData || storeData.length === 0 || !district || !quarter) {
        return [];
    }

    // 조건에 맞는 데이터 객체를 찾음
    const filteredData = storeData.find(item => 
        item.city_name === district && item.date_qr === quarter
    );

    if (!filteredData) {
        // 데이터가 없으면 빈 배열 반환
        return [];
    }

    // stor_co: 일반 점포 수, frenc_store: 프랜차이즈 점포 수
    const { stor_co, frenc_store } = filteredData;

    return [
        { name: '일반 점포', uv: stor_co },
        { name: '프랜차이즈', uv: frenc_store },
    ];
};