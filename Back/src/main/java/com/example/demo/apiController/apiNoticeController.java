package com.example.demo.apiController;

import com.example.demo.domain.dto.*;
import com.example.demo.service.CommentService;
import com.example.demo.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notice")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "apiNoticeController", description = "공지사항 api 컨트롤러")
public class apiNoticeController {

    private final NoticeService noticeService;


    // 파일 저장 경로
    @Value("${noticeFile.dir}")       // 파일 저장 경로
    private String noticeFileDir;

    @Value("${noticeCKEditor.image}")
    private String noticeCKEditorImageDir;

    @Operation(summary = "Notice'sPagingList", description = "공지사항 목록 및 페이징 정보")
    @PostMapping("/save")
    public ResponseEntity<NoticeDto> save(// 1. 폼 데이터 (제목, 글쓴이, 내용 등)를 DTO에 바인딩
                                          @ModelAttribute NoticeDto noticeDto,
                                          // 2. 파일 데이터를 "fileUpload" 키로 명시적으로 받음
                                          @RequestPart(value = "uploadFiles", required = false) List<MultipartFile> noticeFileUploads) throws IOException {
        log.info("post/ api/notice/save , 공지사항 저장");
        // 수신한 파일을 DTO의 필드에 수동으로 설정
        // DTO에 List<MultipartFile> fileUpload; 필드가 있으므로 사용 가능
        if (noticeFileUploads != null && !noticeFileUploads.isEmpty()) {
            noticeDto.setNoticeFileUpload(noticeFileUploads);
        }

        // 4. 서비스 호출 (Service 계층에서는 noticeDto.getFileUpload()로 파일 접근)
        NoticeDto savedNotice = noticeService.save(noticeDto);

        // 201 Created 응답과 함께 저장된 게시글 객체를 반환
        return new ResponseEntity<>(savedNotice, HttpStatus.CREATED);

    }

