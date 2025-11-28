package com.example.demo.apiController;

import com.example.demo.domain.dto.BoardDto;
import com.example.demo.domain.dto.NoticeDto;
import com.example.demo.service.BoardService;
import com.example.demo.service.CommentService;
import com.example.demo.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Operation(summary = "Notice'sPagingList", description = "공지사항 목록 및 페이징 정보")
    @GetMapping("/save")
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

    // ################################################################
    // 게시판 목록 데이터 보내기
// ################################################################
//    @CrossOrigin(origins = {"http://localhost:3000", "http://192.168.5.7:3000"})
    @Operation(summary = "Notice'sPagingList", description = "공지사항 목록 및 페이징 정보")
    @GetMapping("/paging")
    public ResponseEntity<?> paging(
            @PageableDefault(page = 1, size = 10) Pageable pageable){     // @PageableDefault(page = 1) -> 기본적으로 1페이지 보여줄래
        log.info("GET  /api/board/paging... 페이징처리 apiBoardController");
        Page<NoticeDto> noticeList = noticeService.paging(pageable);

        int blockLimit = 10;
        // React에서 startPage, endPage 계산에 필요한 정보를 함께 JSON으로 반환
        int startPage = (((int)(Math.ceil((double)pageable.getPageNumber() / blockLimit))) -1) * blockLimit + 1; // 1, 4, 7,
        int endPage = ((startPage + blockLimit - 1) < noticeList.getTotalPages()) ? startPage + blockLimit - 1 : noticeList.getTotalPages();

        // Json 응답을 위한 Map 또는 별도의 DTO 사용

        Map<String, Object> response = new HashMap();

        response.put("noticeList" , noticeList);
        response.put("startPage", startPage);
        response.put("endPage", endPage);

        return ResponseEntity.ok(response); // JSON 형태로 데이터를 반환
    }


}
