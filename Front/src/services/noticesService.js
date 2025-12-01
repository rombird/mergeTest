import api from "../api/axiosConfig";

// 데이터를 가져오고 보내는 방법에 대한 메뉴얼
// 백엔드의 특정 엔드포인트(URL)에 데이터를 어떤 방식으로(GET, POST, DELETE) 보낼지를 정확하게 약속하는 것
// 백엔드 API 통신과 관련된 모든 역할을 모아둔 곳



// 공지사항 목록 조회(GET /api/notices, 페이지네이션 및 검색 허용)
//  * @param {number} page - 페이지 번호 (0부터 시작, 기본값 0)
//  * @param {string} keyword - 검색 키워드 (기본값 "")
//  * @param {number} size - 페이지 크기 (기본값 10)
//  @returns {Promise<axios.Response<Page<NoticesDto>>>} - Spring Boot의 Page 객체 반환
export const fetchNotices = (page = 0, keyword = "", size = 10) => {
  return api.get("/api/notices", {
    params: {
      page: page,
      size: size,
      keyword: keyword,
    },
  });
};




// 공지사항 상세 조회(GET /api/notices/{id}, 공개 API)
// @param {number} id - 공지사항 ID
export const fetchNotice = (id) => api.get(`/api/notices/${id}`);


// 공지사항 작성(POST /api/notices, ADMIN 권한, 파일 업로드포함)
//  * formData는 NoticesDto 필드(noticesTitle, noticesContents)와 files 필드를 포함해야함
//  * Content-Type 처리는 api의 요청 인터셉터에서 자동 처리됨
//  * @param {FormData} formData - 공지사항 데이터 및 파일
export const createNotice = (formData) =>
    api.post("/api/notices", formData);



// 공지사항 수정(PUT /api/notices{id}, AMIND 권한, 파일 업로드/삭제 포함)
//  * formData는 NoticesDto 필드(noticesTitle, noticesContents, deletedFileIds)와 newFiles 필드를 포함해야함
//  * Content-Type 처리는 api의 요청 인터셉터에서 자동 처리
//  * @param {number} id - 공지사항 ID
// @param {FormData} formData - 수정된 공지사항 데이터 및 새 파일

export const updateNotice = (id, formData) =>
  api.put(`/api/notices/${id}`, formData);



/**
 * [파일 다운로드] 파일 다운로드 (GET /api/notices/download/{fileId}, 공개 API)
 * @param {number} fileId - 다운로드할 파일 ID
 */
export const downloadFile = (fileId) =>
  api.get(`/api/notices/download/${fileId}`, { 
    // 서버에서 바이너리 데이터(파일)를 받기 위해 필수 설정
    responseType: "blob" 
  });
  
  

/**
 * [삭제] 공지사항 삭제 (DELETE /api/notices/{id}, ADMIN 권한)
 * @param {number} id - 공지사항 ID
 */
export const deleteNotice = (id) => api.delete(`/api/notices/${id}`);