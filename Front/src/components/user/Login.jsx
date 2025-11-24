import axios from "axios";
import { Link, useNavigate } from 'react-router-dom';
import {useState,useEffect} from 'react'
import api from '../../api/axiosConfig'; // 새로운 api 인스턴스 임포트
import {useAuth} from '../../context/AuthContext';
import "../../css/common.css";
import "../../css/login.css";


const Login = ()=>{
    const [username,setUsername] = useState()
    const [password,setPassword] = useState()
    const navigate = useNavigate();
    const {login} = useAuth(); // login 함수 사용


  // useEffect에서 API 검증 호출 - AuthContext가 수행
    useEffect(() => {
        const validateToken = async () => {
        try {
            // 토큰 유효성 검증을 위한 별도 엔드포인트 호출
            const resp = await axios.get("http://localhost:8090/validate", {
            withCredentials: true, 
            //★ 파라미터 옵션으로 꼭 넣어줘야 토큰전달가능★
            //쿠키형태의 토큰을 전달하는 옵션
            });
            console.log("토큰 검증 성공:", resp);
            navigate("/"); // 성공 시 / 경로로 이동
        } catch (error) {
            console.log("토큰 검증 실패:", error);
            // 비정상 응답 시 아무 동작도 하지 않음 (현재 페이지 유지)
        }
        };
        validateToken();
    }, [navigate]); // navigate를 의존성 배열에 추가

 
    // 로그인 처리 함수
    const handleLogin = async (e) => {
        e.preventDefault(); // 폼 제출의 기본동작을 막음
        try {
            const resp = await api.post(
                "/login",
                { username, password },
                { headers: { "Content-Type": "application/json" } }
            );
            // alert("로그인 성공:", resp.data);
            console.log("로그인 성공 : ", resp.data)
            login();
            navigate("/"); // 성공 시 / 경로로 이동
        } catch (error) {
            if(error.response && error.response.status === 401){
                // 인증 안된 상태 -> 정상
                return;
            }
            console.error("로그인 실패:", error.response ? error.response.data : error);
            alert("로그인 실패! 다시 시도해주세요."); // 실패 시 메시지 표시
        }
    };

    const handleKakaoLogin = () => {
        window.location.href = 'http://localhost:8090/oauth2/authorization/kakao';
    };
    
    return (
        <>
            <div className="sign-in layoutCenter">
                <div className="sign-box">
                    <div className="original-sign">
                        <h2>로그인</h2>
                        <div className="under-line"></div>
                        <div className="login">
                            <form method="post" id="login-form" className="form">
                                <div className="login-group">
                                    <label><img className="user" src="/images/user.png" alt="아이디" /> 아이디</label>
                                    <input type="text" name="username" onChange={e=>setUsername(e.target.value)} />
                                </div>
                                <div className="login-group">
                                    <label><img className="security" src="/images/security.png" alt="비밀번호" />비밀번호</label>
                                    <input type="password" name="password" onChange={e=>setPassword(e.target.value)} />
                                </div>
                                <button className="submit-block" onClick={handleLogin}>로그인</button>
                                <div className="tx_btns">
                                    <Link className="login-a-tag" to="">회원가입</Link>
                                    <div>
                                        <Link className="login-a-tag" to="">아이디 찾기</Link>
                                        <Link className="login-a-tag" to="">비밀번호 찾기</Link>
                                    </div>
                            </div> 
                            </form>
                        </div>  
                    </div>
                    <div className="social">
                        <p>SNS 계정으로 간편하게 로그인하세요</p>
                        <Link to="/oauth2/authorization/google" ><img src="/images/google_icon.png" alt="구글 로그인" /></Link>
                        <Link to="/oauth2/authorization/naver" ><img src="/images/naver_icon.png" alt="네이버 로그인" /></Link>
                        <button onClick={handleKakaoLogin} className="kakaoButton"><img src="/images/kakao_icon.png" alt="카카오 로그인" /></button>
                    </div>  
                </div>
            </div>
        </>

    )

}

export default Login;


