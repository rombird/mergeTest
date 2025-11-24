import React from 'react';
import {Link} from 'react-router-dom';
import "../../css/mypage.css";
import "../../css/common.css";

const MyPage = () => {

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
                                <p>이름 넣는 작업</p>
                            </dd>
                        </dl>
                        <dl className="table">
                            <dt className="table-label">
                                <p>아이디(ID)</p>
                            </dt>
                            <dd className="table-update">
                                <p>해당유저id</p>
                            </dd>
                        </dl>
                        <dl className="table">
                            <dt className="table-label">
                                <p>비밀번호(PW)</p>
                            </dt>
                            <dd className="table-update">
                                <button className="btn-name" type="button" >비밀번호 변경</button>
                            </dd>
                        </dl>
                        <dl className="table">
                            <dt className="table-label">
                                <p>생년월일</p>
                            </dt>
                            <dd className="table-update">
                                <p>생년월일 확인만 되도록</p>
                            </dd>
                        </dl>
                        <dl className="table">
                            <dt className="table-label">
                                <p>연락처</p>
                            </dt>
                            <dd className="table-update phone-update">
                                <p>해당유저연락처</p>
                                <div>
                                    <button className="btn-name" type="button" >연락처 변경</button>
                                </div>
                            </dd>
                        </dl>
                        <dl className="table">
                            <dt className="table-label">
                                <p>이메일주소</p>
                            </dt>
                            <dd className="table-update">
                                <p>이메일주소</p>
                            </dd>
                        </dl>
                    </div>
                    <div className="table-btn layoutCenter">
                        <button className="table-submit check">확인</button>
                        <button className="table-submit cancel">취소</button>
                    </div>
                </div>
            </main>
        </>
    )

}

export default MyPage;