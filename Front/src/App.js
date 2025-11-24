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

// 라우트들을 관리하는 컴포넌트를 따로 생성
// const AppRoutes = () => {
//     // AuthContext에서 로딩 상태(isLoading)와 로그인 상태(isLoggedIn) 가져옴
//     const { isLoading, isLoggedIn } = useAuth();

//     if (isLoading) {
//         return <div style={{height: '100vh', display: 'flex', justifyContent: 'center', alignItems: 'center'}}>로그인 정보 확인 중...</div>;
//     }

//     return (
//         <main>
//             <Routes> 
//                 <Route path="/" element={<Home />} />
//                 <Route path="/login" element={<Login />} />
//                 <Route path="/join" element={<Join />} />
//                 <Route path="/logout" element={<Logout />} />
//                 <Route path="/api/board/paging" element={<Paging />} />

//                 {/* --- 로그인해야 들어갈 수 있는 페이지 (보호된 라우트) --- */}
//                 {/* 만약 로그인이 안되어있으면 로그인 페이지로 보냄 */}
//                 <Route 
//                     path="/mypage" 
//                     element={isLoggedIn ? <MyPage /> : <Navigate to="/login" replace />} 
//                 />
//             </Routes>
//         </main>
//     );
// }

function App() {
  return (
    <div className="App">
      <Router>
        <AuthProvider>
          <div>
            <Header /> 
            {/* <AppRoutes /> */}
              {/* <Home /> */}
            
              <Routes> 
                <Route path="/" element={<Home />} />
                <Route path="/login" element={<Login />} />
                 {/* <Route path="/oauth" element={<Redirect />} />  */}
                <Route path="/logout" element={<Logout />} />
                <Route path="/mypage" element={<MyPage />} />
                <Route path="/join" element={<Join />} />
                <Route path="/api/board/paging" element={<Paging />} />
                <Route path="/board/WriteBoard" element={<WriteBoard />} />
                {/* <Route path="/api/board/:id" element={<BoardDetail />} /> */}
                {/* 글쓰기/수정 페이지 (수정 모드에 :id 사용) */}
                <Route path="/board/writeBoard" element={<WriteBoard />} />
                <Route path="/board/update/:id" element={<WriteBoard />} />

                {/* 공지사항 */}
                <Route path="/api/notices" element={<NoticePaging />} />
              </Routes>
            
            <Footer />
          </div>
        </AuthProvider>
      </Router>
    </div>
  );
}

export default App;
