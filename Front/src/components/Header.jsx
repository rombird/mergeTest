import React, {createContext, useState, useEffect} from 'react'
import {Link, useNavigate} from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import "../css/common.css";


const Header = () => {
    const { isLoggedIn, logout, user } = useAuth(); 
    const navigate = useNavigate(); // useNavigate 훅 초기화

    console.log("유저정보: {}", user);
    // 로그아웃 처리 함수
    const handleLogout = async (e) => {
        // 폼의 기본 동작 방지 (Link 대신 button/onClick을 사용할 경우 불필요하지만 습관적으로 체크)
        e.preventDefault(); 
        await logout(); // AuthContext의 logout 함수 실행 (서버 측 로그아웃 처리)
        alert("로그아웃이 완료되었습니다."); // 알림창 표시
        navigate('/'); // 메인 페이지로 이동 (경로가 메인 페이지인 / 로 가정)
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
                                        <a className="logout" to="#void" >{displayUsername}님</a>
                                    </li>
                                    <li className="topNavli">
                                        <Link className="mypage" to="/mypage" ><img className="imgMypage" src="/images/login.svg" alt="마이페이지"/>마이페이지</Link>
                                    </li>
                                    <li className="topNavli">
                                        <a className="logout" to="/logout" onClick={handleLogout}><img className="imgLogout" src="/images/join.svg" alt="로그아웃"/>로그아웃</a>
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
                                        <Link className="mainMenu" to="/trend" >상권트렌드</Link>
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
                                <li className="mainList">
                                    <div className="chatList">
                                        <button className="chatbotBtn"><img className="chatbot" src="/images/chat.png" alt="chatbot이미지" /></button>
                                    </div>
                                </li>
                            </ul>
                        </div>
                    </div>
                </div>
            </header>
        </>
    )
}
export default Header;