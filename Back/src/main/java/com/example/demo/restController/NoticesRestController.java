package com.example.demo.restController;

import com.example.demo.domain.dto.NoticesDto;
import com.example.demo.domain.entity.NoticesFile;
import com.example.demo.domain.service.NoticesFileService;
import com.example.demo.domain.service.NoticesService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

//REACT 분리 VER.
@Slf4j
@RestController
@RequestMapping("/api/notices") //-> JSON 반환 -> React에서 화면 렌더링
@RequiredArgsConstructor
@Tag(name="NoticesRestController", description="This is NoticesRestController")
@CrossOrigin(origins = "http://localhost:3000") // CORS 설정 추가

public class NoticesRestController {

    private final NoticesService noticesService;
    private final NoticesFileService noticesFileService;

    //1. 공지사항 목록 조회 + 검색 + 페이징: GET /api/notices
    // GET /api/notices?page=0&size=10&keyword=검색어
    @GetMapping
    // 💡 반환 타입을 List<NoticesDto>에서 Page<NoticesDto>로 변경
    public ResponseEntity<Page<NoticesDto>> getAllNotices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword
    ){
        // 💡 Service 메서드 이름 및 파라미터 변경
        Page<NoticesDto> noticesPage = noticesService.findNoticesWithPagingAndSearch(page, size, keyword);
        // 💡 List<NoticesDto> 대신 Page<NoticesDto> 반환
        return ResponseEntity.ok(noticesPage);
    }

    //2. 상세 조회 및 조회수 증가 READ + UPDATE : GET /api/notices/{id}
    @GetMapping("/{id}")//‼️‼️
    public ResponseEntity<NoticesDto> getNoticesByIdApi(@PathVariable Long id){
        NoticesDto notice = noticesService.findNoticesDetail(id);
        return ResponseEntity.ok(notice); //JSON 데이터 반환
    }

    //3. 작성 : POST /api/notices - 파일 업로드 포함
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NoticesDto> saveNoticesApi(
            // @ModelAttribute는 복합 데이터(DTO + 파일) 수신에 적합
            @ModelAttribute NoticesDto dto,
            @RequestParam(value = "files", required = false) List<MultipartFile> files
    ) {
        try {
            // Service 호출 시 DTO와 파일 리스트 함께 전달
            NoticesDto savedDto = noticesService.saveNotices(dto, files);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedDto);
        } catch (IOException e) {
            log.error("공지사항 및 파일 생성 중 I/O 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            log.error("공지사항 생성 중 예상치 못한 오류 발생", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    //4. 수정 : PUT /api/notices/{id} - 파일 업로드, 삭제 포함
    @SecurityRequirement(name = "BearerAuth")
    @PutMapping("/{id}")//‼️‼️
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NoticesDto> updateNoticesApi(
            @PathVariable Long id,
            @ModelAttribute NoticesDto dto,
            @RequestParam(value = "newFiles", required = false) List<MultipartFile> newFiles
    ) {
        try {
            // Service 호출 시 ID, DTO, 새 파일 리스트 함께 전달
            NoticesDto updatedDto = noticesService.updateNotices(id, dto, newFiles);
            return ResponseEntity.ok(updatedDto);
        } catch (IOException e) {
            log.error("공지사항 및 파일 수정 중 I/O 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (IllegalArgumentException e) {
            log.warn("Notices not found for update: id={}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    //---------------------------------------------------------
    // 5. 파일 다운로드 API (새로 추가)
    //---------------------------------------------------------
    /**
     * [최종] 파일 다운로드 API - 사용자님의 견고한 로직을 유지하고 MIME Type 설정을 통합
     * @param fileId 다운로드할 파일 ID
     * @return 다운로드 응답 (Resource 포함)
     */
    @GetMapping("/download/{fileId}")//‼️‼️
    // IOException을 던지도록 선언하여 Resource.contentLength() 호출 가능하도록 합니다.
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) throws IOException {

        // 1. 파일 ID로 DB에서 파일 메타 정보(경로, 이름, MIME Type) 조회
        //    (Service에서 파일이 없거나 읽을 수 없는 경우 ResponseStatusException 던짐)
        NoticesFile fileInfo = noticesFileService.downloadFile(fileId);

        // 2. 파일 경로를 기반으로 실제 파일 리소스를 로드
        Resource resource = noticesFileService.getFileResource(fileInfo.getFilePath());

        // 3. 파일 이름 인코딩 (한글 파일명 깨짐 방지 - 사용자님 원본 로직 유지)
        String originalFileName = fileInfo.getOriginalFileName();

        // +를 %20으로 치환하는 것은 필수
        String encodedFileName = URLEncoder.encode(originalFileName, StandardCharsets.UTF_8.name())
                .replaceAll("\\+", "%20");

        // 4. HTTP 헤더 설정 (다운로드 형식 지정)
        HttpHeaders headers = new HttpHeaders();

        // Content-Disposition 설정 (RFC 표준: filename="ASCII fallback"; filename*=UTF-8''encoded)
        // ASCII 안전명 (공백이나 특수 문자를 언더바로 치환)
        String asciiSafeName = originalFileName.replaceAll("[^a-zA-Z0-9._-]", "_");
        String contentDisposition = String.format(
                "attachment; filename=\"%s\"; filename*=UTF-8''%s",
                asciiSafeName,
                encodedFileName
        );

        headers.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition);

        // UX 개선 핵심: DB에 저장된 MIME Type을 헤더에 설정
        headers.setContentType(MediaType.parseMediaType(fileInfo.getMimeType()));

        log.info("Downloading file: {}, Content-Type: {}", originalFileName, fileInfo.getMimeType());

        // 5. ResponseEntity 반환 (Resource와 헤더 전달)
        return ResponseEntity.ok()
                // Content-Length 헤더 명시 (다운로드 진행 상황 표시 위함)
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(resource.contentLength()))
                .headers(headers)
                .body(resource);

    }


    //6. 삭제 : DELETE /api/notices/{id}
    @SecurityRequirement(name = "BearerAuth")
    @DeleteMapping("/{id}")//‼️‼️
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteNoticesApi(@PathVariable Long id){
        noticesService.deleteNotices(id);
        return ResponseEntity.noContent().build(); //204 No Content return
    }


}


