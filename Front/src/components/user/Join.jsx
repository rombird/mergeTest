import {useState} from 'react'
import axios from 'axios'
import { useNavigate } from 'react-router-dom';

import "../../css/common.css";
import "../../css/join.css";

const Join  = ()=>{
    const navigate = useNavigate();
    const [username ,setUsername] = useState()
    const [password ,setPassword] = useState()
    const [name, setName] = useState()
    const [phone, setPhone] = useState()
    // 이메일
    const [emailPrefix, setEmailPrefix] = useState(''); 
    const [emailDomain, setEmailDomain] = useState('naver.com'); // 초기값 설정

    const handleJoin = (e)=>{
        e.preventDefault();
        const fullEmail = `${emailPrefix}@${emailDomain}`;

        axios
            .post(
                'http://localhost:8090/join',
                 {"username":username, "password" : password, "name":name, "phone":phone, "email": fullEmail,},
                 {headers:{ 'Content-Type' : 'application/json' }}
            )
            .then(resp=>{
                console.log(resp) // 요청을 해서 정상적인 응답이 오면 console에 반응
                // 응답이 성공(status 200)일 경우 후처리 로직 추가
                if (resp.data === "success" && resp.status === 200) {
                    alert("회원가입이 성공적으로 완료되었습니다!"); // 사용자 알림
                    navigate('/login');  // 3. 로그인 페이지로 이동 (예시: /login 경로로)
                }
            })
            .catch(err=>{console.log(err)})
    }
    return (
        <>
            <div className="custom">
                <div className="box layoutCenter">
                    <div className="title">
                        <h1>회원가입</h1>
                    </div>
                    <ul className="step">
                        <li className="on">
                            <div className="num"><em className="done">1</em></div>
                            <p>약관동의</p>
                        </li>
                        <li className="on">
                            <div className="num"><em className="active">2</em></div>
                            <p>회원정보</p>
                        </li>
                    </ul>
                    <div className="provide-points">
                        <b>회원가입 관련 정보입력시, 필수항목은 반드시 작성하셔야 회원가입이 가능합니다.</b>
                        <b>SMS, 이메일 및 카카오톡 수신 동의 여부 '아니오' 선택할 시, 맞춤형 지원사업 안내 및 지원사업 신청 상태 알림을 받을 수 없습니다.</b>
                    </div>
                    <form className="join-form" onSubmit={handleJoin}>
                        <div className="input-group first">
                            <label htmlFor="id">아이디</label>
                            <input type="text" name="username" id="id" required onChange={e=>setUsername(e.target.value)} />
                        </div>
                        <div className="input-group">
                            <label htmlFor="pw" name="password">비밀번호</label>
                            <input type="password" name = "password" id="pw" required onChange={e => setPassword(e.target.value)}/>
                        </div>
                        <p className="guide"> 영문 대소문자, 숫자, 특수문자를 혼합하여 8~15자 이내로 입력해주세요</p>
                        
                        <div className ="input-group second">
                            <label htmlFor="rePw">비밀번호 확인</label>
                            <input type="password" name="rePassword" id="rePw" required />
                        </div>
                        <div className ="input-group second">
                            <label htmlFor="name">이름</label>
                            <input type="text" name="name" id="name" required onChange={e => setName(e.target.value)}/>
                        </div>
                        <div className ="input-group second birth">
                            <label>생년월일</label>
                            <div className="select-div">
                                <select name="birthYear" id="birthYear" required defaultValue="2000">
                                    <option value="2000">2000년</option>
                                    <option value="1999">1999년</option>
                                    <option value="1998">1998년</option>
                                    <option value="1997">1997년</option>
                                    <option value="1996">1996년</option>
                                    <option value="1995">1995년</option>
                                    <option value="1994">1994년</option>
                                    <option value="1993">1993년</option>
                                    <option value="1992">1992년</option>
                                    <option value="1991">1991년</option>
                                    <option value="1990">1990년</option>
                                    <option value="1989">1989년</option>
                                    <option value="1988">1988년</option>
                                    <option value="1987">1987년</option>
                                    <option value="1986">1986년</option>
                                    <option value="1985">1985년</option>
                                    <option value="1984">1984년</option>
                                    <option value="1983">1983년</option>
                                </select>
                                <select name="birthMonth" id="birthMonth" required defaultValue="01">
                                    <option value="01">01월</option>
                                    <option value="02">02월</option>
                                    <option value="03">03월</option>
                                    <option value="04">04월</option>
                                    <option value="05">05월</option>
                                    <option value="06">06월</option>
                                    <option value="07">07월</option>
                                    <option value="08">08월</option>
                                    <option value="09">09월</option>
                                    <option value="10">10월</option>
                                    <option value="11">11월</option>
                                    <option value="12">12월</option>
                                </select>
                                <select name="birthDay" id="birthDay" required defaultValue="01">
                                    <option value="01">01일</option>
                                    <option value="02">02일</option>
                                    <option value="03">03일</option>
                                    <option value="04">04일</option>
                                    <option value="05">05일</option>
                                    <option value="06">06일</option>
                                    <option value="07">07일</option>
                                    <option value="08">08일</option>
                                    <option value="09">09일</option>
                                    <option value="10">10일</option>
                                    <option value="11">11일</option>
                                    <option value="12">12일</option>
                                    <option value="13">13일</option>
                                    <option value="14">14일</option>
                                    <option value="15">15일</option>
                                    <option value="16">16일</option>
                                    <option value="17">17일</option>
                                    <option value="18">18일</option>
                                    <option value="19">19일</option>
                                    <option value="20">20일</option>
                                    <option value="21">21일</option>
                                    <option value="22">22일</option>
                                    <option value="23">23일</option>
                                    <option value="24">24일</option>
                                    <option value="25">25일</option>
                                    <option value="26">26일</option>
                                    <option value="27">27일</option>
                                    <option value="28">28일</option>
                                    <option value="29">29일</option>
                                    <option value="30">30일</option>
                                    <option value="31">31일</option>
                                </select>
                            </div>
                        </div>
                        <div className ="input-group gender second">
                            <label>성별</label>
                            <div className="gender-check">
                                <label htmlFor="man">
                                    <input id="man" type="radio" name="gender"  value = "man" /> 
                                    남자
                                </label>
                                <label htmlFor="woman">
                                    <input id="woman"  type="radio" name="gender" value = "woman" />
                                    여자
                                </label>
                            </div>
                        </div>
                        <div className ="input-group phone second">
                            <label>연락처</label>
                            <input type="text" id="phone" name = "phone" placeholder="(- 없이 숫자만 입력)" required onChange={e => setPhone(e.target.value)}/>
                        </div>
                        <div className ="input-group email second">
                            <label>이메일</label>
                            <input type="text" name="email" onChange={e => setEmailPrefix(e.target.value)} value={emailPrefix}/>
                            <p>@</p>
                            <select onChange={e => setEmailDomain(e.target.value)} value={emailDomain} >
                                <option>naver.com</option>
                                <option>gmail.com</option>
                                <option>daum.net</option>
                                <option>nate.com</option>
                            </select>    
                        </div>
                        <div className ="input-group keyword-dl">
                            <label>관심 요식업 아이템</label>
                            <div className="keyword-checkbox">
                                <label htmlFor="item1">
                                    <input type="checkbox" name="keyword" value="item1" id = "item1" />
                                    휴게음식점
                                </label>
                                <label htmlFor="item2">
                                    <input type="checkbox" name="keyword" value="item2" id = "item2" />
                                    일반음식점
                                </label>
                                <label htmlFor="item3">
                                    <input type="checkbox" name="keyword" value="item3" id="item3" />
                                    단란주점
                                </label>
                                <label htmlFor="item4">
                                    <input type="checkbox" name="keyword" value="item4" id="item4" />
                                    유흥주점
                                </label>
                                <label htmlFor="item5">
                                    <input type="checkbox" name="keyword" value="item5" id="item5" />
                                    제과점영업
                                </label>
                            </div>
                        </div>
                        <p className = "guide-check">식품위생법에 따라 분류했으며 1개 이상 선택가능합니다. </p> 
                                
                        <div className ="input-group recieve-dl">
                            <label>수신여부(선택)</label>
                            <div className = "recieve-checkbox">
                                <label htmlFor= "recieveSms">
                                    <input type="checkbox" name="recieve" value="recieveSms" id="recieveSms" />
                                    SMS 수신 
                                </label>
                                <label htmlFor="recieveEmail">
                                    <input type="checkbox" name="recieve" value="recieveEmail" id="recieveEmail" />
                                    이메일 수신
                                </label>
                            </div>
                        </div>
                        <p className="guide-recieve">수신 여부는 마이페이지에서 변경하실 수 있습니다.</p>
                        
                        <div className="bottom-border"></div>
                        <div className = "cancel-join">
                            <button>취소</button>
                            <button id = "join_btn" type="submit">회원가입</button>
                        </div>
                    </form>
                </div>
            </div>
        </>
    )
}

export default Join