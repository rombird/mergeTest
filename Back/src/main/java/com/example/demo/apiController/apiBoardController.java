package com.example.demo.apiController;

import com.example.demo.domain.dto.BoardDetailResponse;
import com.example.demo.domain.dto.BoardDto;
import com.example.demo.domain.dto.BoardFileDto;
import com.example.demo.domain.dto.CommentDto;
import com.example.demo.service.BoardService;
import com.example.demo.service.CommentService;
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
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@Tag(name = "apiBoardController", description = "게시판 REST API")
@RequestMapping("/api/board")
public class apiBoardController {

    private final BoardService boardService;
    private final CommentService commentService;

    // 파일 저장 경로
    @Value("${file.dir}")       // 파일 저장 경로
    private String fileDir;

    @Value("${CKEditor.image}")
    private String CKEditorImageDir;

// ################################################################
    // 게시판 목록 데이터 보내기
// ################################################################
//    @CrossOrigin(origins = {"http://localhost:3000", "http://192.168.5.7:3000"})
    @Operation(summary = "PagingList", description = "게시글 목록 및 페이징 정보")
    @GetMapping("/paging")
    public ResponseEntity<?> paging(
            @PageableDefault(page = 0, size = 10) Pageable pageable){     // @PageableDefault(page = 1) -> 기본적으로 1페이지 보여줄래
        log.info("GET  /api/board/paging... 페이징처리 apiBoardController");
        Page<BoardDto> boardList = boardService.paging(pageable);

//        int blockLimit = 10;
//
//        // startPage, endPage 계산 시 0-기반인 boardList.getNumber()를 1-기반으로 변환하여 사용해야 합니다.
//        // boardList.getNumber()는 현재 페이지 인덱스 (0부터 시작)
//        int currentPageOneBased = boardList.getNumber() + 1;
//
//        // 1-기반 페이지를 사용하여 startPage 계산
//        int startPage = (((int)(Math.ceil((double)currentPageOneBased / blockLimit))) -1) * blockLimit + 1;
//        int endPage = ((startPage + blockLimit - 1) < boardList.getTotalPages()) ? startPage + blockLimit - 1 : boardList.getTotalPages();
//
//        Map<String, Object> response = new HashMap();
//
//        response.put("boardList" , boardList);
//        response.put("startPage", startPage);
//        response.put("endPage", endPage);
//
//        return ResponseEntity.ok(response);

        int blockLimit = 10;
//        // React에서 startPage, endPage 계산에 필요한 정보를 함께 JSON으로 반환
//        int startPage = (((int)(Math.ceil((double)pageable.getPageNumber() / blockLimit))) -1) * blockLimit + 1; // 1, 4, 7,
//        int endPage = ((startPage + blockLimit - 1) < boardList.getTotalPages()) ? startPage + blockLimit - 1 : boardList.getTotalPages();


        int currentPage = pageable.getPageNumber() + 1; // ★ 0 기반 → 1 기반

        int startPage = ((currentPage - 1) / blockLimit) * blockLimit + 1;

        int endPage = Math.min(startPage + blockLimit - 1, boardList.getTotalPages());

        // Json 응답을 위한 Map 또는 별도의 DTO 사용

        Map<String, Object> response = new HashMap();

        response.put("boardList" , boardList);
        response.put("startPage", startPage);
        response.put("endPage", endPage);

        return ResponseEntity.ok(response); // JSON 형태로 데이터를 반환
    }


    // 글 쓴거 포스팅
    @Operation(summary = "writeBoardPost", description = "글 쓴거 DB로 보냄")
    @PostMapping("/writeBoard") // /api/board 로 POST 요청
    public ResponseEntity<BoardDto> write(
            // 1. 폼 데이터 (제목, 글쓴이, 내용 등)를 DTO에 바인딩
            @ModelAttribute BoardDto boardDto,
            // 2. 파일 데이터를 "fileUpload" 키로 명시적으로 받음
            @RequestPart(value = "uploadFiles", required = false) List<MultipartFile> fileUploads) throws IOException {

        log.info("POST /api/board/writeBoard 게시글 작성 요청: {}", boardDto.getBoardTitle());

        // 3. 수신한 파일을 DTO의 필드에 수동으로 설정
        // DTO에 List<MultipartFile> fileUpload; 필드가 있으므로 사용 가능
        if (fileUploads != null && !fileUploads.isEmpty()) {
            boardDto.setFileUpload(fileUploads);
        }

        // 4. 서비스 호출 (Service 계층에서는 boardDto.getFileUpload()로 파일 접근)
        BoardDto savedBoard = boardService.save(boardDto);

        // 201 Created 응답과 함께 저장된 게시글 객체를 반환
        return new ResponseEntity<>(savedBoard, HttpStatus.CREATED);
    }

    // ################################################################
    // 게시글 조회
    // ################################################################

