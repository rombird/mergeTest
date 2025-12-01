import './App.css';
import React from 'react'; // React import 추가
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';

import Home from "./components/Home";
import Login from "./components/user/Login";
import Logout from "./components/user/Logout";
import MyPage from "./components/user/MyPage";
import Join from "./components/user/Join";
import Paging from "./components/board/Paging";
import WriteBoard from "./components/board/WriteBoard";
import BoardDetail from "./components/board/BoardDetail";
import Header from "./components/Header";
import Footer from "./components/Footer";
import NoticePaging from './components/notice/NoticePaging';
import NoticeWrite from './components/notice/NoticeWrite';
import NoticeDetail from './components/notice/NoticeDetail';

function App() {
  return (
    <div className="App">
      <Router>
        <AuthProvider>
          <div>
            <Header /> 
            {/* <AppRoutes /> */}
              <Routes> 
                <Route path="/" element={<Home />} />
                <Route path="/login" element={<Login />} />
                <Route path="/logout" element={<Logout />} />
                <Route path="/mypage" element={<MyPage />} />
                <Route path="/join" element={<Join />} />
                <Route path="/api/board/paging" element={<Paging />} />
                <Route path="/board/WriteBoard" element={<WriteBoard />} />
                <Route path="/board/:id" element={<BoardDetail />} />
                {/* 글쓰기/수정 페이지 (수정 모드에 :id 사용) */}
                <Route path="/board/update/:id" element={<WriteBoard />} />

                {/* 공지사항 */}
                <Route path="/api/notices" element={<NoticePaging />} />
                <Route path="/notice/noticeWrite" element={<NoticeWrite />} />
                <Route path="/notice/:id" element={<NoticeDetail />} />
                <Route path="/notice/update/:id" element={<NoticeWrite />} />

                {/* <Route path="/predict" element={}/> */}

              </Routes>
            
            <Footer />
          </div>
        </AuthProvider>
      </Router>
    </div>
  );
}

export default App;
