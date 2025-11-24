import {Link, useNavigate, useSearchParams} from 'react-router-dom';
// import { useAuth } from '../../context/AuthContext';
import React, { useEffect, useState, useCallback } from 'react';
import moment from 'moment'; // moment 추가 (날짜 포맷용)
import axios from 'axios';

import "../../css/paging.css"
import "../../css/common.css"


const Paging = () => {
    const API_BASE_URL = 'http://localhost:8090/api/board/paging';
    // const { isLoggedIn, logout } = useAuth(); 
    
    const navigate = useNavigate(); // useNavigate 를 사용하여 이동함수 가져옴
    const [searchParams] = useSearchParams(); // url에서 'page' 쿼리 파라미터를 가져와 현재 페이지를 결정
    const page = searchParams.get('page') || '1';
    const pageSize = 5; // 한 페이지당 게시글 수

    // State 선언 (두 번째 코드 블록의 State)
    const [boardData, setBoardData] = useState({
        content: [],
        number: 0, // 현재 페이지 인덱스 (0부터 시작)
        totalPages: 1,
        first: true,
        last: true
    });
    const [loading, setLoading] = useState(true);
    const [startPage, setStartPage] = useState(1);
    const [endPage, setEndPage] = useState(1);

    // 데이터 로딩 함수 (두 번째 코드 블록의 함수)
    const fetchBoardList = useCallback(async () => {
        setLoading(true);
        try {
            // 서버에 요청할 때 page는 스프링의 Pageable에 맞게 1부터 시작하는 page를 전달 (예시 코드에 따라)
            // page=1 로 요청 시 서버에서 0번 페이지를 가져올 수도 있습니다.
            // 서버 설정에 따라 page=${page - 1} 또는 page=${page}를 사용해야 합니다.
            // 현재는 URL의 page를 그대로 사용하겠습니다.
            const response = await axios.get(`${API_BASE_URL}?page=${page}&size=${pageSize}`);
            const data = response.data;
            
            setBoardData(data.boardList);
            setStartPage(data.startPage);
            setEndPage(data.endPage);

        } catch (error) {
            console.error("게시글 목록을 불러오는데 실패했습니다.", error);
            setBoardData({ content: [], number: 0, totalPages: 1, first: true, last: true }); 
            setStartPage(1);
            setEndPage(1);
        } finally {
            setLoading(false);
        }
    }, [page]); // page가 변경될 때마다 다시 호출

    useEffect(() => {
        fetchBoardList();
    }, [fetchBoardList]);

    // 현재 페이지 번호 (사용자에게 1부터 보여주는 번호)
    // Spring Data JPA의 Page 객체에서 number는 0부터 시작하므로 +1 해줍니다.
    const currentDisplayPage = boardData.number + 1;

    // 페이지 링크 생성 헬퍼 함수
    const getPageLink = (pageNum) => `/board/paging?page=${pageNum}`;

    // 로딩 중일 때 표시
    // if (loading) {
    //     return <div className="loading-state">Loading...</div>;
    // }

    // 버튼 클릭 시 실행할 함수
    const handleRegisterClick = () => {
        // 새 글 등록 페이지의 경로로 이동합니다.
        navigate('/board/writeBoard'); 
    };

    const articleList = boardData.content.length > 0 ? (
        boardData.content.map((board) => (
            <div className="article" key={board.id}>
                <div className="article-main">
                    {/* 게시글 제목을 클릭하면 상세 페이지로 이동 */}
                    <Link to={`/board/${board.id}?page=${currentDisplayPage}`}>
                        {/* 이 부분은 실제 데이터를 보여주는 항목으로 교체합니다. */}
                        {/* {board.boardTitle} 이 부분이 기존 <p>게시글 제목</p>을 대체 */}
                        <p className="article-title">{board.boardTitle}</p>
                        <p className="article-writer">글쓴이: {board.boardWriter}</p>
                        <p className="article-date">날짜: {moment(board.boardCreateTime).format('YYYY-MM-DD HH:mm:ss')}</p>
                    </Link>
                </div>
                <div className="article-response">
                    <div className="read">
                        <img className="response-img" src="../images/read.png" alt="조회" />
                        <span>{board.boardHits}</span> {/* 실제 조회수 데이터 사용 */}
                    </div>
                    {/* 댓글, 좋아요 데이터는 API에 따라 board 객체에 포함되어 있다면 사용 가능 */}
                    {/* <div className="comment">...</div> */}
                    {/* <div className="heart">...</div> */}
                </div>
            </div>
        ))
    ) : (
        <div className="no-articles">등록된 게시글이 없습니다.</div>
    );

    // 시작 페이지부터 끝 페이지까지 배열 생성
    const pageNumbers = Array.from(
        { length: endPage - startPage + 1 },
        (_, i) => startPage + i
    );

    return(
        <>
           <div className="community">
            <div className="community-title layoutCenter">
                <h1>전체 게시글</h1>
                <p> HOME &gt; 이용안내 &gt; Community </p>
            </div>
            <div className="community-box layoutCenter">
                <div>
                    <button className="community-filter">최신글</button>
                    <button className="community-filter">인기글</button>
                    <button className="community-filter">댓글 많은 글</button>
                    <input type="text" placeholder='제목, 내용, 작성자, 태그 검색' />
                </div>
                <div className="article-box">
                    {articleList}
                </div>
                <div className="community-bottom layoutCenter">
                    <div className="paging">
                        <div className="paging-container">
                            {/* << 처음으로 */}
                            <Link to={getPageLink(1)} className="paging-first">&lt;&lt;</Link>
                            
                            {/* < 이전 */}
                            <Link
                                to={boardData.first ? '#' : getPageLink(currentDisplayPage - 1)}
                                className={`paging-prev paging-second ${boardData.first ? 'disabled-link' : ''}`}
                            >
                                &lt;
                            </Link>

                            {/* 페이지 번호 목록 랜더링 및 비활성화 처리 */}
                            <span className="page-numbers">
                                {pageNumbers.map(pageNum => {
                                    const isCurrent = pageNum === currentDisplayPage;
                                    const isDisabled = pageNum > boardData.totalPages;
                                    return(
                                        <React.Fragment key={pageNum}>
                                            {isCurrent ? (
                                                <span className="current-page">{pageNum}</span>
                                            ) : isDisabled ? (
                                                <span className="disabled-page-number">{pageNum}</span>
                                            ) : (
                                                <Link to={getPageLink(pageNum)}>{pageNum}</Link>
                                            )
                                            }
                                        </React.Fragment>
                                    );
                                })}
                            </span>
                            
                            {/* > 다음 */}
                            <Link
                                to={boardData.last ? '#' : getPageLink(currentDisplayPage + 1)}
                                className={`paging-next paging-second ${boardData.last ? 'disabled-link' : ''}`}
                            >
                            &gt;
                            </Link>
                            
                            {/* >> 마지막으로 */}
                            <Link to={getPageLink(boardData.totalPages)} className="paging-last">&gt;&gt;</Link>
                        </div>
                    </div>   

                    <div className="createbtn">
                    <button className="board-create" onClick={handleRegisterClick}>등록</button>
                    </div>
                </div>
                
            </div>
           </div>
        
        
        </>
    )
}

