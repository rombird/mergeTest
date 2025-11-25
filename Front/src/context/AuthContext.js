// AuthContext.js
import React, { createContext, useContext, useState, useEffect, useCallback, useSyncExternalStore } from 'react';
import api from '../api/axiosConfig';
import axios from 'axios'; 

const AuthContext = createContext(null);

export const useAuth = () => useContext(AuthContext);

// 전역적인 인증상태 관리하는 파일
export const AuthProvider = ({ children }) => {
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const [user, setUser] = useState(null);
    const [isLoading, setIsLoading] = useState(true); // 로딩상태

    const logout = async () => {
        try{
            await api.post('/logout');
        }catch(error){
            console.error("로그아웃 실패: ", error);
        }finally{
            // 서버 요청 성공/실패와 관계없이 클라이언트 상태는 로그아웃으로 변경
        }
    };
    
    const fetchUserInfo = useCallback(async () => {
        try{
            const response = await axios.get('http://localhost:8090/user', {
                withCredentials: true
            });
            setUser(response.data);
            return response.data;
        }catch(error){
            console.error("사용자 정보 조회 실패: ", error);
            // 사용자 정보 조회 실패 시에는 로그아웃 상태로 간주
            logout();
            return null
        }
    }, [setUser, logout])   

    // 자동 로그인 체크
    // 사용자가 이전에 로그인 한 적 있는지 자동으로 체크하는 useEffect
    useEffect(() => {
        const checkAuthStatus = async () => {
            try {   // 로그인 성공하면 로그인 상태 TRUE
                await axios.get('http://localhost:8090/validate', { // axios를 사용하면 로그인 안된 상태(401)이라도 로그인 페이지로 튕기지 X
                    withCredentials:true    // 이 코드덕분에 브라우저에서 갖고 있는 인증 쿠기가 요청에 자동으로 포함되어 서버로 전송됌
                });  // 쿠키 기반 토큰 자동 전달

                // 유효성 검사 성공 시, 사용자 정보 조회 및 저장
                const userInfo = await fetchUserInfo();

                if(userInfo){
                    setIsLoading(true);
                }else{
                    // 유효성은 통과했지만 /user Api가 실패하면 로그인 실패 처리
                    setIsLoggedIn(false);
                }
            } catch (error) {
                // 로그인 실패하면 로그인 상태 False
                setIsLoggedIn(false);
                setUser(null);
            } finally {
                setIsLoading(false);
            }
        };
        checkAuthStatus();
    }, []);        
        // 로그인 성공 시
    const login = async () =>{
        try{
            // 수정: 로그인 성공 시, 사용자 정보를 가져와 상태에 저장
            const userInfo = await fetchUserInfo();

            if(userInfo){
                setIsLoggedIn(true);
            }else{
                // 정보 조회에 실패했다면 로그인 상태는 false
                setIsLoggedIn(false);
            }
        }catch(error){
            console.error("로그인 후 사용자 정보 로드 실패: ", error);
            setIsLoggedIn(false);
        }
    };

   

    return (    // AuthContext.Provider -> 이 컴포넌트가 감싸고 있는 모든 하위 컴포넌트들에게(children) value 객체를 제공한다
        <AuthContext.Provider value={{ isLoggedIn, user, login, logout, isLoading }}>
            {children}
        </AuthContext.Provider>
    );
};
