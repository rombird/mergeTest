// AuthContext.js
import React, { createContext, useContext, useState, useEffect, useCallback, useSyncExternalStore  } from 'react';
import api from '../api/axiosConfig';
import axios from 'axios'; 

const AuthContext = createContext(null);

export const useAuth = () => useContext(AuthContext);

// 전역적인 인증상태 관리하는 파일
export const AuthProvider = ({ children }) => {
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const [user, setUser] = useState(null);
    const [isLoading, setIsLoading] = useState(true); // 로딩상태

    // 로그아웃
    const logout = async () => {
        try {
            await api.post('/logout'); // 서버에서 로그아웃 요청
            setIsLoggedIn(false); // 인증상태를 false
            setUser(null); // 사용자 정보 초기화
        } catch (error) {
            console.error("로그아웃 실패:", error);
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
    useEffect(() => {
        const checkAuthStatus = async () => {
            try {
                await axios.get('http://localhost:8090/validate', { // axios를 사용하면 로그인 안된 상태(401)이라도 로그인 페이지로 튕기지 X
                    withCredentials:true
                });  // 쿠키 기반 토큰 자동 전달
                
                // 추가한 코드) 유효성 검사 성공 시, 사용자 정보 조회 및 저장
                const userInfo = await fetchUserInfo();

                if(userInfo){
                    setIsLoggedIn(true);
                }else{
                    setIsLoggedIn(false);
                }
                
            } catch (error) {
                setIsLoggedIn(false);
                setUser(null); // 추가한 코드
            } finally {
                setIsLoading(false);
            }
        };
        checkAuthStatus();
    }, []);

    // 로그인 성공 시
    const login = async () => {
        try{
            // 추가한 코드) 수정: 로그인 성공 시, 사용자 정보를 가져와 상태에 저장
            const userInfo = await fetchUserInfo();
            if(userInfo){
                setIsLoggedIn(true);
            }else{
                setIsLoggedIn(false);
            }
        }catch(error){
            console.error("로그인 후 사용자 정보 로드 실패 : ", error);
            setIsLoggedIn(false);
        }
        
    };

    
    return (
        <AuthContext.Provider value={{ isLoggedIn, user, login, logout, isLoading }}>
            {children}
        </AuthContext.Provider>
    );
};
