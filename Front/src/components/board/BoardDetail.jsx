
import axios from "axios";
import { useNavigate, Link, useParams } from 'react-router-dom';
import { useEffect, useState } from "react";

import "../../css/common.css";

// 수정: 모든 로직을 Paging 함수 컴포넌트 내부에 정의합니다.
const BoardDetail = () => {
    // 1. URL에서 게시글 ID(pathVariable)를 가져옴
    const { id: boardId } = useParams(); // url경로가 /board/:id
    const navigate = useNavigate(); // 페이지 이동을 위한 함수

    console.log('게시글 ID:', boardId);
    // 상태 관리
    const [board, setBoard] = useState(null); // 게시글 데이터 (BoardDto)
    const [commentList, setCommentList] = useState([]); // 댓글은 배열로
    const [commentInput, setCommentInput] = useState({
        writer: '',
        content: ''
    });

    const [page, setPage] = useState(1);    // 목록으로 돌아갈 때 필요한 페이지 정보
 
    // 2. 데이터 가져오기
    useEffect(() => {
        if(!boardId) return;

        const fetchBoardDetail = async () => {
            try{
                // 백엔드 API 호출: GET /api/board/{id}
                console.log("boardId",boardId);
                const response = await axios.get(`http://localhost:8090/api/board/${boardId}`);
                // 응답 데이터 구조: BoardDetailResponse {boardDto, commentDtoList}

                const boardData = response.data.board;
                const commentsData = response.data.commentDtoList || [];
                
                console.log("게시글 정보:" , boardData);

                setBoard(boardData);
                setCommentList(commentsData);
            }
            catch(error){
                console.error(`게시글${boardId} 조회 실패: `, error);
                alert("게시글 정보를 불러오는데 실패 혹은 해당 게시글이 존재하지 않습니다");
                navigate('/board/paging');  // 실패시 게시글 목록페이지로 이동
            }
        };
        fetchBoardDetail();
    }, [boardId, navigate]);

    // 3. 이벤트 핸들러(댓글 작성)
   
    // 댓글 입력 필드 변경 핸들러
    const handleCommentInputChange = (e) => {
        const{id, value} = e.target;
        const stateKey = id === "comment-writer" ? "writer" : "content";

        setCommentInput(prev => ({
            ...prev,
            [stateKey]: value
        }));
    };

    // 댓글 작성 처리 (Post /comment/save)
    const commentWrite = async () => {
        const {writer, content} = commentInput;
    
        if(!writer || !content){
            alert("작성자와 내용을 모두 입력하세요");
            return;
        }

        try{
            // 백엔드 API 호출: Post api/comment/save
            const res = await axios.post("http://localhost:8090/api/comment/save",{
                // CommentDto의 필드에 값 넣어주기
                commentWriter : writer,
                commentContents: content,
                boardId: board.id   // 현재 게시글의 Id
            });

            // 서버에서 반환된 갱신된 댓글 목록으로 상태 업데이트
            setCommentList(res.data);
            setCommentInput({writer: '', content: ''});     // 입력필드 초기화
        
        }catch(error){
            console.error("댓글 작성 실패:", error);
            alert("댓글 작성 중 오류가 발생했습니다.");
        }
    };

    // 댓글 작성시 Enter 키 입력 처리 함수
    const handleEnterKey = (e) => {
        if(e.key === 'Enter' || e.keyCode === 13){
            commentWrite();
        }
    }

    // 4. 페이지 이동 핸들러

    // 목록 이동(url에 페이지 정보 포함)
    const listReq = () => {
        // 
        navigate(`/board/paging?page=${page}`);
    };

    // 수정 페이지 이동
    const updateReq = () => {
        navigate(`/board/update/${board.id}`);
    };

    // 삭제 요청
    const deleteReq = async () => {
        if(window.confirm("정말 삭제하시겠습니까?")){
            try{
                // 1. 백엔드 API 호출 : Delete /api/board/delete/{id}
                // axios.delete 메서드 사용
            
            await axios.delete(`http://localhost:8090/api/board/delete/${board.id}`);
            
            // 2. 성공 시 알림 및 목록으로 이동
            alert("게시글이 삭제되었습니다");
            navigate(`/board/Paging`);
        
        }catch(error){
            console.error("삭제 실패: ", error);
            alert("삭제 중 오류가 발생했습니다");
        }
    }
};
    // 로딩 처리
    if (!board){
        return <div>게시글 데이터를 불러오는 중입니다...</div>
    }

    return (
        <div className="boardDetail">
            <h2>게시글 상세</h2>

            {/* 게시글 정보 */}
            <section className="boardContent">
                <table>
                    <tbody>
                        <tr><th>게시글번호</th><td>{board.id}</td></tr>
                        <tr><th>작성자</th><td>{board.boardWriter}</td></tr>
                        <tr><th>제목</th><td>{board.boardTitle}</td></tr>
                        <tr><th>작성일</th><td>{board.boardCreateTime}</td></tr>
                        <tr><th>조회수</th><td>{board.boardHits}</td></tr>
                        <tr><th>내용</th>
                            <td
                                dangerouslySetInnerHTML={{ __html: board.boardContents }}
                            ></td>
                        </tr>
                    
                        {/* 파일 첨부 */}
                        {board.fileAttached === 1 && board.boardFileDtoList && board.boardFileDtoList.length > 0 && (
                            <tr>
                                <th>업로드한 파일</th>
                                <td>
                                    {board.boardFileDtoList.map((file, index) => (
                                        <div key={index}>
                                            <a 
                                                href={`http://localhost:8090/api/board/download/${board.id}/${index}`}
                                                target="_blank" rel="noopener noreferrer"
                                            >
                                                {file.originalFilename}
                                            </a>
                                        </div>
                                    ))}
                                </td>
                            </tr>
                        )}
                    </tbody>
                </table>
            </section>

            <hr />

            {/* 액션 버튼 */}
            <section className="actionButtons">
                <button onClick={listReq}>목록</button>
                <button onClick={updateReq}>수정</button>
                <button onClick={deleteReq}>삭제</button>
            </section>

            <hr />

            {/* 댓글 작성 섹션 */}
            <section id="comment-write">
                <h3>댓글 작성</h3>
                <input 
                    type="text" id="comment-writer" placeholder="작성자 이름"
                    value={commentInput.writer} onChange={handleCommentInputChange}
                    onKeyPress={handleEnterKey}
                />
                <input 
                    type="text" id="comment-contents" placeholder="내용"
                    value={commentInput.content} onChange={handleCommentInputChange}
                    onKeyPress={handleEnterKey}
                />
                <button onClick={commentWrite}>댓글 작성</button>
            </section>



            <hr />

            {/* 댓글 목록 섹션 */}
            <section id="comment-list">
                <h3>댓글 목록</h3>
                {commentList.length > 0 ? (
                    <table>
                        <thead>
                            <tr><th>댓글번호</th><th>작성자</th><th>내용</th><th>작성시간</th></tr>
                        </thead>
                        <tbody>
                            {commentList.map((comment) => (
                                <tr key={comment.id}>
                                    <td>{comment.id}</td>
                                    <td>{comment.commentWriter}</td>
                                    <td>{comment.commentContents}</td>
                                    <td>{comment.commentCreatedTime}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                ) : (
                    <p>등록된 댓글이 없습니다.</p>
                )}
            </section>
        </div>
    );
}

export default BoardDetail;



// import axios from "axios";
// import { useNavigate, Link, useParams } from 'react-router-dom';
// import { useEffect, useState } from "react";

// import "../../css/common.css";

// // 수정: 모든 로직을 Paging 함수 컴포넌트 내부에 정의합니다.
// const BoardDetail = () => {
//     // 1. URL에서 게시글 ID(pathVariable)를 가져옴
//     const { id: boardId } = useParams(); // url경로가 /board/:id
//     const navigate = useNavigate(); // 페이지 이동을 위한 함수

//     console.log('게시글 ID:', boardId);
//     // 상태 관리
//     const [board, setBoard] = useState(null); // 게시글 데이터 (BoardDto)
//     const [commentList, setCommentList] = useState([]); // 댓글은 배열로
//     const [commentInput, setCommentInput] = useState({
//         writer: '',
//         content: ''
//     });

//     const [page, setPage] = useState(1);    // 목록으로 돌아갈 때 필요한 페이지 정보
 
//     // 2. 데이터 가져오기
//     useEffect(() => {
//         if(!boardId) return;

//         const fetchBoardDetail = async () => {
//             try{
//                 // 백엔드 API 호출: GET /api/board/{id}
//                 console.log("boardId",boardId);
//                 const response = await axios.get(`http://localhost:8090/api/board/${boardId}`);
//                 // 응답 데이터 구조: BoardDetailResponse {boardDto, commentDtoList}

//                 const boardData = response.data.board;
//                 const commentsData = response.data.commentDtoList || [];
                
//                 console.log("게시글 정보:" , boardData);

//                 setBoard(boardData);
//                 setCommentList(commentsData);
//             }
//             catch(error){
//                 console.error(`게시글${boardId} 조회 실패: `, error);
//                 alert("게시글 정보를 불러오는데 실패 혹은 해당 게시글이 존재하지 않습니다");
//                 navigate('/board/paging');  // 실패시 게시글 목록페이지로 이동
//             }
//         };
//         fetchBoardDetail();
//     }, [boardId, navigate]);

//     // 3. 이벤트 핸들러(댓글 작성)
   
//     // 댓글 입력 필드 변경 핸들러
//     const handleCommentInputChange = (e) => {
//         const{id, value} = e.target;
//         const stateKey = id === "comment-writer" ? "writer" : "content";

//         setCommentInput(prev => ({
//             ...prev,
//             [stateKey]: value
//         }));
//     };

//     // 댓글 작성 처리 (Post /comment/save)
//     const commentWrite = async () => {
//         const {writer, content} = commentInput;
    
//         if(!writer || !content){
//             alert("작성자와 내용을 모두 입력하세요");
//             return;
//         }

//         try{
//             // 백엔드 API 호출: Post api/comment/save
//             const res = await axios.post("http://localhost:8090/api/comment/save",{
//                 // CommentDto의 필드에 값 넣어주기
//                 commentWriter : writer,
//                 commentContents: content,
//                 boardId: board.id   // 현재 게시글의 Id
//             });

//             // 서버에서 반환된 갱신된 댓글 목록으로 상태 업데이트
//             setCommentList(res.data);
//             setCommentInput({writer: '', content: ''});     // 입력필드 초기화
        
//         }catch(error){
//             console.error("댓글 작성 실패:", error);
//             alert("댓글 작성 중 오류가 발생했습니다.");
//         }
//     };

//     // 댓글 작성시 Enter 키 입력 처리 함수
//     const handleEnterKey = (e) => {
//         if(e.key === 'Enter' || e.keyCode === 13){
//             commentWrite();
//         }
//     }

//     // 4. 페이지 이동 핸들러

//     // 목록 이동(url에 페이지 정보 포함)
//     const listReq = () => {
//         // 
//         navigate(`/board/paging?page=${page}`);
//     };

//     // 수정 페이지 이동
//     const updateReq = () => {
//         navigate(`/board/update/${board.id}`);
//     };

//     // 삭제 요청
//     const deleteReq = async () => {
//         if(window.confirm("정말 삭제하시겠습니까?")){
//             try{
//                 // 1. 백엔드 API 호출 : Delete /api/board/delete/{id}
//                 // axios.delete 메서드 사용
            
//             await axios.delete(`http://localhost:8090/api/board/delete/${board.id}`);
            
//             // 2. 성공 시 알림 및 목록으로 이동
//             alert("게시글이 삭제되었습니다");
//             navigate(`/board/Paging`);
        
//         }catch(error){
//             console.error("삭제 실패: ", error);
//             alert("삭제 중 오류가 발생했습니다");
//         }
//     }
// };
//     // 로딩 처리
//     if (!board){
//         return <div>게시글 데이터를 불러오는 중입니다...</div>
//     }

//     return (
//         <div className="boardDetail">
//             <h2>게시글 상세</h2>

//             {/* 게시글 정보 */}
//             <section className="boardContent">
//                 <table>
//                     <tbody>
//                         <tr><th>게시글번호</th><td>{board.id}</td></tr>
//                         <tr><th>작성자</th><td>{board.boardWriter}</td></tr>
//                         <tr><th>제목</th><td>{board.boardTitle}</td></tr>
//                         <tr><th>작성일</th><td>{board.boardCreateTime}</td></tr>
//                         <tr><th>조회수</th><td>{board.boardHits}</td></tr>
//                         <tr><th>내용</th>
//                             <td
//                                 dangerouslySetInnerHTML={{ __html: board.boardContents }}
//                             ></td>
//                         </tr>
                    
//                         {/* 파일 첨부 */}
//                         {board.fileAttached === 1 && board.boardFileDtoList && board.boardFileDtoList.length > 0 && (
//                             <tr>
//                                 <th>업로드한 파일</th>
//                                 <td>
//                                     {board.boardFileDtoList.map((file, index) => (
//                                         <div key={index}>
//                                             <a 
//                                                 href={`http://localhost:8090/api/board/download/${board.id}/${index}`}
//                                                 target="_blank" rel="noopener noreferrer"
//                                             >
//                                                 {file.originalFilename}
//                                             </a>
//                                         </div>
//                                     ))}
//                                 </td>
//                             </tr>
//                         )}
//                     </tbody>
//                 </table>
//             </section>

//             <hr />

//             {/* 액션 버튼 */}
//             <section className="actionButtons">
//                 <button onClick={listReq}>목록</button>
//                 <button onClick={updateReq}>수정</button>
//                 <button onClick={deleteReq}>삭제</button>
//             </section>

//             <hr />

//             {/* 댓글 작성 섹션 */}
//             <section id="comment-write">
//                 <h3>댓글 작성</h3>
//                 <input 
//                     type="text" id="comment-writer" placeholder="작성자 이름"
//                     value={commentInput.writer} onChange={handleCommentInputChange}
//                     onKeyPress={handleEnterKey}
//                 />
//                 <input 
//                     type="text" id="comment-contents" placeholder="내용"
//                     value={commentInput.content} onChange={handleCommentInputChange}
//                     onKeyPress={handleEnterKey}
//                 />
//                 <button onClick={commentWrite}>댓글 작성</button>
//             </section>



//             <hr />

//             {/* 댓글 목록 섹션 */}
//             <section id="comment-list">
//                 <h3>댓글 목록</h3>
//                 {commentList.length > 0 ? (
//                     <table>
//                         <thead>
//                             <tr><th>댓글번호</th><th>작성자</th><th>내용</th><th>작성시간</th></tr>
//                         </thead>
//                         <tbody>
//                             {commentList.map((comment) => (
//                                 <tr key={comment.id}>
//                                     <td>{comment.id}</td>
//                                     <td>{comment.commentWriter}</td>
//                                     <td>{comment.commentContents}</td>
//                                     <td>{comment.commentCreatedTime}</td>
//                                 </tr>
//                             ))}
//                         </tbody>
//                     </table>
//                 ) : (
//                     <p>등록된 댓글이 없습니다.</p>
//                 )}
//             </section>
//         </div>
//     );
// }

// export default BoardDetail;



// // import React, { useEffect, useState } from 'react';
// // import { useParams, useNavigate } from 'react-router-dom';
// // import axios from 'axios';
// // import api from '../../api/axiosConfig';

// // import "../../css/boardDetail.css";


// // const BoardDetail = () => {
// //     const { id } = useParams(); // URL에서 게시글 ID (예: 5) 가져오기
// //     const navigate = useNavigate(); // 페이지 이동을 위한 함수
// //     const [board, setBoard] = useState(null); // 게시글 데이터(BoardDto)

// //     // 1. 게시글 데이터 가져오기
// //     useEffect(() => {
// //         const fetchBoard = async () => {
// //             try {
// //                 const response = await api.get(`/api/board/${id}`);
// //                 // 백엔드 응답 구조: { boardDto: {...}, commentDtoList: [...] }
// //                 setBoard(response.data.boardDto); 
// //             } catch (error) {
// //                 console.error("게시글 로드 실패", error);
// //                 alert("게시글을 불러올 수 없습니다.");
// //                 navigate('/api/board/paging');
// //             }
// //         };
// //         fetchBoard();
// //     }, [id, navigate]);

// //     // 2. 삭제 처리 함수
// //     const handleDelete = async () => {
// //         if (window.confirm("정말 삭제하시겠습니까?")) {
// //             try {
// //                 await api.delete(`/api/board/delete/${id}`);
// //                 alert("삭제되었습니다.");
// //                 navigate('/api/board/paging');
// //             } catch (error) {
// //                 console.error("삭제 실패", error);
// //                 alert("삭제 중 오류가 발생했습니다.");
// //             }
// //         }
// //     };

// //     // 3. 수정 페이지로 이동
// //     const handleUpdate = () => {
// //         // 수정 페이지도 WriteBoard를 재사용하므로, URL을 다르게 해서 보냅니다.
// //         // App.js 라우트 설정에서 /board/update/:id -> WriteBoard 연결 필요
// //         navigate(`/board/update/${id}`);
// //     };

// //     if (!board) return <div>로딩 중...</div>;

// //     return (
// //         <main>
// //             <div className="layoutCenter">
// //                 <div className="board-detail">
// //                     {/* 제목 영역 */}
// //                     <div className="detail-header" style={{borderBottom: '2px solid #333', padding: '20px 0'}}>
// //                         <h2>{board.boardTitle}</h2>
// //                         <div className="detail-info" style={{display: 'flex', justifyContent: 'space-between', marginTop: '10px', color: '#666'}}>
// //                             <span>작성자: {board.boardWriter}</span>
// //                             <span>조회수: {board.boardHits}</span>
// //                             <span>작성일: {board.boardCreatedTime}</span>
// //                         </div>
// //                     </div>

// //                     {/* 본문 영역 */}
// //                     <div className="detail-content" style={{minHeight: '300px', padding: '30px 0', borderBottom: '1px solid #ddd'}}>
// //                         {/* CKEditor로 저장된 내용은 HTML 태그가 포함되어 있을 수 있습니다.
// //                            단순 출력이 아니라 HTML을 해석해서 보여줘야 한다면 dangerouslySetInnerHTML 등을 사용해야 할 수도 있습니다.
// //                            일단은 텍스트로 출력합니다.
// //                         */}
// //                         <p>{board.boardContents}</p>
                        
// //                         {/* 첨부파일이 있다면 다운로드 링크 표시 */}
// //                         {/* 백엔드 DTO에 fileAttached=1 이라면 파일 목록을 보여주는 로직 추가 필요 */}
// //                     </div>

// //                     {/* 버튼 영역 */}
// //                     <div className="detail-btns" style={{marginTop: '20px', display: 'flex', gap: '10px', justifyContent: 'center'}}>
// //                         <button onClick={() => navigate('/api/board/paging')} style={{padding: '10px 20px', background: '#eee', border: 'none', cursor: 'pointer'}}>목록</button>
                        
// //                         {/* 본인 글일 때만 보여야 하는 버튼들 (일단은 다 보이게 구현) */}
// //                         <button onClick={handleUpdate} style={{padding: '10px 20px', background: '#4CAF50', color: '#fff', border: 'none', cursor: 'pointer'}}>수정</button>
// //                         <button onClick={handleDelete} style={{padding: '10px 20px', background: '#ff4d4d', color: '#fff', border: 'none', cursor: 'pointer'}}>삭제</button>
// //                     </div>
// //                 </div>
                
// //                 {/* 여기에 댓글 컴포넌트(Comment.jsx)를 추가하면 더 좋습니다 */}
// //             </div>
// //         </main>
// //     );
// // };

// // export default BoardDetail;