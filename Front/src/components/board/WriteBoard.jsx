// import axios from 'axios'; 
// import { useNavigate, useParams } from 'react-router-dom';
// import { Link } from 'react-router-dom';
// import api from '../../api/axiosConfig'; 
// import "../../css/login.css";
// import "../../css/writeBoard.css";

// // 게시글 등록, 수정 모두 가능
// const WriteBoard = () => {
//     const { id } = useParams(); // 글쓰기 수정 작업
//     const navigate = useNavigate();

//     // 게시글 등록 처리 함수
//     const handleSubmit = async (e) => {
//         e.preventDefault(); // 폼의 기본 제출 동작(새로고침)을 막습니다.
//         // 폼 데이터를 가져옵니다 (파일 포함)
//         const form = e.target;
//         const formData = new FormData(form);

//         try {
//             // 백엔드 API로 데이터 전송
//             // 파일 업로드 시에는 Content-Type을 multipart/form-data로 설정
//             const response = await api.post('/api/board/writeBoard', formData, {
//                 headers: {
//                     'Content-Type': 'multipart/form-data',
//                 },
//             });

//             if (response.status === 200 || response.status === 201) {
//                 alert("게시글이 작성되었습니다.");
//                 navigate('/api/board/paging');  // 게시글 목록 페이지로 이동
//             }
//         } catch (error) {
//             console.error("글 작성 실패:", error);
//             alert("글 작성 중 오류가 발생했습니다.");
//         }
//     };
    
 
//     return (
//         <>
//             <div className="write layoutCenter">
//                 <div className="sub-title">
//                     <div className="inquiry">
//                         <h3>커뮤니티 글쓰기</h3>
//                         <img src="../images/writing.png" alt="게시글 작성" />
//                     </div>
//                     <div className="path">
//                         <div>이용안내 &gt; Community </div>
//                     </div>
//                 </div>

//                 {/* <!-- 글 적는곳 --> */}
//                 <div className="write-space">
//                     <form onSubmit={handleSubmit} encType="multipart/form-data" >
//                         <div className="label-and-writeArea">
//                             <div className="label-area">
//                                 <div>제목</div>
//                             </div>
//                             <div className="write-area">
//                                 <input className="write-input" type="text" name="boardTitle" placeholder="제목"/>
//                             </div>
//                         </div>

//                         {/* <!-- 점선 --> */}
//                         <div className="line-dotted"></div>

//                         {/* <!-- 첨부파일 --> */}
//                         <div className="label-and-writeArea">
//                             <div className="label-area">
//                                 <div>첨부파일</div>
//                                 <div><i style={{color: '#3A6B71'}} class="fa-solid fa-star-of-life fa-2xs"></i></div>
//                             </div>
//                             <div className="write-area">
//                                 <div className="file-upload-info">
//                                     <div className="file-upload-info-left">
//                                         <div>용량 제한 : 6.0MB, 객수 제한 : 5개</div>
//                                         <div>파일 형식 : xlsx, pptx, txt, pdf, jpg, jpeg, png, hwp</div>
//                                     </div>
//                                     <input className="file-upload-btn" type="file" id="fileUpload" name="fileUpload" multiple/>
//                                     <label className="upload-btn" for="fileUpload">
//                                         <img src="../images/upload.png" alt="파일업로드" />
//                                     </label>
//                                 </div>
//                                 <div className="upload" id="upload">
//                                     <p>파일을 드래그하여 첨부할 수 있습니다</p>
//                                 </div>
//                             </div>
//                         </div>    

//                         <div className="line-dotted"></div>

