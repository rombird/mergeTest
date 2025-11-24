import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import api from '../../api/axiosConfig';


<<<<<<< HEAD
import "../../css/boardDetail.css";
=======
import "../../css/boardDetail.css"
>>>>>>> dev

const BoardDetail = () => {
    const { id } = useParams(); // URL에서 게시글 ID (예: 5) 가져오기
    const navigate = useNavigate(); // 페이지 이동을 위한 함수
    const [board, setBoard] = useState(null); // 게시글 데이터(BoardDto)

    // 1. 게시글 데이터 가져오기
    useEffect(() => {
        const fetchBoard = async () => {
            try {
                const response = await api.get(`/api/board/${id}`);
                // 백엔드 응답 구조: { boardDto: {...}, commentDtoList: [...] }
                setBoard(response.data.boardDto); 
            } catch (error) {
                console.error("게시글 로드 실패", error);
                alert("게시글을 불러올 수 없습니다.");
                navigate('/api/board/paging');
            }
        };
        fetchBoard();
    }, [id, navigate]);

    // 2. 삭제 처리 함수
    const handleDelete = async () => {
        if (window.confirm("정말 삭제하시겠습니까?")) {
            try {
                await api.delete(`/api/board/delete/${id}`);
                alert("삭제되었습니다.");
                navigate('/api/board/paging');
            } catch (error) {
                console.error("삭제 실패", error);
                alert("삭제 중 오류가 발생했습니다.");
            }
        }
    };

    // 3. 수정 페이지로 이동
    const handleUpdate = () => {
        // 수정 페이지도 WriteBoard를 재사용하므로, URL을 다르게 해서 보냅니다.
        // App.js 라우트 설정에서 /board/update/:id -> WriteBoard 연결 필요
        navigate(`/board/update/${id}`);
    };

    if (!board) return <div>로딩 중...</div>;

    return (
        <main>
            <div className="layoutCenter">
                <div className="board-detail">
                    {/* 제목 영역 */}
                    <div className="detail-header" style={{borderBottom: '2px solid #333', padding: '20px 0'}}>
                        <h2>{board.boardTitle}</h2>
                        <div className="detail-info" style={{display: 'flex', justifyContent: 'space-between', marginTop: '10px', color: '#666'}}>
                            <span>작성자: {board.boardWriter}</span>
                            <span>조회수: {board.boardHits}</span>
                            <span>작성일: {board.boardCreatedTime}</span>
                        </div>
                    </div>

                    {/* 본문 영역 */}
                    <div className="detail-content" style={{minHeight: '300px', padding: '30px 0', borderBottom: '1px solid #ddd'}}>
                        {/* CKEditor로 저장된 내용은 HTML 태그가 포함되어 있을 수 있습니다.
                           단순 출력이 아니라 HTML을 해석해서 보여줘야 한다면 dangerouslySetInnerHTML 등을 사용해야 할 수도 있습니다.
                           일단은 텍스트로 출력합니다.
                        */}
                        <p>{board.boardContents}</p>
                        
                        {/* 첨부파일이 있다면 다운로드 링크 표시 */}
                        {/* 백엔드 DTO에 fileAttached=1 이라면 파일 목록을 보여주는 로직 추가 필요 */}
                    </div>

                    {/* 버튼 영역 */}
                    <div className="detail-btns" style={{marginTop: '20px', display: 'flex', gap: '10px', justifyContent: 'center'}}>
                        <button onClick={() => navigate('/api/board/paging')} style={{padding: '10px 20px', background: '#eee', border: 'none', cursor: 'pointer'}}>목록</button>
                        
                        {/* 본인 글일 때만 보여야 하는 버튼들 (일단은 다 보이게 구현) */}
                        <button onClick={handleUpdate} style={{padding: '10px 20px', background: '#4CAF50', color: '#fff', border: 'none', cursor: 'pointer'}}>수정</button>
                        <button onClick={handleDelete} style={{padding: '10px 20px', background: '#ff4d4d', color: '#fff', border: 'none', cursor: 'pointer'}}>삭제</button>
                    </div>
                </div>
                
                {/* 여기에 댓글 컴포넌트(Comment.jsx)를 추가하면 더 좋습니다 */}
            </div>
        </main>
    );
};

export default BoardDetail;