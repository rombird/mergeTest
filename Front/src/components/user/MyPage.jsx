import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import axios from 'axios';

// API URL 정의
const API_USER_FETCH_URL = 'http://localhost:8090/user';           // 회원 정보 조회 (GET)
const API_PHONE_UPDATE_URL = 'http://localhost:8090/myInfo/phone';   // 휴대폰 번호 변경 (PUT)
const API_PASSWORD_UPDATE_URL = 'http://localhost:8090/myInfo/password';  // 비밀번호 변경 (PUT)
const API_USER_DELETE_URL = 'http://localhost:8090/user';          // 계정 삭제 (DELETE)

// 인증 정보 전달을 위한 설정 객체 (withCredentials: true 필수)
const AXIOS_CONFIG = {
    withCredentials: true
};

const MyPage = () => {
    const navigate = useNavigate();
    // 1. 서버에서 가져올 사용자 정보 상태
    const [userInfo, setUserInfo] = useState({
        name: '로딩 중...',
        username: '로딩 중',
        birthdate: '정보 없음',
        email: '', 
        phone: '' 
    });

    // 2. 연락처 변경을 위한 입력 필드 상태
    const [currentPhone, setCurrentPhone] = useState('');
    const [currentEmail, setCurrentEmail] = useState('');
    // 3. 비밀번호 변경을 위한 입력 필드 상태
    const [passwordInput, setPasswordInput] = useState({
        currentPassword: '',
        newPassword: '',
        confirmNewPassword: ''
    });

    // 4. 로딩 및 에러 상태
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState(null);

    // 사용자 정보 불러오기 (초기 로드)
    useEffect(() => {
        const fetchUserInfo = async () => {
            try {
                // GET 요청은 /myInfo 엔드포인트를 사용하며, withCredentials를 통해 쿠키를 자동 전송
                const response = await axios.get(API_USER_FETCH_URL, AXIOS_CONFIG); 
                const data = response.data; // 백엔드의 UserResponseDto 형태의 데이터
                
                // 1. userInfo 상태 설정
                setUserInfo({
                    name: data.name || '이름없음',
                    username: data.username || '아이디 없음', // 오타 수정: uaername -> username
                    birthdate: data.birthdate || '정보없음',
                    email: data.email || '',
                    phone: data.phone || ''
                });
                
                // 2. 수정 가능한 필드(phone)의 입력 필드 초기값 설정
                setCurrentPhone(data.phone || '');
                setCurrentEmail(data.email || '');
                
            } catch (error) {
                console.error("회원 정보 불러오기 실패", error);
                // 401 Unauthorized 에러일 가능성이 높음
                setError("회원정보를 불러오는데 실패했습니다. 로그인 상태를 확인해 주세요 (401 에러 발생 가능)");
            } finally {
                setIsLoading(false);
            }
        };
        fetchUserInfo();
    }, []);

    // 연락처 변경 핸들러 (PUT http://localhost:8090/myInfo/phone)
    const handleUpdateUserInfo = async () => {
        const updateData = {
            phone: currentPhone,
            email: currentEmail
        };
        
        try {
            // PUT 요청도 withCredentials: true를 사용
            // 이메일 업데이트 로직은 API 엔드포인트에 맞춰 제외하고 연락처만 업데이트
            await axios.put(API_PHONE_UPDATE_URL, updateData, AXIOS_CONFIG);

            // 성공 시 userInfo 상태 업데이트
            setUserInfo(prev => ({
                ...prev,
                phone: currentPhone,
                email: currentEmail 
            }));
            alert('회원 정보(연락처)가 성공적으로 변경되었습니다.');
            
            // 성공 시 메인 페이지로 이동
            navigate('/')
        } catch (error) {
            console.error("정보 수정 실패: ", error.response);
            const errorMessage = error.response?.data?.message || '연락처 수정에 실패했습니다. 서버 또는 인증 상태를 확인하세요.';
            alert(errorMessage);
        }
    };

    // 비밀번호 입력값 변경 핸들러
    const handlePasswordInputChange = (e) => {
        const { name, value } = e.target;
        setPasswordInput(prev => ({ ...prev, [name]: value }));
    };

    // 비밀번호 변경 핸들러 (PUT http://localhost:8090/myInfo/password)
    const handlePasswordChange = async () => {
        // 1. 프론트엔드에서 일차 검증
        if (passwordInput.newPassword !== passwordInput.confirmNewPassword) {
            alert('새 비밀번호와 확인 비밀번호가 일치하지 않습니다');
            return;
        }

        const passwordData = {
            currentPassword: passwordInput.currentPassword,
            newPassword: passwordInput.newPassword,
            // confirmNewPassword는 백엔드에서 검증할 필요는 없으나 DTO에 맞게 포함
        }

        try {
            // PUT 요청도 withCredentials: true를 사용
            await axios.put(API_PASSWORD_UPDATE_URL, passwordData, AXIOS_CONFIG);

            alert('비밀번호가 성공적으로 변경되었습니다');

            // 성공 후 입력 필드 초기화
            setPasswordInput({ currentPassword: '', newPassword: '', confirmNewPassword: '' });
        
            // 성공 시 메인페이지로 이동
            navigate('/');
        } catch (error) {
            console.error("비밀번호 변경 실패:", error.response);
            // 백엔드에서 제공하는 에러 메시지를 우선 사용
            const errorMessage = error.response?.data?.message || '비밀번호 변경에 실패했습니다 (현재 비밀번호 불일치 등)';
            alert(errorMessage);
        }

    };

    // 계정 삭제 핸들러
    const handleDeleteAccount = async () => {
            // 1.  사용자에게 계정 삭제 확인 및 아이디 재확인 
            if (!window.confirm("정말로 계정을 삭제하시겠습니까? 삭제된 정보는 복구할 수 없습니다.")) {
            return;
        }
        // 아이디 확인
        const confirmUsername = prompt(`계정 삭제를 확인하려면 아이디("${userInfo.username}")를 정확히 입력하세요.`);

        if(confirmUsername === null || confirmUsername !== userInfo.username){
            alert('아이디가 일치하지 않거나 취소되었습니다. 계정 삭제 취소합니다');
            return;
        }

        // 비밀번호 입력
        const password = prompt("계정 삭제를 위해 현재 비밀번호를 입력하세요.");

        if(!password){
            alert("비밀번호를 입력하지 않아 계정 삭제를 취소합니다");
        }

        const deleteData = {
            password: password
        }

        // API 호출
        try{
            await axios.delete(API_USER_DELETE_URL, {
                data: deleteData,
                ...AXIOS_CONFIG
            })
            
            // 삭제 성공 처리: 알림 후 홈으로 리다이렉트
            alert('회원계정 삭제되었습니다');
            navigate('/')
        }catch(error){
            console.log("계정 삭제 실패: ", error.response);
            const errorMessage = error.response?.data.message || '계정삭제에 실패';
            alert(errorMessage);
        }
    }

    // 로딩 및 오류 상태 처리
    if (isLoading) {
        return (
            <main >
                <div>회원 정보를 불러오는 중입니다...</div>
            </main>
        );
    }

    if (error) {
        return (
            <main className="min-h-screen flex items-center justify-center bg-gray-50">
                <div className="text-xl text-red-600 p-8 bg-white shadow-lg rounded-xl">{error}</div>
            </main>
        );
    }

    return (
        <>
            <main>
                <div className="my-page layoutCenter">
                    <div className="mypage-title">
                        <h1>MyPage</h1> 
                        <p> HOME &gt; 마이페이지 </p>
                    </div>
                    <div className="my-page-notice layoutCenter">
                        <div>
                            <p>⨂ 회원님의 정보를 관리하는 곳입니다.</p>
                            <p>수정이나 확인이 모두 끝나면 회원정보가 다른사람에게 알려지지않도록 반드시 창을 닫아주시기 바랍니다.</p>
                            <p>귀하의 정보는 동의없이 공개되지 않으며, 개인정보보호법에 의하여 보호되고 있습니다.</p>
                        </div>
                    </div>
                    <div className="table-title layoutCenter">
                        <h4>◾ 기본정보</h4>
                        <p className="starsign">⁎</p>
                        <p>필수 입력 항목입니다. </p>
                    </div>
                    <div className="dl-table layoutCenter">
                        <dl className="table">
                            <dt className="table-label">
                                <p>이름</p>
                            </dt>
                            <dd className="table-update">
                                <p>{userInfo.name}</p>
                            </dd>
                        </dl>
                        <dl className="table">
                            <dt className="table-label">
                                <p>아이디(ID)</p>
                            </dt>
                            <dd className="table-update">
                                <p>{userInfo.username}</p>
                            </dd>
                        </dl>
                        <dl className="table">
                            <dt className="table-label">
                                <p>비밀번호(PW)</p>
                            </dt>
                            <input
                                placeholder='현재 비밀번호'
                                type='password'
                                name='currentPassword'
                                value={passwordInput.currentPassword}
                                onChange={handlePasswordInputChange}
                            >
                            </input>
                            <input
                                placeholder='변경할 새 비밀번호'
                                type='password'
                                name='newPassword'
                                value={passwordInput.newPassword}
                                onChange={handlePasswordInputChange}
                            >
                            </input>
                            <input
                                placeholder='새 비밀번호 확인'
                                type='password'
                                name='confirmNewPassword'
                                value={passwordInput.confirmNewPassword}
                                onChange={handlePasswordInputChange}
                            >
                            </input>
                        </dl>
                        <dl className="table">
                            <dt className="table-label">
                                <p>생년월일</p>
                            </dt>
                            <dd className="table-update">
                                <p>{userInfo.birthdate}</p>
                            </dd>
                        </dl>
                        <dl className="table">
                            <dt className="table-label">
                                <p>연락처</p>
                            </dt>
                            <dd className="table-update phone-update">
                                <input
                                    placeholder='너의 연락처'
                                    name='phone'
                                    id='phone'
                                    value={currentPhone}
                                    onChange={(e) => setCurrentPhone(e.target.value)}
                                >
                                </input>
                            </dd>
                        </dl>
                        <dl className="table">
                            <dt className="table-label">
                                <input
                                    placeholder='너의 이메일'
                                    name='emall'
                                    id='email'
                                    value={currentEmail}
                                    onChange={(e) => setCurrentEmail(e.target.value)}
                                >
                                </input>
                            </dt>
                            <dd className="table-update">
                            <p>{userInfo.email}</p>
                            </dd>
                        </dl>
                    </div>
                    <div className="table-btn layoutCenter">
                        <button className="table-submit check" onClick={handleUpdateUserInfo}>확인</button>
                        <button className="table-submit cancel" onClick={() => window.location.reload()}>취소</button>
                    </div>
                    <button onClick={handleDeleteAccount}>계정 삭제</button>
                </div>
            </main>
        </>
    )

}

export default MyPage;