    @Operation(summary = "boardDetail", description = "게시글 단건 조회")
    @GetMapping("/{id}")
    public ResponseEntity<BoardDetailResponse> findById(@PathVariable Long id) {
        log.info("GET /api/board/{id}... 게시글 단건 조회 apiBoardController");
        System.out.println("id:" + id);
        // 해당 게시글의 조회수를 하나 늘리고
        boardService.updateHits(id);

        // 게시글 데이터를 가져와서 detail.html에 출력
        BoardDto boardDto = boardService.findById(id);

        // 댓글 목록 조회
        List<CommentDto>commentDtoList = commentService.findAll(id);

        // 응답 Dto에 데이터 통합
        BoardDetailResponse response = new BoardDetailResponse(boardDto, commentDtoList);

        System.out.println("response:" + response + "...apiBoardController의 findById");

        // HTTP 200 ok 상태코드와 함께 Json데이터를 반환
        return ResponseEntity.ok(response);
    }

    // ################################################################
    // 첨부 파일 다운로드
    // ################################################################

    @Operation(summary = "fileDownload", description = "첨부파일 다운로드")
    @GetMapping("/download/{boardId}/{fileIndex}")
    public ResponseEntity<Resource> fileDownload(@PathVariable Long boardId,
                                                 @PathVariable int fileIndex){
        log.info("get /api/board/download/{boardId}/{fileIndex}... 첨부파일 다운로드, apiBoardController");

        try{
            // 1. 서비스에서 해당 파일 정보(Dto) 가져오기
            BoardFileDto boardFileDto = boardService.fileDownloadByIndex(boardId, fileIndex);

            String originalFilename = boardFileDto.getOriginalFilename();
            String storedFilename = boardFileDto.getStoredFilename();

            System.out.println("오리지널파일이름, 저장파일이름: " + originalFilename +  ", " + storedFilename);
            // 2. 파일 경로 생성
            if(originalFilename == null || storedFilename == null){
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "DB에 파일 정보를 찾을 수 없습니다");
            }

            Path filePath = Paths.get(fileDir, storedFilename);
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


    // 삭제
    @Operation(summary = "boardDelete", description = "게시글 삭제")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id){
        log.info("Delete /api/board/delete/{id} ... 게시글 삭제 요청, apiBoardController", id);

        // 서비스의 삭제 로직 호출
        boardService.delete(id);

        return ResponseEntity.ok("삭제 성공");
    }


    // CKEditor의 이미지 업로드 처리를 위한 API
    @PostMapping("/image/upload")
    public ResponseEntity<Map<String, Object>> uploadImage(@RequestParam("upload") MultipartFile file){

        Map<String, Object> response = new HashMap<>();

        try{
            // 1. 파일 이름 생성 및 경로 설정
            String originalImageName = file.getOriginalFilename();

            // 2. 고유한 파일명 생성(CKEditor 이미지용)
            String storedImageName = UUID.randomUUID().toString() + "_" + originalImageName;

            // 3. 파일이 저장될 경로
            String CKEditorImageSavePath = CKEditorImageDir + storedImageName;

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

    // Put api/board/update/{id}
    @Operation(summary = "게시글 수정 처리", description = "수정된 게시글 정보를 받아 DB에 반영하고, 수정된 DTO를 JSON으로 반환")
    @PutMapping("/update/{id}")
    public ResponseEntity<BoardDto> updateBoard(
            @PathVariable Long id,
            @ModelAttribute BoardDto boardDto,  // ModelAttribute로 받아서 텍스트 필드와 boardDto내의 MultipartFile 필드를 받도록 준비
            @RequestParam(value = "uploadFiles", required = false) List<MultipartFile> newFiles, // 2. 새 파일들 클라이언트에서 uploadFiles로 보냄
            @RequestParam(value = "deleteFileIds", required = false) List<Long> deleteFileIds   // 삭제할 파일들 클라이언트에서 deleteFileIds로 보냄
    ){
        log.info("Put /api/board/{id}... 게시글 수정 apiBoardController", id);

        // Dto에 Id 설정(경로 변수 사용)
        boardDto.setId(id);

        // 비밀번호가 필수로 입력되므로, Dto에서 비밀번호 필드를 가져와야 함
        String inputPassword = boardDto.getBoardPass();
        if (inputPassword == null || inputPassword.isEmpty()){
            log.warn("비밀번호 누락: 수정을 위한 비밀번호가 입력되지 않았습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();  // 401 오류(권한 없음) 반환
        }

        try{
            // BoardService에 텍스트, 새 파일, 삭제할 ID 목록을 전달
            BoardDto updateBoard = boardService.update(boardDto, newFiles, deleteFileIds);

            log.info("게시글 수정 완료, ID: ()", updateBoard.getId());

            // 수정된 DTO와 200 OK상태 반환
            return new ResponseEntity<>(updateBoard, HttpStatus.OK);
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




}
