import axios from 'axios';

// axios 인스턴스 생성
const api = axios.create({
  baseURL: 'http://localhost:8090',
  withCredentials: true, // HTTP-Only 쿠키 포함
  timeout:10000, // 10초 타임아웃
  headers:{"Content-Type": "application/json",},
});

const REFRESH_URL = '/reissue'; // 백엔드 재발급 엔드포인트
//------------------------
// 요청 인터셉터 설정
//------------------------
api.interceptors.request.use(
  // async (config) => {
    (config) => {
    // 로그인 페이지나 회원가입 페이지 등 인증이 필요없는 경로는 제외
    const publicPaths = ['/login', '/join'];
    // 로그인/회원가입 요청은 그대로 통과
    if (publicPaths.some(path => config.url.includes(path))) {
      return config;
    }

    // 그 외 요청도  config 반드시 return
    return config;
  },
  (error) => {
    console.log("[오류-요청 인터셉터] ",error);
    window.location.href = '/login';
    return Promise.reject(error);
  }
);

//------------------------
// 응답 인터셉터 설정
//------------------------
api.interceptors.response.use(
  (response) => {
    console.log("[정상-응답 인터셉터] ",response);
    if (response.data?.auth === false) {
      window.location.href = '/login';
      return Promise.reject('세션이 만료되었습니다.');
    }
    return response;
  },
  async (error) => {
    // 응답은 왔지만 HTTP 에러 상태인 경우
    console.log("[오류-응답 인터셉터] ",error);

    const origianlRequest = error.config;
    const status = error.response?.status;
    
    // AccessToken 만료 및 재발급 처리(401에러)
    if (status == 401 && origianlRequest.url !== REFRESH_URL && !origianlRequest._retry){
      origianlRequest._retry = true; // 무한루프 방지

      try {
        // Access token 재발급 요청
        await api.post(REFRESH_URL, null);
        console.log("재발급 성공 Access Token 갱신 완료. 요청 재시도");

        return api(origianlRequest);
      }catch(refreshError){
        console.error("재발급 실패 Refresh Token도 만료됨. 로그인 필요.");

        if(window.location.pathname !== '/login'){
          window.location.href = "/login";
        }
        return Promise.reject(refreshError);
      }
    } 
    // 기존의 HTTP 에러 상태 처리
    // if (error.response?.data?.expired === true) {
    //   window.location.href = '/login';
    // }

    if (error.response) {
            switch (status) {
                case 400:
                    console.error("잘못된 요청 (400):", error.response.data);
                    break;
                // 401은 위에서 처리했으나, 재발급 후에도 다시 401이 발생하면 오류 처리
                case 401: 
                    console.error("인증 실패 (401): 로그인 페이지로 이동");
                    // 재발급 로직이 작동했지만 다시 401이면 무시하고 넘어감 (혹은 /user/login으로 이동)
                    if (window.location.pathname !== '/login') {
                        window.location.href = "/login"; 
                    }
                    break;
                case 403:
                    console.error("권한 없음 (403): ADMIN 권한 필요");
                    break;
                case 404:
                    console.error("요청한 리소스를 찾을 수 없음 (404)");
                    break;
                case 500:
                    console.error("서버 내부 에러 (500)");
                    break;
                default:
                    console.error(`API Error ${status}:`, error.response.data);
            }
        }
        // 요청은 보냈지만 응답이 없는 경우 (네트워크, 서버 다운 등)
        else if (error.request) {
            if (error.code === "ECONNABORTED") {
                console.error("요청 시간 초과 (timeout)");
            } else {
                console.error("서버 응답 없음 또는 네트워크 오류:", error.message);
            }
        }
        // 요청 자체가 잘못된 경우
        else {
            console.error("요청 설정 오류:", error.message);
        }

    return Promise.reject(error);
  }
);

export default api; 