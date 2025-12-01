import React, {useState,useEffect} from 'react'
import {Link, useNavigate} from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import SimpleChatbot from './SimpleChatbot';
import "../css/common.css";


const Header = () => {
    const { isLoggedIn, logout, user } = useAuth(); 
    const navigate = useNavigate(); // useNavigate 훅 초기화

    // #################################################
    // 챗봇 상태 추가
    // #################################################
    const [isChatbotVisible, setIsChatbotVisible] = useState(false);

    // 챗봇 열기/ 닫기 토글 함수
    const handleChatbotToggle = () => {
        setIsChatbotVisible(prev => !prev);
    }

    // 로그아웃 처리 함수
    const handleLogout = async (e) => {
        e.preventDefault(); // 폼의 기본 동작 방지 (Link 대신 button/onClick을 사용할 경우 불필요하지만 습관적으로 체크)
        await logout(); // AuthContext의 logout 함수 실행 (서버 측 로그아웃 처리)
        alert("로그아웃이 완료되었습니다.");  // 알림창 표시(실행흐름을 정지시키기때문에 나중에 처리) -> AuthContext 파일에서 logout 작업을 했기 때문에 OK
        window.location.replace('/'); // 메인 페이지로 이동
    };

    const displayUsername = user ? user.name : '';

    return(
        <>
            <header className="header">
                <div className="topHeader">
                    <div className="topList layoutCenter">
                        <ul className="topNav">
                            {isLoggedIn ? (
                                // 로그인 상태
                                <>  
                                    <li className="topNavli">
                                        <Link className="username-check" to="" >{displayUsername}님</Link>
                                    </li>
                                    <li className="topNavli">
                                        <Link className="mypage" to="/mypage" ><img className="imgMypage" src="/images/login.svg" alt="마이페이지"/>마이페이지</Link>
                                    </li>
                                    <li className="topNavli">
                                        <Link className="logout" to="/logout" onClick={handleLogout}><img className="imgLogout" src="/images/join.svg" alt="로그아웃"/>로그아웃</Link>
                                    </li>
                                </>
                            ) : ( 
                                // 로그아웃 상태
                                <>
                                    <li className="topNavli">
                                        <Link className="login" to="/login" ><img className="imgLogin" src="/images/login.svg" alt="로그인"/>로그인</Link>
                                    </li>
                                    <li className="topNavli">
                                        <Link className="join" to="/join" ><img className="imgJoin" src="/images/join.svg" alt="회원가입"/>회원가입</Link>
                                    </li>
                                </>
                            )}
                        </ul>
                    </div>
                </div>
                <div className="navHeader">
                    <div className="detailnav layoutCenter"> 
                        <Link className="logo" to="/" >JOB-A-YO</ Link>
                        <div className="wrap">
                            <ul className="mainNav">
                                <li className="mainList">
                                    <div className="listLine">
                                        <Link className="mainMenu" to="" >상권트렌드</Link>
                                    </div>
                                </li>
                                <li className="mainList">
                                    <div className="listLine">
                                        <Link className="mainMenu" to="/myshop" >나는 사장</Link>
                                    </div>
                                </li>
                                <li className="mainList">
                                    <div className="listLine">
                                        <Link className="mainMenu" to="/newshop" >나도 곧 사장</Link>
                                    </div>
                                </li>
                                <li className="mainList">
                                    <div className="listLine">
                                        <Link className="mainMenu" to="/guide" >이용안내</Link>
                                    </div>
                                    <ul className="subNav">
                                        <li className="subList">
                                            <Link to="api/notices" >공지사항</Link>
                                        </li>
                                        <li className="subList">
                                            <Link to="/inquiry" >문의사항</Link>
                                        </li>
                                        <li className="subList">
                                            <Link to="/api/board/paging" >Community</Link>
                                        </li>
                                    </ul>
                                </li>
                                {/* 챗봇 버튼에 클릭 이벤트 연결 */}
                                <li className="mainList">
                                    <div className="chatList">
                                        <button className="chatbotBtn" onClick={handleChatbotToggle} aria-label='챗봇 열기'><img className="chatbot" src="/images/chat.png" alt="chatbot이미지" /></button>
                                    </div>
                                </li>
                            </ul>
                        </div>
                    </div>
                </div>
            </header>
            
            {/* 챗봇 컴포넌트 렌더링 */}
            <SimpleChatbot
                isVisible={isChatbotVisible}
                onClose={() => setIsChatbotVisible(false)}  // 닫기버튼을 누르면 상태를 false로 설정
            />
        </>
    )
}
export default Header;