export default Paging;












// import axios from "axios";
// import { useNavigate, useSearchParams, Link } from 'react-router-dom';
// import moment from 'moment';
// import { useEffect, useState, useCallback } from "react";

// import "../../css/common.css";


// // 수정: 모든 로직을 Paging 함수 컴포넌트 내부에 정의합니다.
// const Paging = () => {
    
//     const API_BASE_URL = 'http://localhost:8090/api/board/paging';
    
//     //  Hook들은 여기서 선언
//     const navigate = useNavigate();
//     const [searchParams] = useSearchParams();

//     // 현재 페이지 번호
//     const page = searchParams.get('page') || '1';
//     const pageSize = 11;
//     //  클라이언트 측에서 -1을 해서 서버(0부터 시작)로 보냄
//     // const pageIndex = parseInt(page, 10) - 1; 

//     // State 선언
//     const[boardData, setBoardData] = useState({
//         content: [],
//         number: 0,
//         totalPages: 1,
//         first: true,
//         last: true
//     });
//     const[loading, setLoading] = useState(true);
//     const[startPage, setStartPage] = useState(1);
//     const[endPage, setEndPage] = useState(1);

//     // 데이터 로딩 함수
//     const fetchBoardList = useCallback(async () => {
//         setLoading(true);
//         try{
//             // 요청 시 pageIndex 사용 (0부터 시작)
//             const response = await axios.get(`${API_BASE_URL}?page=${page}&size=${pageSize}`);
//             const data = response.data;
            
