
const NoticeWrite = () => {
    return(
        <>
            <div className="write layoutCenter">
                <div className="sub-title">
                    <div className="inquiry">
                        <h3>공지사항 글쓰기</h3>
                        <img src="../images/writing.png" alt="게시글 작성" />
                    </div>
                    <div className="path">
                        <div>이용안내 &gt; Community </div>
                    </div>
                </div>

                <div className="write-space">
                    <form onSubmit={handleSubmit} encType="multipart/form-data" >
                        <div className="label-and-writeArea">
                            <div className="label-area">
                                <div>제목</div>
                            </div>
                            <div className="write-area">
                                <input className="write-input" type="text" name="boardTitle" placeholder="제목"/>
                            </div>
                        </div>

                        <div className="line-dotted"></div>

                        <div className="label-and-writeArea">
                            <div className="label-area">
                                <div>첨부파일</div>
                                <div><i style={{color: '#3A6B71'}} class="fa-solid fa-star-of-life fa-2xs"></i></div>
                            </div>
                            <div className="write-area">
                                <div className="file-upload-info">
                                    <div className="file-upload-info-left">
                                        <div>용량 제한 : 6.0MB, 객수 제한 : 5개</div>
                                        <div>파일 형식 : xlsx, pptx, txt, pdf, jpg, jpeg, png, hwp</div>
                                    </div>
                                    <input className="file-upload-btn" type="file" id="fileUpload" name="fileUpload" multiple/>
                                    <label className="upload-btn" for="fileUpload">
                                        <img src="../images/upload.png" alt="파일업로드" />
                                    </label>
                                </div>
                                <div className="upload" id="upload">
                                    <p>파일을 드래그하여 첨부할 수 있습니다</p>
                                </div>
                            </div>
                        </div>    

                        <div className="line-dotted"></div>

                        <div className="label-and-writeArea">
                            <div className="label-area content-label">
                                <div>내용</div>
                            </div>
                            <div className="write-area content-area">
                                <div className="main-container">
                                    <div className="editor-container editor-container_classic-editor" id="editor-container">
                                        <div className="editor-container__editor">
                                            <textarea className="editor" name="boardContents" id="editor" placeholder="나의 소식을 공유해보세요!" ></textarea>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div className="under-line-dotted line-dotted"></div>
                    </form>
                    
                    <div className="submit-box layoutCenter">
                        <button className="submit-btn" type="submit" >등록</button>
                    </div>
                </div>
            </div>
        </>
    )
}
export default NoticeWrite;