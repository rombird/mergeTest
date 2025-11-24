// AuthContext.js
import React, { createContext, useContext, useState, useEffect } from 'react';
import api from '../api/axiosConfig';
import axios from 'axios'; 

const AuthContext = createContext(null);

export const useAuth = () => useContext(AuthContext);

// 전역적인 인증상태 관리하는 파일
export const AuthProvider = ({ children }) => {
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const [isLoading, setIsLoading] = useState(true); // 로딩상태

    // 자동 로그인 체크
    // 사용자가 이전에 로그인 한 적 있는지 자동으로 체크하는 useEffect
    useEffect(() => {
        const checkAuthStatus = async () => {
            try {   // 로그인 성공하면 로그인 상태 TRUE
                await axios.get('http://localhost:8090/validate', { // axios를 사용하면 로그인 안된 상태(401)이라도 로그인 페이지로 튕기지 X
                    withCredentials:true    // 이 코드덕분에 브라우저에서 갖고 있는 인증 쿠기가 요청에 자동으로 포함되어 서버로 전송됌
                });  // 쿠키 기반 토큰 자동 전달
                setIsLoggedIn(true);
            } catch (error) {
                // 로그인 실패하면 로그인 상태 False
                setIsLoggedIn(false);
            } finally {
                setIsLoading(false);
            }
        };
        checkAuthStatus();
    }, []);

    // 로그인 성공 시
    const login = () => {
        setIsLoggedIn(true);
    };

    // 로그아웃
    const logout = async () => {
        try {
            await api.post('/logout'); // 만약 에러가 나면 axios로 바꿔야 함
            setIsLoggedIn(false);
        } catch (error) {
            console.error("로그아웃 실패:", error);
        }
    };

    return (    // AuthContext.Provider -> 이 컴포넌트가 감싸고 있는 모든 하위 컴포넌트들에게(children) value 객체를 제공한다
        <AuthContext.Provider value={{ isLoggedIn, login, logout, isLoading }}>
            {children}
        </AuthContext.Provider>
    );
};