//             // 백엔드 JSON 응답 키(boardList)에 맞게 업데이트
//             setBoardData(data.boardList);
//             setStartPage(data.startPage);
//             setEndPage(data.endPage);

//         }catch(error){
//             console.error("게시글 목록을 불러오는데 실패했습니다.", error);
//             setBoardData({content : [], number : 0, totalPages: 1, first: true, last: true }); 
//             setStartPage(1);
//             setEndPage(1);
//         } finally{
//             setLoading(false);
//         }
//     },[page]); // pageIndex가 변경될 때마다 다시 호출

//     useEffect(() => {
//         fetchBoardList();
//     }, [fetchBoardList]); 

//     // 현재 페이지 번호 (사용자에게 1부터 보여주는 번호)
//     const currentDisplayPage = boardData.number + 1;

//     // '글 작성' 버튼 클릭 핸들러
//     const saveReq = () => {
//         navigate('/board/WriteBoard');
//     };

//     // 로딩 중일 때 표시
//     if(loading){
//         return <div className="loading-state">Loading...</div>
//     }

//     // ... (나머지 JSX 코드는 아래에 포함)

//     // ############ 게시글 목록 테이블 JSX ###################
//     const boardTable = (
//         <table className="board-table">
//             <thead>
//                 <tr>
//                     <th>게시글.No</th>
//                     <th>제목</th>
//                     <th>글쓴이</th>
//                     <th>날짜</th>
//                     <th>조회수</th>
//                 </tr>
//             </thead>
//             <tbody>
//                 {boardData.content.length > 0 ? (
//                     boardData.content.map((board) => (
//                         <tr key={board.id}>
//                             <td>{board.id}</td>
//                             <td>
//                                 <Link to={`/board/${board.id}?page=${currentDisplayPage}`}>
//                                     {board.boardTitle}
//                                 </Link>
//                             </td>
//                             <td>{board.boardWriter}</td>
//                             <td>
//                                 {moment(board.boardCreateTime).format('YYYY-MM-DD HH:mm:ss')} {/* 💡 moment 포맷 YYYY 수정 */}
//                             </td>
//                             <td>{board.boardHits}</td>
//                         </tr>
//                     ))
//                 ) : (
//                     <tr>
//                         <td colSpan="5" className="no-data">등록된 게시글이 없습니다</td>
//                     </tr>
//                 )}
//             </tbody>
//         </table>
//     );
    
//     // #################### 페이징 링크 및 생성 JSX #########################

//     const getPageLink = (pageNum) => `/board/paging?page=${pageNum}`;

//     const pageNumbers = Array.from(
//         {length: endPage - startPage + 1},
//         (_, i) => startPage + i
//     );

//     const pagingLinks = (
//         <div className="paging-container">
//             <Link to = {getPageLink(1)}>처음</Link>
//             <Link
//                 to = {boardData.first ? '#' : getPageLink(currentDisplayPage -1)}
//                 className={boardData.first ? 'disabled-link' : ''}>
//                 이전
//             </Link>

//             <span className="page-numbers">
//                 {pageNumbers.map(pageNum => (
//                     <span key={pageNum} className="page-number-item">
//                         {pageNum === currentDisplayPage ? (
//                             <span className="current-page">{pageNum}</span>
//                         ) : (
//                             <Link to={getPageLink(pageNum)}>{pageNum}</Link>
//                         )}
//                     </span>
//                 ))}
//             </span>
            
//             <Link
//                 to={boardData.last ? '#': getPageLink(currentDisplayPage + 1)}
//                 className={boardData.last ? 'disabled-link': ''}>
//                 다음
//             </Link>
//             <Link to={getPageLink(boardData.totalPages)}>마지막</Link>
//         </div>
//     );

//     return (
//         <div className="board-list-container">
//             <button onClick={saveReq}>글 작성</button>
//             {boardTable}
//             {pagingLinks}
//         </div>
//     );
// }

// export default Paging;

