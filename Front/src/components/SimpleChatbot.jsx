
import React, {useState, useRef, useEffect, useCallback} from 'react';
import "../css/simpleChatbot.css";


// 심플챗봇  주소
const SIMPLE_AI_URL = 'http://localhost:8090/api/v1/simple-chat';

// ###########################################
// 컴포넌트 정의 및 상태 관리
// ###########################################
const SimpleChatbot = ({isVisible, onClose}) => {

    // 대화 메시지 목록 상태
    const [message, setMessage] = useState([
        // 메시지 목록 상태({role : 'user' | 'bot', content: '...', isLoading: true나 false})
        {role: 'bot', content: "안녕하세요! 심플한 AI 챗봇입니다. 무엇이든 물어보세요"}
    ]);
    const[inputMessage, setInputMessage] = useState('');    // 입력받는 메시지, 초기화를 위해 useState는 빈열로
    const[isLoading, setIsLoading] = useState(false);
 
    // Dom 참조(스크롤 제어용)
    const chatBodyRef = useRef(null);

    // #################################################
    // 메시지 전송 로직
    // #################################################
    const sendSimpleMessage = useCallback(async (textTosend) => {
        const text = textTosend.trim();
        if(!text || isLoading) return;

        // 사용자 메시지 추가 및 입력창 비우기
        const userMessage = {role: 'user', content: text};
        // 상태 업데이트 시 함수형 업데이트를 사용하여 이전 상태 기반으로 안전하게 처리
        setMessage(prev => [...prev, userMessage]);
        setInputMessage('');

        // 로딩 시작
        setIsLoading(true);
        const loadingMessage ={role : 'bot', content: '처리 중...', isLoading: true};
        setMessage(prev => [...prev, loadingMessage]);
    
        try{
            const response = await fetch(SIMPLE_AI_URL, {
                method: 'post',
                headers: {'Content-Type' : 'application/json'},
                body: JSON.stringify({message: text})
            });
        
            if(!response.ok){
                // 에러 처리
                throw new Error(`HTTP error! Status: ${response.status}`);
            }
            const data = await response.json();
            const reply = data.reply || '응답 내용이 없습니다.';

            // 3. 응답 메시지로 대체: 로딩 메시지를 제거하고 실제 응답 추가
            setMessage(prev => {
                const newMessage = prev.filter(msg => !msg.isLoading);
                return [...newMessage, {role: 'bot', content: reply}];
            });
        
        } catch(error){
            console.error('챗봇 API 호출 중 오류: ', error);
            const errorMessage = "죄송합니다. 현재 응답을 생성할 수 없습니다.";

            // 에러 메시지로 대체
            setMessage(prev => {
                const newMessage = prev.filter(msg => !msg.isLoading);
                return [...newMessage, {role: 'bot', content: errorMessage, error: true}];
            });
        
        } finally{
            // 로딩 상태 해제
            setIsLoading(false);
        }
    }, [isLoading]);

    // Enter 키 핸들러
    const handleKeypress = (e) => {
        if(e.key === 'Enter' && !e.shiftKey){
            e.preventDefault();
            sendSimpleMessage(inputMessage);
        }
    };

    // ##############################################
    // 스크롤 자동 이동 효과
    // ##############################################
    useEffect(() => {
        if(chatBodyRef.current){
            chatBodyRef.current.scrollTop = chatBodyRef.current.scrollHeight;
        }
    }, [message, isVisible]);

    // ###############################################
    // 랜더링
    // ###############################################
    return(
        <>
            {/* // chat-panel과 visible클래스를 isVisible prop에 따라 조건부 적용 */}
             <div className={`chat-panel ${isVisible ? 'visible' : ''}`}             >
                <div className='chat-header' style = {{backgroundColor: '#ec4899'}}>
                    <div className='chat-header-title'>Simple Chatbot</div>
                    <button className='chat-close' onClick={onClose} aria-label='대화창 닫기'>&times;</button>
                </div>
             
             {/* 메시지 본문 영여기 Ref 연결 */}
                <div className='chat-body' ref={chatBodyRef}>
                    {message.map((msg, index) => (
                        // role에 따라 user/bot 클래스 적용
                        <div key={index} className={`chat-message ${msg.role}`}>
                        {/* 봇 메시지일 때만 뱃지 표시 */}
                        {msg.role === 'bot' && <span className='bot-badge'>AI 상담</span>}
                        {msg.isLoading ? (
                            // 로딩 중일 때 로딩 애니메이션 표시
                            <div className="loading-dots">처리 중...</div> 
                        ) : (
                            // 최종 메시지 내용
                            msg.content 
                        )}
                        </div>
                    ))}
                </div>

                {/* 입력 영역 */}
                <div className='chat-input-area'>
                    <input className='chat-input' 
                    type='text' 
                    placeholder='메시지를 입력하세요' 
                    value={inputMessage} 
                    onChange={(e) => setInputMessage(e.target.value)}
                    onKeyPress={handleKeypress}
                    disabled={isLoading}
                    />
                    <button
                        className='chat-send'
                        type='button'
                        onClick={() => sendSimpleMessage(inputMessage)}
                        // 로딩 중이거나 입력값이 비어있으면 전송버튼 비활성화
                        disabled={isLoading || !inputMessage.trim()}
                    >
                        {isLoading ? '전송 중' : '전송'}
                    </button>
                </div>
            </div>
        </>
    );
};

export default SimpleChatbot;