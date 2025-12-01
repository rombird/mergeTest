// CKEditor 이미지 업로드 관련 클래스 및 플러그인
import axios from "../api/axiosConfig";

// 1. 이미지 업로드 커스텀 어댑터 클래스
export class MyUploadAdapter {
    constructor(loader) {
        this.loader = loader;
        // 서버의 이미지 업로드 API 엔드포인트
        this.url = 'http://localhost:8090/api/board/image/upload';
    }

    upload() {
        return this.loader.file
            .then(file => {
                const data = new FormData();
                // 서버 컨트롤러에서 받는 파라미터 이름이 'upload'로 일치해야 합니다.
                data.append('upload', file);

                return axios.post(this.url, data, {
                    headers: {
                        'Content-Type': 'multipart/form-data'
                    }
                })
                .then(res => {
                    // 서버 응답: {uploaded: 1, url: "http://localhost:8090/images/..."}
                    if (res.data.uploaded) {
                        return {
                            default: res.data.url
                        };
                    } else {
                        throw new Error('Image upload failed');
                    }
                })
                .catch(error => {
                    console.error("CKEditor 이미지 업로드 에러:", error);
                    return Promise.reject(error);
                });
            });
    }

    abort() {
        // 업로드 취소 로직
    }
}

// 2. CKEditor 플러그인 등록 함수
export function MyCustomUploadAdapterPlugin(editor) {
    editor.plugins.get('FileRepository').createUploadAdapter = (loader) => {
        return new MyUploadAdapter(loader);
    };
}