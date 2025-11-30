import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import React, {useEffect, useState, useCallback} from 'react';
import "../../context/AuthContext";
import api from "../../api/axiosConfig";
import moment from 'moment';

import "../../css/common.css";
import "../../css/noticePaging.css";
 
const NoticePaging = () => {
    const navigate = useNavigate(); 
    const [searchParams, setSearchParams] = useSearchParams();
    const [userInfo, setUserInfo] = useState(null);
    const[isAdmin, setIsAdmin] = useState(false);

    // 데이터 및 페이징 정보 상태
    const [noticeData, setNoticeData] = useState({
        noticeList: {content: [], totalPages: 0, number: 0, first: true, last: true},
        startPage: 1,
        endPage: 1
    });

    // URL에서 page = X 값을 가져옴
    const currentPage = parseInt(searchParams.get('page') || '1', 10);   
    const searchQuery = searchParams.get('search') || '';

    // 사용자 정보(ADMIN 권한) 로딩
    useEffect(() => {
        const fetchUser = async () => {
            try{
                const response = await api.get('/user');
                setUserInfo(response.data);

                // role이 ADMIN인지 체크
                if(response.data.role === 'ADMIN'){
                    setIsAdmin(true);
                }
            }catch(error){
                console.error("회원정보 조회 실패", error);
            }
        };
        fetchUser();
    }, []);

    // 공지사항 목록 API 호출 함수
    const fetchNotices = useCallback(async (page, search) => {
        try{
            // GET 요청으로 변경하고, url 쿼리 파라미터로 page와 size를 전달
            const response = await api.get('/api/notice/paging', {
                params: {
                    page: page,
                    searchKeyword: search,
                    size: 10
                }
            });

            // 응답 구조에 맞게 상태 저장
            setNoticeData(response.data);
        }catch(error){
            console.error("게시글 목록 로드 실패", error);
            alert("게시글 목록을 불러오는데 실패했습니다");
        }
    }, []);

    // URL 파라미터 (currnetPage) 변경 시 목록 다시 불러오기
    useEffect(() => {
        fetchNotices(currentPage, searchQuery);
    }, [currentPage, searchQuery, fetchNotices]);

    // 페이지 번호 클릭 핸들러
    const handlePageChange = (newPage) => {
        // newPage는 1부터 시작하는 페이지 번호
        setSearchParams({page: newPage});
    };

    // 검색 폼 제출 핸들러(백엔드에서 만들고 싶다)
    const handleSearchSubmit = (e) => {
        e.preventDefault();
        const newSearchQuery = e.target.elements.searchKeyword.value;
        // 검색어와 함께 페이지를 1로 초기화하여 URL 업데이트
        setSearchParams({page: 1, search: newSearchQuery});
    };

    const goToWrite = () => {
        navigate('/notice/noticeWrite');
    };

    // 구조 분해하여 데이터 사용 용이하게
    const content = noticeData.noticeList.content;
    const {startPage, endPage} = noticeData;
    const totalPages = noticeData.noticeList.totalPages;

    // 페이지네이션 버튼 렌더링을 위한 배열 생성(startPage ~ endPage)
    // const getPageNumbers = () =>{
    //     const pages = [];
    //     for (let i = startPage; i <= endPage; i++){
    //         pages.push(i - 1);  // 배열에는 0부터 시작하는 index를 저장
    //     }
    //     return pages;
    // };

    // 1. 관리자 권한 확인 (AuthContext에서 사용자 역할(role)을 가져와야 합니다.)
    // const { userRole } = useAuth();
    // const isAdmin = userRole === 'ADMIN'; 
    // useEffect(() => {
    //     // [백엔드 API 호출] GET /api/notices
    //     const fetchNotices = async () => {
    //         try {
    //             // 백엔드 NoticesRestController의 1번 메서드(getAllNotices)와 연결
    //             const response = await api.get('/api/notices'); 
    //             setNoticeList(response.data); // 응답 데이터가 List<NoticesDto> 라고 가정
    //         } catch (error) {
    //             console.error("공지사항 목록 로드 실패", error);
    //             // navigate('/'); // 실패 시 홈으로 리다이렉트
    //         }
    //     };
    //     fetchNotices();
    // }, []);

    // const handleRegisterClick = () => {
    //     // 관리자만 접근 가능한 작성 페이지 경로
    //     navigate('/notice/write'); 
    // };

                

    return(
        <>
            <div className='notice layoutCenter'>
                <div className="notice-title layoutCenter">
                    <h1>공지사항</h1>
                    <p> HOME &gt; 이용안내 &gt; 공지사항 </p>
                </div>
                <div className="notice-list layoutCenter">
                    <form>
                        <input 
                            type='text'
                            placeholder='제목, 내용 검색'
                            name='searchKeyword'
                            defaultValue={searchQuery}
                        />
                        <button type='submit' className='search-btn'>검색</button>
                    </form>
                    <div className='notice-table'>
                        <div className='notice-middle'>
                            <table>
                                <thead>
                                    <tr>
                                        <th>번호</th>
                                        <th>제목</th>
                                        <th>작성자</th>
                                        <th>작성일자</th>
                                        <th>조회수</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {/* 동적 목록 렌더링 */}
                                    {content.length > 0 ? (
                                        content.map((notice) => (
                                            <tr key={notice.id}>
                                                <td>{notice.id}</td>
                                                <td>
                                                    <Link to={`/notice/${notice.id}`} className='notice-link'>
                                                        {notice.noticeTitle}
                                                    </Link>
                                                </td>
                                                <td>{notice.noticeWriter}</td>
                                                <td>{moment(notice.noticeCreateTime).format('YYYY-MM-DD')}</td>
                                                <td>{notice.noticeHits}</td>
                                            </tr>
                                        ))
                                    ):(
                                        <tr>
                                            <td colSpan="5">등록된 공지사항이 없습니다.</td>
                                        </tr>
                                    )}
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
            <div className="notice-bottom layoutCenter">
                {/* 페이징 영역 */}
                <div className='notice-paging'>
                    {/* 이전 페이지 그룹으로 이동(startPage가 1보다 클 때) */}
                    {startPage > 1 &&(
                        // startPage - 10으로 이동 (예: 11페이지에서 << 누르면 1페이지)
                        <button className='page-move' onClick={() => handlePageChange(startPage -1)}>
                            &lt;&lt; 
                        </button>
                    )}
                    {/* 이전 페이지로 이동(currentPage가 1보다 클 떼) */}
                    {currentPage > 1 &&(
                        <button className="page-move" onClick={() => handlePageChange(currentPage - 1)}>
                            &lt;
                        </button>
                    )}

                    {/* 페이지 번호(StartPage부터 endPage까지) */}
                    {/* Array.from으로 startPage부터 endPage까지 배열 생성 */}
                    {Array.from({ length: endPage - startPage + 1 }, (_, i) => startPage + i).map(page => (
                        <button
                            key={page}
                            className={`page-number ${page === currentPage ? 'active': ''}`}
                            onClick={() => handlePageChange(page)}
                        >
                            {page}
                        </button>
                    ))}

                    {/* 다음 페이지로 이동 (currentPage가 totalPage보다 작을 때) */}
                    {currentPage < totalPages && (
                        <button className='page-move' onClick={() => handlePageChange(currentPage + 1)}>
                            &gt;
                        </button>    
                    )}
                    {/* 다음 페이지 그룹으로 이동 (endPage가 totalPages보다 작을 때) */}
                    {endPage < totalPages &&(
                        <button className='page-move' onClick={() => handlePageChange(endPage + 1)}>
                            &gt;&gt;
                        </button>
                    )}
                </div>
                {/* 공지 등록 버튼(Admin만 표시) */}
                <div className='notice-btn'>
                    {isAdmin && (
                        <button onClick={goToWrite} className='notice-create'>공지등록</button>
                    )}
                </div>
            </div>
        </>
    );
}
export default NoticePaging;