    // ################################################################
    // 게시판 목록 데이터 보내기
// ################################################################
//    @CrossOrigin(origins = {"http://localhost:3000", "http://192.168.5.7:3000"})
    @Operation(summary = "Notice'sPagingList", description = "공지사항 목록 및 페이징 정보")
    @GetMapping("/paging")
    public ResponseEntity<?> paging(
            @PageableDefault(page = 1, size = 10) Pageable pageable){     // @PageableDefault(page = 1) -> 기본적으로 1페이지 보여줄래
        log.info("GET  /api/notice/paging... 페이징처리 apiNoticeController");
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

    // ################################################################
    // 게시글 조회
    // ################################################################
    @Operation(summary = "noticeDetail", description = "공지사항 단건 조회")
    @GetMapping("/{id}")
    public ResponseEntity<NoticeDetailResponse> findById(@PathVariable Long id) {
        log.info("GET /api/notice/{id}... 공지사항 단건 조회 apinoticeController");
        System.out.println("id:" + id);
        // 해당 게시글의 조회수를 하나 늘리고
        noticeService.updateHits(id);

        // 게시글 데이터를 가져와서 detail.html에 출력
        NoticeDto noticeDto = noticeService.findById(id);

        // 댓글 목록 조회
//        List<CommentDto>commentDtoList = commentService.findAll(id);

        // 응답 Dto에 데이터 통합
        NoticeDetailResponse response = new NoticeDetailResponse(noticeDto);

        System.out.println("response:" + response + "...apiNoticeController의 findById");

        // HTTP 200 ok 상태코드와 함께 Json데이터를 반환
        return ResponseEntity.ok(response);
    }

    // ################################################################
    // 첨부 파일 다운로드
    // ################################################################

    @Operation(summary = "NoticeFileDownload", description = "공지사항 첨부파일 다운로드")
    @GetMapping("/download/{noticeId}/{fileIndex}")
    public ResponseEntity<Resource> fileDownload(@PathVariable Long noticeId,
                                                 @PathVariable int fileIndex){
        log.info("get /api/notice/download/{noticeId}/{fileIndex}... 첨부파일 다운로드, apiNoticeController");

        try{
            // 1. 서비스에서 해당 파일 정보(Dto) 가져오기
            NoticeFileDto noticeFileDto = noticeService.fileDownloadByIndex(noticeId, fileIndex);

            String originalFilename = noticeFileDto.getOriginalFilename();
            String storedFilename = noticeFileDto.getStoredFilename();

            System.out.println("오리지널파일이름, 저장파일이름: " + originalFilename +  ", " + storedFilename);
            // 2. 파일 경로 생성
            if(originalFilename == null || storedFilename == null){
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "DB에 파일 정보를 찾을 수 없습니다");
            }

            Path filePath = Paths.get(noticeFileDir, storedFilename);
            Resource resource = new UrlResource(filePath.toUri());

            // 3. 실제 파일 존재 여부 확인
            if(!resource.exists() || !resource.isReadable()){
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }

            // 파일 다운로드 시 파일이 깨지는 거 해결해준 ContentDisposition
            ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                    .filename(originalFilename, StandardCharsets.UTF_8)
                    .build();

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM) // 바이너리 데이터
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                    .body(resource);

        } catch (MalformedURLException e) {
            log.error("파일 경로 오류", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 경로가 잘못되었습니다");
        } catch(IllegalArgumentException e){
            log.error("파일 정보 조회 실패", e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    // CKEditor의 이미지 업로드 처리를 위한 API
    @Operation(summary = "CKEditor's ImageHandler", description = "CKEditor의 이미지 등록을 위한 메서드")
    @PostMapping("/image/upload")
    public ResponseEntity<Map<String, Object>> uploadImage(@RequestParam("upload") MultipartFile file){

        Map<String, Object> response = new HashMap<>();

        try{
            // 1. 파일 이름 생성 및 경로 설정
            String originalImageName = file.getOriginalFilename();

            // 2. 고유한 파일명 생성(CKEditor 이미지용)
            String storedImageName = UUID.randomUUID().toString() + "_" + originalImageName;

            // 3. 파일이 저장될 경로
            String CKEditorImageSavePath = noticeCKEditorImageDir + storedImageName;

            // 4. 파일 시스템에 저장
            File saveFile = new File(CKEditorImageSavePath); // 🟢 변경된 변수 사용
            file.transferTo(saveFile);

            // 5. CKEditor에 반환할 응답 생성
            // accessUrl은 WebConfig의 정적 리소스 핸들러와 일치해야 함
            String accessUrl = "/images/" + storedImageName; //

            response.put("uploaded", 1);
            response.put("url", "http://localhost:8090" + accessUrl); // 클라이언트가 접근할 수 있는 전체 URL

            System.out.println("CKEedior 이미지 업로드 하고싶다: " + response);
        } catch (IOException e) {
            e.printStackTrace();
            response.put("uploaded", 0);
            response.put("error", Map.of("message", "파일 업로드 실패"));
        }
        return ResponseEntity.ok(response);
    }

    // ################################################################
    // 게시글 수정
    // ################################################################

    // Put api/notice/update/{id}
    @Operation(summary = "공지사항 수정 처리", description = "수정된 공지사항 정보를 받아 DB에 반영하고, 수정된 DTO를 JSON으로 반환")
    @PutMapping("/update/{id}")
    public ResponseEntity<NoticeDto> updateNotice(
            @PathVariable Long id,
            @ModelAttribute NoticeDto noticeDto,  // ModelAttribute로 받아서 텍스트 필드와 noticeDto내의 MultipartFile 필드를 받도록 준비
            @RequestParam(value = "uploadFiles", required = false) List<MultipartFile> newFiles, // 2. 새 파일들 클라이언트에서 uploadFiles로 보냄
            @RequestParam(value = "deleteFileIds", required = false) List<Long> deleteFileIds   // 삭제할 파일들 클라이언트에서 deleteFileIds로 보냄
    ){
        log.info("Put /api/notice/{id}... 게시글 수정 apiNoticeController", id);

        // Dto에 Id 설정(경로 변수 사용)
        noticeDto.setId(id);

        try{
            // NoticeService에 텍스트, 새 파일, 삭제할 ID 목록을 전달
            NoticeDto updateNotice = noticeService.update(noticeDto, newFiles, deleteFileIds);

            log.info("게시글 수정 완료, ID: ()", updateNotice.getId());

            // 수정된 DTO와 200 OK상태 반환
            return new ResponseEntity<>(updateNotice, HttpStatus.OK);
        }catch (IllegalArgumentException e){
            // 비밀번호 불일치 예외 처리
            if(e.getMessage().contains("비밀번호")){
                log.warn("게시글 수정 실패: 비밀번호 불일치 (ID: {})", id);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();  // 401 반환
            }
            log.error("게시글 수정 중 오류 발생: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);    // 다른 유효성 오류

        }catch(Exception e){
            log.error("게시글 수정 중 오류 발생: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    // 삭제
    @Operation(summary = "noticeDelete", description = "공지사항 삭제")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id){
        log.info("Delete /api/notice/delete/{id} ... 게시글 삭제 요청, apiNoticeController", id);

        // 서비스의 삭제 로직 호출
        noticeService.delete(id);

        return ResponseEntity.ok("삭제 성공");
    }



}