//                         <div className="label-and-writeArea">
//                             <div className="label-area content-label">
//                                 <div>내용</div>
//                             </div>
//                             <div className="write-area content-area">
//                                 <div className="main-container">
//                                     <div className="editor-container editor-container_classic-editor" id="editor-container">
//                                         <div className="editor-container__editor">
//                                             <textarea className="editor" name="boardContents" id="editor" placeholder="나의 소식을 공유해보세요!" ></textarea>
//                                         </div>
//                                     </div>
//                                 </div>
//                             </div>
//                         </div>
//                         <div className="under-line-dotted line-dotted"></div>
//                     </form>
//                     {/* <!-- 제출 버튼 있는 줄 --> */}
//                     <div className="submit-box layoutCenter">
//                         <button className="submit-btn" type="submit" >등록</button>
//                     </div>
//                 </div>
//             </div>
            

//         </>

//     )

// }

// export default WriteBoard;


import axios from "axios";
import { useNavigate } from 'react-router-dom';
import { Link } from 'react-router-dom';
import React, { Fragment, useState, useCallback, useRef, useEffect } from 'react';
import { CKEditor } from "@ckeditor/ckeditor5-react";
import ClassicEditor from "@ckeditor/ckeditor5-build-classic";

import "../../css/common.css";
import "../../css/writeBoard.css";
import "../../css/ckEditorStyle.css";


class MyUploadAdapter{
    constructor(loader){
        // 파일 정보 로더
        this.loader = loader;
        // 서버의 이미지 업로드 API 엔드포인트
        this.url = 'http://localhost:8090/api/board/image/upload';
    }

    // 파일 전송 메서드
    upload(){
        return this.loader.file
                .then(file => {
                    const data = new FormData();
                    // 서버 컨트롤러에서 받는 파라미터 이름이 upload(RequestParam)로 일치하여야함
                data.append('upload', file);

                return axios.post(this.url, data, {
                    headers: {
                        'Content-Type' : 'multipart/form-data'
                    }
                })
                .then(res => {
                    // 서버 응답: {uploaded: 1, url: "http://localhost:8090/images/..."}
                    // CKEditor 형식에 맞게 변환하여 반환
                    if (res.data.uploaded){     // uploaded는 CKEditor가 요구하는 Header에 맞춘거
                        return{
                            default: res.data.url
                        };
                    }else{
                        throw new Error('Image upload failed');
                    }
                })
                .catch(error => {
                    console.error("CKEditor 이미지 업로드 에러:", error);
                });
            });
    }
    // 어댑터가 취소될 때 호출 (필수)
    abort(){
        // 업로드 취소 로직
    }
}

// 커스텀 업로드 어댑터를 CKEditor 플러그인으로 등록하는 함수
function MyCustomUploadAdapterPlugin(editor){
    editor.plugins.get('FileRepository').createUploadAdapter = (loader) => {
        return new MyUploadAdapter(loader);
    };
}



