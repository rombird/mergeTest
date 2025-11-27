package com.example.demo.apiController;

import com.example.demo.domain.dto.BoardDto;
import com.example.demo.domain.dto.NoticeDto;
import com.example.demo.service.BoardService;
import com.example.demo.service.CommentService;
import com.example.demo.service.NoticeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/notice")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "apiNoticeController", description = "공지사항 api 컨트롤러")
public class apiNoticeController {

    private final NoticeService noticeService;


    // 파일 저장 경로
    @Value("${file.dir}")       // 파일 저장 경로
    private String fileDir;

    @Value("${CKEditor.image}")
    private String CKEditorImageDir;

    public ResponseEntity<NoticeDto> save(// 1. 폼 데이터 (제목, 글쓴이, 내용 등)를 DTO에 바인딩
                                          @ModelAttribute NoticeDto noticeDto,
                                          // 2. 파일 데이터를 "fileUpload" 키로 명시적으로 받음
                                          @RequestPart(value = "noticeFileUpload", required = false) List<MultipartFile> noticeFileUploads) throws IOException {

        // 수신한 파일을 DTO의 필드에 수동으로 설정
        // DTO에 List<MultipartFile> fileUpload; 필드가 있으므로 사용 가능
        if (noticeFileUploads != null && !noticeFileUploads.isEmpty()) {
            noticeDto.setNoticeFileUpload(noticeFileUploads);
        }

        // 4. 서비스 호출 (Service 계층에서는 boardDto.getFileUpload()로 파일 접근)
        NoticeDto savedBoard = noticeService.save(noticeDto);

        // 201 Created 응답과 함께 저장된 게시글 객체를 반환
        return new ResponseEntity<>(savedBoard, HttpStatus.CREATED);

    }
}
