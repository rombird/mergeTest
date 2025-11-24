import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import React, {useEffect, useState, useCallback} from 'react';
import "../../context/AuthContext";
import "../../api/axiosConfig";
import moment from 'moment';

import "../../css/common.css";
import "../../css/noticePaging.css";
 
const NoticePaging = () => {
    const navigate = useNavigate(); 
    const [searchParams] = useSearchParams();

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

    const handleRegisterClick = () => {
        // 관리자만 접근 가능한 작성 페이지 경로
        navigate('/notice/write'); 
    };

    return(
        <>
            <div className="notice layoutCenter">
                <div className="notice-title layoutCenter">
                    <h1>공지사항</h1>
                    <p> HOME &gt; 이용안내 &gt; 공지사항 </p>
                </div>
                <div className="notice-list layoutCenter">
                    <form>
                        <input type="text" placeholder='제목, 내용 검색' />
                    </form>
                    <div className="notice-table">
                        <div className="notice-middle">
                            <table>
                                {/* <caption>번호, 제목, 작성자, 작성일자, 조회수의 내용으로 이루어진 표입니다.</caption> */}
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
                                    <tr>
                                        <td>번호1</td>
                                        <td>제목1</td>
                                        <td>관리자</td>
                                        <td>작성일자1</td>
                                        <td>조회수1</td>
                                    </tr>
                                    <tr>
                                        <td>번호2</td>
                                        <td>제목2</td>
                                        <td>관리자</td>
                                        <td>작성일자2</td>
                                        <td>조회수2</td>
                                    </tr>
                                    <tr>
                                        <td>번호3</td>
                                        <td>제목3</td>
                                        <td>관리자</td>
                                        <td>작성일자3</td>
                                        <td>조회수3</td>
                                    </tr>
                                    <tr>
                                        <td>번호4</td>
                                        <td>제목4</td>
                                        <td>관리자</td>
                                        <td>작성일자4</td>
                                        <td>조회수4</td>
                                    </tr>
                                    <tr>
                                        <td>번호5</td>
                                        <td>제목5</td>
                                        <td>관리자</td>
                                        <td>작성일자5</td>
                                        <td>조회수5</td>
                                    </tr>
                                    <tr>
                                        <td>번호6</td>
                                        <td>제목6</td>
                                        <td>관리자</td>
                                        <td>작성일자6</td>
                                        <td>조회수6</td>
                                    </tr>
                                    <tr>
                                        <td>번호7</td>
                                        <td>제목7</td>
                                        <td>관리자</td>
                                        <td>작성일자7</td>
                                        <td>조회수7</td>
                                    </tr>
                                    <tr>
                                        <td>번호8</td>
                                        <td>제목8</td>
                                        <td>관리자</td>
                                        <td>작성일자8</td>
                                        <td>조회수8</td>
                                    </tr>
                                    <tr>
                                        <td>번호9</td>
                                        <td>제목9</td>
                                        <td>관리자</td>
                                        <td>작성일자9</td>
                                        <td>조회수9</td>
                                    </tr>
                                    <tr>
                                        <td>번호10</td>
                                        <td>제목10</td>
                                        <td>관리자</td>
                                        <td>작성일자10</td>
                                        <td>조회수10</td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
                <div className="notice-bottom layoutCenter">
                    <div className="notice-paging"></div>
                    <div className="notice-btn">
                        <button className="notice-create">공지등록</button>
                    </div>
                </div>

                {/* 참고 */}
                {/* <div className="article-box">
                    {noticeList.length > 0 ? noticeList.map((notice) => (
                        <div key={notice.id}>
                            <Link to={`/api/notices/${notice.id}`}>
                                <p>[공지] {notice.noticeTitle}</p>
                                <p>작성일: {notice.noticeCreatedTime || 'YYYY-MM-DD'} | 조회수: {notice.noticeHits || 0}</p>
                            </Link>
                        </div>
                    )) : (
                        <div>등록된 공지사항이 없습니다.</div>
                    )}
                </div> */}
                {/* <div className="community-bottom layoutCenter">
                     2. 관리자일 때만 '등록' 버튼 표시 
                    {isAdmin && (
                        <div className="createbtn">
                            <button className="board-create" onClick={handleRegisterClick}>공지 등록</button>
                        </div>
                    )}
                </div> */}
            </div>

        </>
    )
}
export default NoticePaging;