const WriteBoard = () => {

    const navigate = useNavigate();
    const [boardContents, setBoardContents] = useState("");

    // 1. 상태관리
    const [uploadedFiles, setUploadFiles] = useState([]);

    // 2. DOM 요소 참조
    const uploadAreaRef = useRef(null);
    const fileInputRef = useRef(null);

    // 3. 파일 처리 로직
    const formatBytes = (bytes) => {
        if (bytes === 0) return '0 Bytes';
        const k = 1024;
        const sizes = ['Bytes', 'KB', 'MB', 'GB']
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i]
    }

    // 4. 파일 유효성 검사 및 추가하는 함수
    const allowedExtensions = ['xlsx', 'pptx', 'txt', 'pdf', 'jpg', 'jpeg', 'png', 'hwp'];
    const maxCount = 5;
    const maxSize = 20 * 1024 * 1024; // 6MB

    // CKEditor 설정 객체 정의
    const editorConfig = {

        image: {
            upload: {
                types: ['png', 'jpeg', 'gif', 'bmp', 'webp'],
                // 서버 업로드 api 경로
            withCredentials: true,
            }
        },

        // 1. 커스텀 업로드 어댑터 플러그인 등록
        extraPlugins: [MyCustomUploadAdapterPlugin],

        // 2. 툴바 버튼 설정(imageUploade 버튼 포함)
        toolbar: [ 'heading', '|', 'bold', 'italic', 'link', 'bulletedList', 'numberedList', 'blockQuote', 'insertImage', 'mediaEmbed', 'undo', 'redo' ]
    };




    const handleFiles = useCallback((files) => {
        const newFiles = Array.from(files);
        let validFiles = [];

        for (const file of newFiles) {
            const ext = file.name.split('.').pop().toLowerCase();

            // 1. 확장자 검사
            if (!allowedExtensions.includes(ext)) {
                alert(`${file.name}: 지원하지 않는 형식입니다.`);
                continue;
            }

            // 2. 파일 개수 제한 검사 (현재 파일 개수 + 추가할 파일 개수)
            if (uploadedFiles.length + validFiles.length >= maxCount) {
                alert("최대 5개까지 업로드 할 수 있습니다");
                break; // 5개 넘기면 더 이상 파일을 처리하지 않음
            }

            // 3. 파일 크기 검사
            if (file.size > maxSize) {
                alert(`${file.name} : 20MB 초과`);
                continue;
            }

            // 4. 중복 파일명 검사
            if (uploadedFiles.some(f => f.name === file.name) || validFiles.some(f => f.name === file.name)) {
                alert(`${file.name}은 이미 업로드 되어 있습니다.`);
                continue;
            }
            validFiles.push(file);
        }

        // 유효한 파일만 상태에 추가
        if (validFiles.length > 0) {
            setUploadFiles(prevFiles => [...prevFiles, ...validFiles]);
        }

        // 파일 input 초기화(같은 파일 재선택 가능하도록)
        if (fileInputRef.current) {
            fileInputRef.current.value = "";
        }
    }, [uploadedFiles]);   // uploadedFiles가 변경될 때마다 함수 재생성

    // 파일 입력(input)의 change 이벤트 핸들러
    const onFileInputChange = (e) => {
        handleFiles(e.target.files);
    };

    // 파일 삭제 함수
    const deleteFile = (fileName, fileSize) => {
        setUploadFiles(prevFiles => {
            const updatedFiles = prevFiles.filter(f => !(f.name === fileName && f.size === fileSize));
            return updatedFiles;
        });
    };

    // 드래그 앤 드롭 이벤트(useEffect 사용해서 들어오면 하나 추가 삭제하면 하나 삭제)
    useEffect(() => {
        const uploadArea = uploadAreaRef.current;
        if (!uploadArea) return;

        // 드래그 시 시각효과
        const handleDragOver = (e) => {
            e.preventDefault();
            uploadArea.classList.add("dragover");
        }

        // 드래그 해온 거 빠지면 dragover 시각효과 사라짐
        const handleDragLeave = () => {
            uploadArea.classList.remove("dragover");
        }

        // 파일 드롭 시 처리
        const handleDrop = (e) => {
            e.preventDefault();
            uploadArea.classList.remove("dragover");
            handleFiles(e.dataTransfer.files);
        }

        uploadArea.addEventListener("dragover", handleDragOver);
        uploadArea.addEventListener("dragleave", handleDragLeave);
        uploadArea.addEventListener("drop", handleDrop);

        // 컴포넌트 언마운트 시 이벤트 제거
        return () => {
            uploadArea.removeEventListener("dragover", handleDragOver);
            uploadArea.removeEventListener("dragleave", handleDragLeave);
            uploadArea.removeEventListener("drop", handleDrop);
        };
    }, [handleFiles]);      // handleFiles(즉 uploadFiles)가 변경될 때마다 useEffect 재실행

    // 제출 버튼 핸들러(API 호출 로직)
    const handleSubmit = (e) => {
        e.preventDefault();

        // 필수 입력 항목 유효성 검사 (추가 필요)
        const boardTitle = e.target.elements.boardTitle.value;
        const boardWriter = e.target.elements.boardWriter.value;
        const boardPass = e.target.elements.boardPass.value;

        if (!boardTitle || !boardWriter || !boardPass || !boardContents) {
            alert("제목, 글쓴이, 비밀번호, 내용을 모두 입력해주세요.");
            return; // 유효성 검사 실패 시 전송 중단
        }

        const formData = new FormData(e.target);    // 폼의 다른 입력 필드 포함

        // CKEditor 내용 추가
        formData.append("boardContents", boardContents);

        // 파일들을 formData에 추가
        uploadedFiles.forEach(file => {
            formData.append("fileUpload", file); // fileUpload는 서버에서 파일 받을 때 쓰는 이름
        });

        console.log("폼 데이터 전송 준비 완료. 파일 개수: ", uploadedFiles.length);


        // axios를 이용한 서버 전송
        axios.post('http://localhost:8090/api/board/writeBoard', formData, {
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        })
            .then(res => {
                alert("게시글이 성공적으로 작성되었습니다.");
                navigate('/board/Paging'); // 제출하면 게시글 페이지로 이동
            })
            .catch(error => {
                console.error("게시글 작성 실패:", error);
                alert("게시글 작성 중 오류가 발생했습니다.");
            });

    };

    return (
        <>


            <div className="custom">
                <header className="header">
                    <div className="topHeader">
                        <div className="topList layoutCenter">
                            <ul className="topNav">
                                <li className="topNavli">
                                    <Link to="user/Login"><img className="imgLogin" src="@{/image/person.svg}" alt="" />로그인</Link>
                                </li>
                                <li className="topNavli">
                                    <Link to="user/Join">회원가입</Link>
                                </li>
                            </ul>
                        </div>
                    </div>

                    <div className="navHeader">
                        {/* <!-- layoutCenter를 줄지 말지 풀드롭다운에 영향 미칠지 확인 --> */}
                        <div className="detailnav layoutCenter">
                            <h1><a href="/main">LOGO</a></h1>
                            <div className="wrap">
                                <ul className="mainNav">
                                    <li className="mainList">
                                        <div className="listLine">
                                            <a className="mainMenu" href="#">빅데이터 상권 분석</a>
                                        </div>
                                        <ul className="subNav">
                                            <li className="subList">
                                                <a href="#">맞춤형정보</a>
                                            </li>
                                            <li className="subList">
                                                <a href="#">맞춤형정보2</a>
                                            </li>
                                            <li className="subList">
                                                <a href="#">상권정보3</a>
                                            </li>
                                        </ul>

                                    </li>
                                    <li className="mainList">
                                        <div className="listLine">
                                            <a className="mainMenu" href="#">상권시장 TREND</a>
                                        </div>
                                        <ul className="subNav">
                                            <li className="subList">
                                                <a href="#">Trend NOW</a>
                                            </li>
                                            <li className="subList">
                                                <a href="#">NEWS</a>
                                            </li>
                                            <li className="subList">
                                                <a href="#">Trend 3</a>
                                            </li>
                                        </ul>
                                    </li>

                                    <li className="mainList">
                                        <div className="listLine">
                                            <a className="mainMenu" href="#">소상공인 대시보드</a>
                                        </div>
                                        <ul className="subNav">
                                            <li className="subList">
                                                <a href="#">내 가게 경영진단</a>
                                            </li>
                                            <li className="subList">
                                                <a href="#">커뮤니티</a>
                                            </li>
                                            <li className="subList">
                                                <a href="#"></a>
                                            </li>
                                        </ul>
                                    </li>
                                    <li className="mainList">
                                        <div className="listLine">
                                            <a className="mainMenu" href="#">이용안내</a>
                                        </div>
                                        <ul className="subNav">
                                            <li className="subList">
                                                <a href="#">공지사항</a>
                                            </li>
                                            <li className="subList">
                                                <a href="#">문의사항</a>
                                            </li>
                                            <li className="subList">
                                                <Link to="/user/Paging">커뮤니티</Link>
                                            </li>
                                        </ul>
                                    </li>
                                </ul>
                            </div>
                        </div>
                    </div>
                </header>


                {/* <!-- 게시글 작성 공간 --> */}
                <main>
                    <div className="layoutCenter">

                        {/* <!-- 문의사항 ?, 별 아이콘 ..... --> */}
                        <div className="sub-title">
                            {/* <!-- 문의사항, 아이콘 --> */}
                            <div className="inquiry">
                                <h3>문의사항</h3>
                                <div className="question-mark-container">
                                    <div className="question-mark"><i className="fa-solid fa-question fa-lg"></i></div>
                                    <div className="question-mark-hidden">문의사항</div>
                                </div>
                                <div className="question-mark-container">
                                    <div className="question-mark"><i className="fa-solid fa-star fa-lg"></i></div>
                                    <div className="question-mark-hidden">즐겨찾기 추가</div>
                                </div>
                            </div>

                            {/* <!-- 경로 --> */}
                            <div className="path">
                                <div><i className="fa-solid fa-house"></i></div>
                                <div>이용안내</div>
                                <div><i className="fa-solid fa-chevron-right"></i></div>
                                <div>문의사항</div>
                                <div><i className="fa-solid fa-chevron-right"></i></div>
                                <a className="inquiry-right-side" href="/board/paging">커뮤니티</a>
                            </div>
                        </div>

                        {/* <!-- 글 적는곳 --> */}
                        <div className="write-space">
                            <form onSubmit={handleSubmit} action="/board/writeBoard" method="post" encType="multipart/form-data">

                                {/* <!-- 제목 부분 --> */}
                                <div className="label-and-writeArea">
                                    <div className="label-area">
                                        <div>제목</div>
                                        <div><i style={{ color: '#3A6B71' }} className="fa-solid fa-star-of-life fa-2xs"></i></div>
                                    </div>
                                    <div className="write-area">
                                        <input className="write-input" type="text" name="boardTitle" />
                                    </div>
                                </div>

                                {/* <!-- 점선 --> */}
                                <div className="line-dotted"></div>

                                {/* <!-- 글쓴이 부분 --> */}
                                <div className="label-and-writeArea">
                                    <div className="label-area">
                                        <div>글쓴이</div>
                                        <div><i style={{ color: '#3A6B71' }} className="fa-solid fa-star-of-life fa-2xs"></i></div>
                                    </div>
                                    <div className="write-area">
                                        <input className="write-input" type="text" name="boardWriter" />
                                    </div>
                                </div>

                                {/* <!-- 점선 --> */}
                                <div className="line-dotted"></div>

                                {/* <!-- 비밀번호 부분 --> */}
                                <div className="label-and-writeArea">
                                    <div className="label-area">
                                        <div>비밀번호</div>
                                        <div><i style={{ color: '#3A6B71' }} className="fa-solid fa-star-of-life fa-2xs"></i></div>
                                    </div>
                                    <div className="write-area">
                                        <input className="write-input" type="text" name="boardPass" />
                                    </div>
                                </div>

                                {/* <!-- 점선 --> */}
                                <div className="line-dotted"></div>


                                {/* <!-- 내용 부분 --> */}
                                <div className="label-and-writeArea">
                                    <div className="label-area content-label">
                                        <div>내용</div>
                                        <div><i style={{ color: '#3A6B71' }} className="fa-solid fa-star-of-life fa-2xs"></i></div>
                                    </div>
                                    <div className="write-area content-area">
                                        <div className="main-container">
                                            <div className="editor-container editor-container_classic-editor" id="editor-container">
                                                <div className="editor-container__editor">
                                                    {/* <textarea className="editor" name="boardContents" id="editor"></textarea> */}
                                                    <CKEditor
                                                        editor={ClassicEditor}
                                                        data=""
                                                        config={editorConfig}
                                                        onChange={(event, editor) => {
                                                            const data = editor.getData();
                                                            setBoardContents(data);
                                                        }}
                                                    />
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>


                                {/* <!-- 점선 --> */}
                                <div className="line-dotted"></div>

                                {/* <!-- 첨부파일 --> */}
                                <div className="label-and-writeArea">
                                    <div className="label-area">
                                        <div>첨부파일</div>
                                        <div><i style={{ color: '#3A6B71' }} className="fa-solid fa-star-of-life fa-2xs"></i></div>
                                    </div>
                                    <div className="write-area">
                                        <div className="file-upload-info">
                                            <div className="file-upload-info-left">
                                                <div>용량 제한 : {formatBytes(maxSize)}, 개수제한 : {maxCount}개</div>
                                                <div>파일 형식 : {allowedExtensions.join(', ')}</div>
                                            </div>
                                            <input
                                                className="file-upload-btn"
                                                type="file"
                                                id="fileUpload"
                                                name="fileUpload"
                                                multiple
                                                ref={fileInputRef}  // useRef 연결
                                                onChange={onFileInputChange} />
                                            <label className="upload-btn" htmlFor="fileUpload">
                                                <i className="fa-solid fa-upload"></i>
                                            </label>
                                        </div>

                                        {/* 파일 업로드 영역 및 미리보기 */}
                                        <div className="upload" id="upload" ref={uploadAreaRef}>
                                            {/* 조건부 렌더링 */}
                                            {uploadedFiles.length === 0 ? (
                                                <p>파일을 드래그하여 첨부할 수 있습니다</p>
                                            ) : (
                                                <div className="preview-container">
                                                    {uploadedFiles.map((file, index) => (
                                                        // key는 React가 목록 요소를 식별하는 데 도움을 줍니다.
                                                        <React.Fragment key={file.name + file.size}>
                                                            {/* 첫 번째 요소가 아닐 경우에만 점선 추가 */}
                                                            {index > 0 && (
                                                                <div className="line-dotted-preview"></div>
                                                            )}

                                                            <div className="preview-box">
                                                                <div>
                                                                    <div className="file-name">{file.name}</div>
                                                                    <div className="file-size">{formatBytes(file.size)}</div>
                                                                </div>
                                                                <button
                                                                    className="delete-btn"
                                                                    type="button"
                                                                    onClick={() => deleteFile(file.name, file.size)}
                                                                >
                                                                    <i className="fa-solid fa-trash fa-lg"></i>
                                                                </button>
                                                            </div>
                                                        </React.Fragment>
                                                    ))}
                                                </div>
                                            )}
                                        </div>
                                    </div>
                                </div>

                                {/* */}
                                <div className="under-line-dotted line-dotted"></div>

                                {/* */}
                                <div className="submit-btn-group">
                                    <button style={{ borderColor: '#3A6B71', color: '#335f64' }} type="submit" className="btn btn-outline-success btn-lg">제출</button>

                                    <button style={{ borderColor: '#3A6B71', color: '#335f64' }} type="button" className="btn btn-outline-success btn-lg">
                                        <Link to="/board/paging" style={{ textDecoration: 'none', color: 'inherit' }}>목록</Link>
                                    </button>

                                </div>
                            </form>
                        </div>
                    </div>
                </main>


                <footer className="">
                    <div className="mainFooter layoutCenter">
                        <div className="footerLogo">
                            <h1>LOGO</h1>
                        </div>
                        <div className="footerInfo">
                            <div className="footerInfoL">
                                <p>대구광역시 중구 중앙대로 366</p>
                                <p>임과 함께</p>
                                <p>admin@gmail.com</p>
                                <p>053-123-4567</p>
                            </div>
                            <div className="footerInfoR">
                                <ul className="site">
                                    <li><a href="#">FAQ</a></li>
                                    <li><a href="#">사이트맵</a></li>
                                </ul>
                                <ul className="related">
                                    <li><a href="#">관련기관정보</a></li>
                                </ul>
                                <ul className="personInfo">
                                    <li><a href="#">개인정보처리방침</a></li>
                                    <li><a href="#">이용약관</a></li>
                                </ul>
                            </div>
                        </div>
                    </div>
                </footer>
            </div>
        </>
    )

}

export default WriteBoard;


