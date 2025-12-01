import axios from "axios";
import { useNavigate, Link, useParams, useSearchParams } from 'react-router-dom';
import { useEffect, useState } from "react";
import { useAuth } from '../../context/AuthContext';
import moment from 'moment'; 

import "../../css/common.css";
import "../../css/boardDetail.css";

const NoticeDetail = () => {
    const { id: noticeId } = useParams(); // url경로가 /notice/:id
    const navigate = useNavigate(); // 페이지 이동을 위한 함수
    const { user, isLoggedIn } = useAuth(); // 로그인 상태 및 사용자 정보 활요
    const [searchParams] =  useSearchParams();
    
    console.log('게시글 ID:', noticeId);

    // 상태 관리
    const [notice, setNotice] = useState(null); // 게시글 데이터 (NoticeDto)
    // const [commentList, setCommentList] = useState([]); // 댓글은 배열로
    // const [commentInput, setCommentInput] = useState({
    //     writer: '',
    //     content: ''
    // });

    const [page, setPage] = useState(1);    // 목록으로 돌아갈 때 필요한 페이지 정보
 
    useEffect(() => {
        if(!noticeId) return;

        const fetchNoticeDetail = async () => {
            try{
                // 백엔드 API 호출: GET /api/notice/{id}
                console.log("noticeId", noticeId);
                const response = await axios.get(`http://localhost:8090/api/notice/${noticeId}`);
                // 응답 데이터 구조: NoticeDetailResponse {noticeDto, commentDtoList}

                // console.log("어떻게 들어오는거야?" , response.data);

                const noticeData = response.data.noticeDto;
                // const commentsData = response.data.commentDtoList || [];
                
                console.log("게시글 정보:" , noticeData);

                setNotice(noticeData);
                // setCommentList(commentsData);
            }
            catch(error){
                console.error(`게시글${noticeId} 조회 실패: `, error);
                alert("게시글 정보를 불러오는데 실패 혹은 해당 게시글이 존재하지 않습니다");
                navigate('/api/notices');  // 실패시 게시글 목록페이지로 이동
            }
        };
        fetchNoticeDetail();
    }, [noticeId, navigate]);

    // 댓글 작성
    // 댓글 입력 필드 변경 핸들러
    // const handleCommentInputChange = (e) => {
    //     const{id, value} = e.target;
    //     const stateKey = id === "comment-writer" ? "writer" : "content";

    //     setCommentInput(prev => ({
    //         ...prev,
    //         [stateKey]: value
    //     }));
    // };

    // // 댓글 작성 처리 (Post /comment/save)
    // const commentWrite = async () => {
    //     // 로그인한 회원만 댓글 작성할 수 있도록 
    //     const writer = user ? user.name : '';
    //     const {content} = commentInput;
        
    //     if(!isLoggedIn){
    //         alert("댓글을 작성하려면 로그인해야 합니다.");
    //         navigate('/login', { state: { from: window.location.pathname } }); // 로그인하고 다시 댓글페이지로
    //         return;
    //     }
    //     if(!content){
    //         alert("내용을 입력하세요.");
    //         return;
    //     }
    //     try{
    //         // 백엔드 API 호출: Post api/comment/save
    //         const res = await axios.post("http://localhost:8090/api/comment/save",{
    //             // CommentDto의 필드에 값 넣어주기
    //             commentWriter : writer,
    //             commentContents: content,
    //             noticeId: notice.id   // 현재 게시글의 Id
    //         }, {withCredentials: true

    //         });

    //         // 서버에서 반환된 갱신된 댓글 목록으로 상태 업데이트
    //         setCommentList(res.data);
    //         setCommentInput({writer: '', content: ''});     // 입력필드 초기화
        
    //     }catch(error){
    //         console.error("댓글 작성 실패:", error);
    //         alert("댓글 작성 중 오류가 발생했습니다.");
    //     }
    // };

    // // 댓글 작성시 Enter 키 입력 처리 함수
    // const handleEnterKey = (e) => {
    //     if(e.key === 'Enter' || e.keyCode === 13){
    //         e.preventDefault();
    //         commentWrite();
    //     }
    // }

    // 페이지 이동 핸들러
    const listReq = () => {
        // 
        navigate(`/api/notices`);
    };

    // 수정 페이지 이동
    const updateReq = () => {
        navigate(`/notice/update/${notice.id}`);
    };

    // 삭제 요청
    const deleteReq = async () => {
        if(window.confirm("정말 삭제하시겠습니까?")){
            try{
                // 1. 백엔드 API 호출 : Delete /api/notice/delete/{id}
                // axios.delete 메서드 사용
            
            await axios.delete(`http://localhost:8090/api/notice/delete/${notice.id}`);
            
            // 2. 성공 시 알림 및 목록으로 이동
            alert("게시글이 삭제되었습니다");
            navigate('/api/notices');
        
        }catch(error){
            console.error("삭제 실패: ", error);
            alert("삭제 중 오류가 발생했습니다");
        }
    }
};

// 작성자/ 관리자 확인 로직
// DMIN 권한을 가진 사용자에게만 수정/삭제 버튼을 보여줄거야 
const isAdminUser = user && (user.role === 'ADMIN' || user.roleType === 'ADMIN');

// 로딩 처리
if (!notice){
    return <div>공지사항 데이터를 불러오는 중입니다...</div>
}

    return (
        <div className="boardDetail layoutCenter">
            <div className="pathtitle">
                <div className="path">
                    <p>이용안내 &gt; Community </p>
                </div>
            </div>
            <div className="boardContent">
                <div className="content-number">
                    <p>NO. {notice.id}</p>
                </div>
                <div className="content-title">
                    <h3>{notice.noticeTitle}</h3>
                </div>
                <div className="writer">
                    <p>{notice.noticeWriter}</p>
                </div>
                <div className="date-hit">
                    <div className="date">
                        <img src="../../images/clock.png" alt="시계사진넣기" />
                        <p>{moment(notice.noticeCreateTime).format('YYYY-MM-DD HH:mm:ss')}</p>
                    </div>
                    <div className="hit">
                        <img src="../../images/read.png" alt="조회" />
                        <p>조회수: {notice.noticeHits}</p>
                    </div>
                </div>
                <div className="content-line"></div>
                <div className="contentkey">
                    <div style={{whiteSpace: 'pre-wrap'}}>{notice.noticeContents}</div>
                </div>

                {/* 파일 첨부 목록 표시 */}
                <div className="fileadd">
                    {notice.noticeFileAttached === 1 && notice.noticeFileDtoList && notice.noticeFileDtoList.length > 0 && (
                        notice.noticeFileDtoList.map((file, index) => (
                                    <div key={index} className="filename">
                                        <p>첨부파일 </p>
                                        <a 
                                            href={`http://localhost:8090/api/notice/download/${notice.id}/${index}`}
                                            target="_blank" rel="noopener noreferrer"
                                        >
                                            {file.originalFilename}
                                        </a>
                                    </div>
                                ))
                            )
                        }
                </div>
            </div>
            <div className="action-btn">
                <button className="listback-btn" onClick={listReq}>목록</button>
                {isAdminUser && (
                    <>
                        <button className="update-btn" onClick={updateReq}>수정</button>
                        <button className="delete-btn" onClick={deleteReq}>삭제</button>    
                    </>
                )}
            </div>
            {/* <div className="comment">
                <h4>댓글 작성</h4>
                <input 
                    className="comment-write"
                    type="text" id="comment-writer" placeholder="작성자 이름"
                    value={commentInput.writer} onChange={handleCommentInputChange}
                    onKeyDown={handleEnterKey}
                />
                <input 
                    className="comment-content"
                    type="text" id="comment-contents" placeholder="내용"
                    value={commentInput.content} onChange={handleCommentInputChange}
                    onKeyDown={handleEnterKey}
                />
                <button className="comment-btn" onClick={commentWrite}>작성</button>
            </div>
            <div className="comment-list">
                <h4>댓글 목록</h4>
                {commentList.length > 0 ? (
                    <div className="comment-detail-wrapper">
                        {commentList.map((comment) => (
                            <div key={comment.id} className="comment-item">
                                <p className="comment-item-list">
                                    <span className="comment-writer">{comment.commentWriter}</span>
                                    <span className="comment-time">{moment(
                                            comment.commentCreatedTime, 
                                            'YYYYMMDDHHmmssSSS' // 들어오는 문자열의 포맷을 명시
                                        ).format('YYYY-MM-DD HH:mm:ss')}</span>
                                    <span classnName="comment-content">{comment.commentContents}</span>
                                </p>
                            </div>
                            ))}
                    </div>        
                ) : ( <p>등록된 댓글이 없습니다.</p> )
                }
            </div> */}
        </div>
    );
}

export default NoticeDetail;
