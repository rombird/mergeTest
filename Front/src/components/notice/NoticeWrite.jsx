import axios from 'axios';
import {Link, useNavigate, useParams} from 'react-router-dom';
import React, {Fragment, useState, useCallback, useRef, useEffect} from 'react';

// import { CKEditor } from "@ckeditor/ckeditor5-react";
// import ClassicEditor from "@ckeditor/ckeditor5-build-classic";
// import { MyCustomUploadAdapterPlugin } from '../../services/CKEditorAdapter';

import api from '../../api/axiosConfig';
import { formatBytes, allowedExtensions, maxCount, maxSize, validateAndGetFiles } from '../../services/noticeFileUtils';
// CSS 파일은 재활용

import "../../css/login.css";
import "../../css/writeBoard.css";

const NoticeWrite = () => {
    const {id} = useParams();   // 공지사항 수정 작업 시 필요한 id
    const navigate = useNavigate();
    const isEditMode = !!id;    // 수정 모드 여부

    // 텍스트 상태관리
    const [noticeTitle, setNoticeTitle] = useState("");  
    const [noticeWriter, setNoticeWriter] = useState(""); 
    const [noticeContents, setNoticeContents] = useState(""); 

    // 파일 상태관리
    const [uploadedFiles, setUploadedFiles] = useState([]);
    const [existingFiles, setExistingFiles] = useState([]);
    const [filesToDeleteIds, setFilesToDeleteIds] = useState([]);

    // Dom 요소 참조
    const uploadAreaRef = useRef(null);
    const fileInputRef = useRef(null);

    // 파일 처리 로직 (기존 코드 유지)
    const formatBytes = (bytes) => {
        if (bytes === 0) return '0 Bytes';
        const k = 1024;
        const sizes = ['Bytes', 'KB', 'MB', 'GB']
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i]
    }

    // 파일 유효성 검사 및 추가하는 함수 (기존 코드 유지)
    const allowedExtensions = ['xlsx', 'pptx', 'txt', 'pdf', 'jpg', 'jpeg', 'png', 'hwp'];
    const maxCount = 5;
    const maxSize = 20 * 1024 * 1024; // 20MB
    // 참고: 두 번째 요청의 주석은 6MB로 되어 있으나, 로직은 20MB이므로 로직을 따라 20MB로 유지합니다.

    // 파일 유효성 검사 및 추가하는 함수
    const handleFiles = useCallback((files) => {
        // 분리된 유틸리티 함수를 사용하여 유효성 검사 및 유효 파일 목록 받기
        const validFiles = validateAndGetFiles(files, uploadedFiles, existingFiles, filesToDeleteIds);

        // 유효한 파일만 상태에 추가
        if (validFiles.length > 0) {
            setUploadedFiles(prevFiles => [...prevFiles, ...validFiles]);
        }

        // 파일 input 초기화 (같은 파일 재선택 가능하도록)
        if (fileInputRef.current) {
            fileInputRef.current.value = "";
        }
    }, [uploadedFiles, existingFiles, filesToDeleteIds]); // 의존성 배열 유지

    // 파일 입력(input)의 change 이벤트 핸들러
    const onFileInputChange = (e) => {
        handleFiles(e.target.files);
    };

    // 파일 삭제 함수
    const deleteFile = (fileName, fileSize) => {
        setUploadedFiles(prevFiles => { // setUploadedFiles로 수정
        return prevFiles.filter(f => !(f.name === fileName && f.size === fileSize));
        });
    };

    // 기존 파일 삭제 함수(서버에 파일 ID 전송 대기)
    const deleteExistingFile = (fileId) => {
        // existingFiles 목록에서만 시각적으로 제거되도록 filesToDeleteIds에 ID 추가
        if (!filesToDeleteIds.includes(fileId)) {
            setFilesToDeleteIds(prevIds => [...prevIds, fileId]);
        }
    };
    
    // 드래그 앤 드롭 이벤트 등록 (기존 코드 유지)
    useEffect(() => {
        const uploadArea = uploadAreaRef.current;
        if (!uploadArea) return;

        const handleDragOver = (e) => {
            e.preventDefault();
            uploadArea.classList.add("dragover");
        }

        const handleDragLeave = () => {
            uploadArea.classList.remove("dragover");
        }

        const handleDrop = (e) => {
            e.preventDefault();
            uploadArea.classList.remove("dragover");
            handleFiles(e.dataTransfer.files);
        }

        uploadArea.addEventListener("dragover", handleDragOver);
        uploadArea.addEventListener("dragleave", handleDragLeave);
        uploadArea.addEventListener("drop", handleDrop);

        return () => {
            uploadArea.removeEventListener("dragover", handleDragOver);
            uploadArea.removeEventListener("dragleave", handleDragLeave);
            uploadArea.removeEventListener("drop", handleDrop);
        };
    }, [handleFiles]);

    useEffect(() => {
        if(isEditMode){
            axios.get(`http://localhost:8090/api/notice/${id}`)
            .then(response => {
                console.log("뭐가 들어오는가? ", response.data);
                const data = response.data.noticeDto;
                if(!data) throw new Error("공지사항 데이터가 없습니다");

                setNoticeTitle(data.noticeTitle);   // 제목 설정
                setNoticeWriter(data.noticeWriter); // 글쓴이 설정
                setNoticeContents(data.noticeContents); // 글 쓴 내용

                if(data.noticeFileDtoList){ 
                    setExistingFiles(data.noticeFileDtoList);
                }
            }) 
            .catch(error => {
                console.error("공지사항 로딩 실패", error);
                alert("공지사항 정보를 불러오는데 실패하였습니다. 목록으로 돌아갑니다");
                navigate('/api/notices');
            });
        }
    }, [id, navigate, isEditMode]);

    //     // CKEditor 설정 객체 정의
    // const editorConfig = {
    //     image: {
    //         upload: {
    //             types: ['png', 'jpeg', 'gif', 'bmp', 'webp'],
    //             withCredentials: true,
    //         }
    //     },
    //     extraPlugins: [MyCustomUploadAdapterPlugin],
    //     toolbar: [ 'heading', '|', 'bold', 'italic', 'link', 'bulletedList', 'numberedList', 'blockQuote', 'insertImage', 'mediaEmbed', 'undo', 'redo' ]
    // };

    // 게시글 등록 처리 함수
    const handleSubmit = async (e) => {
        e.preventDefault();

        const form = e.target;
        const submitTitle = form.elements.noticeTitle ? form.elements.noticeTitle.value : '';
        const submitWriter = form.elements.noticeWriter ? form.elements.noticeWriter.value : ''; // 비회원 작성자 필드가 없는 경우 대비
        // const boardPass = form.elements.boardPass ? form.elements.boardPass.value : ''; 

        // 필수 입력 항목 유효성 검사
        if (!submitTitle || !submitWriter || !noticeContents) {
            alert("제목, 글쓴이, 내용을 모두 입력해주세요."); // 비밀번호 언급 제거
            return;
        }

        const formData = new FormData();

        // 공지사항 필드명으로 변경하여 formData에 추가
        formData.append("noticeTitle", submitTitle);
        formData.append("noticeWriter", submitWriter);
        formData.append("noticeContents", noticeContents);
        // formData.append("noticePass", noticePass);

        // 파일들을 formData에 추가
        uploadedFiles.forEach(file => {
            formData.append("uploadFiles", file);
        });

        const url = isEditMode ? `/api/notice/update/${id}` : '/api/notice/save';
        const method = isEditMode ? 'put' : 'post';

        console.log("폼 데이터 전송 준비 완료. 파일 개수: ", uploadedFiles.length);
        try {
            // 백엔드 API로 데이터 전송
            if (isEditMode){
                // 수정 모드: 삭제할 기존 파일 ID 목록 추가
                filesToDeleteIds
                    .filter(fileId => fileId !== undefined && fileId !== null && fileId !== "")
                    .forEach(fileId => {
                        formData.append("deleteFileIds", fileId); 
                    });
            }
            let response;
            if(method === 'post'){
                response= await api.post('/api/notice/save', formData, {
                    headers: {'Content-Type': 'multipart/form-data'},
                });     
            } else{
                response = await api.put(`/api/notice/update/${id}`, formData, {
                    headers:{'Content-Type':'multipart/form-data'}
                })
            }

            if (response.status === 200 || response.status){
                alert(isEditMode ? "공지사항이 수정되었습니다." : "공지사항이 작성되었습니다");
                navigate(isEditMode ? `/notice/${id}` : "/api/notices");
            }
        }catch(error){
            console.error("공지사항 처리 실패", error);
            const status = error.response?.status;

            if(isEditMode && status === 401){
                alert("공지사항 수정에 필요한 권한이 없거나 오류가 발생했습니다.");
            }else{
                alert("공지사항 처리 중 오류가 발생했습니다.");
            }
        }
    };
    

    return (
        <>
            <div className="write layoutCenter">
                <div className="sub-title">
                    <div className="inquiry">
                        <h3>공지사항{isEditMode ? '수정' : '작성'}</h3>
                        <img src="../../images/writing.png" alt="공지사항 작성" />
                    </div>
                    <div className="path">
                        <div>이용안내 &gt; Community </div>
                    </div>
                </div>

                {/* <!-- 글 적는곳 --> */}
                <div className="write-space">
                    <form onSubmit={handleSubmit} encType="multipart/form-data" >
                        <div className="label-and-writeArea">
                            <div className="label-area">
                                <div>제목</div>
                            </div>
                            <div className="write-area">
                                <input 
                                    className="write-input" 
                                    type="text" 
                                    name="noticeTitle" 
                                    value={noticeTitle} 
                                    onChange={(e) => setNoticeTitle(e.target.value)} />
                            </div>
                        </div>

                        {/* <!-- 점선 --> */}
                        <div className="line-dotted"></div>

                        {/* <!-- 글쓴이 부분 --> */}
                        <div className="label-and-writeArea">
                            <div className="label-area">
                                <div>글쓴이</div>
                            </div>
                            <div className="write-area">
                                <input 
                                className="write-input" 
                                type="text" 
                                name="noticeWriter"
                                value={noticeWriter}
                                onChange={(e) => setNoticeWriter(e.target.value)} />
                            </div>
                        </div>
                        
                        {/* <!-- 점선 --> */}
                        <div className="line-dotted"></div>

                        {/* <!-- 비밀번호 부분 --> */}
                        {/* <div className="label-and-writeArea">
                            <div className="label-area">
                                <div>비밀번호</div>
                            </div>
                            <div className="write-area">
                                <input className="write-input" type="text" name="boardPass" 
                                value={boardPass}
                                    onChange={(e) => setBoardPass(e.target.value)}
                                    placeholder={isEditMode ? "수정을 위해 비밀번호를 입력해주세요." : "비밀번호를 입력해주세요."} />
                            </div>
                        </div> */}

                        {/* <!-- 점선 --> */}
                        {/* <div className="line-dotted"></div> */}

                        {/* <!-- 첨부파일 --> */}
                        <div className="label-and-writeArea">
                            <div className="label-area">
                                <div>첨부파일</div>
                                <div><i className="fa-solid fa-star-of-life fa-2xs"></i></div>
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
                                        id="noticeFileUpload"
                                        name="noticeFileUpload"
                                        multiple
                                        ref={fileInputRef}  // useRef 연결
                                        onChange={onFileInputChange} />
                                    <label className="upload-btn" htmlFor="noticeFileUpload">
                                        <i className="fa-solid fa-upload"></i>
                                    </label>
                                </div>

                                {/* 파일 업로드 영역 및 미리보기 */}
                                <div className="upload" id="upload" ref={uploadAreaRef}>
                                    {existingFiles.filter(f => !filesToDeleteIds.includes(f.id)).length === 0 && uploadedFiles.length === 0 ? (
                                        <p>파일을 드래그하여 첨부할 수 있습니다</p>
                                    ) : (
                                        <div className="preview-container">
                                            {existingFiles.filter(f => !filesToDeleteIds.includes(f.id)).map((file, index) => (
                                                // key는 React가 목록 요소를 식별하는 데 도움을 줍니다.
                                                <React.Fragment key={file.id}>
                                                    {/* 첫 번째 요소가 아닐 경우에만 점선 추가 */}
                                                    {index > 0 && (
                                                        <div className="line-dotted-preview"></div>
                                                    )}
                                                    <div className="preview-box">
                                                        <div>
                                                            <div className="file-name">{file.originalFilename}</div>
                                                            <div className="file-size">{formatBytes(file.fileSize)}</div>
                                                        </div>
                                                        <button
                                                            className="delete-btn"
                                                            type="button"
                                                            onClick={() => deleteExistingFile(file.id)}
                                                        >
                                                            <i className="fa-solid fa-trash fa-lg"></i>
                                                        </button>
                                                    </div>
                                                </React.Fragment>
                                            ))}

                                            {/* 2. 새로 업로드된 파일 목록 */}
                                            {uploadedFiles.map((file, index) => (
                                                <React.Fragment key={file.name + file.size}>
                                                    {(existingFiles.filter(f => !filesToDeleteIds.includes(f.id)).length > 0 || index > 0) && (
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

                        <div className="line-dotted"></div>

                        <div className="label-and-writeArea">
                            <div className="label-area content-label">
                                <div>내용</div>
                            </div>
                            <div className="write-area content-area">
                                <div className="main-container">
                                    <div className="editor-container editor-container_classic-editor" id="editor-container">
                                        <div className="editor-container__editor">
                                            <textarea
                                                className="notice-textarea" 
                                                value={noticeContents}
                                                onChange={(e) => setNoticeContents(e.target.value)}
                                                placeholder="내용을 입력해주세요."
                                            ></textarea>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div className="under-line-dotted line-dotted"></div>
                        {/* <!-- 제출 버튼 있는 줄 --> */}
                        <div className="submit-box layoutCenter">
                            <button type="button" className="list-btn" onClick={() => navigate('/api/notices')}>
                                목록
                            </button>
                            <button type="submit" className="submit-btn" >{isEditMode ? "수정" : "등록"}</button>
                        </div>
                    </form>
                    
                </div>
            </div>
            
        </>
    )

}
export default NoticeWrite