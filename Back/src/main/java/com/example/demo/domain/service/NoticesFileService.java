package com.example.demo.domain.service;

import com.example.demo.domain.entity.NoticesEntity;
import com.example.demo.domain.entity.NoticesFile;
import com.example.demo.domain.repository.NoticesFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticesFileService {

    private final NoticesFileRepository noticesFileRepository;

    @Value("${file.upload.dir}")
    private String uploadDir;

    //---------------------------------------------------------
    // 1. 파일 저장 로직 (업로드) - MIME Type 저장 로직 포함
    //---------------------------------------------------------

    /**
     * 실제 파일 시스템에 파일을 저장하고, DB에 파일 메타 정보를 저장합니다.
     * @param file 클라이언트가 업로드한 파일
     * @param notices 파일이 연결될 NoticesEntity
     * @return 저장된 NoticesFile Entity
     */
    @Transactional
    public NoticesFile saveFile(MultipartFile file, NoticesEntity notices) throws IOException {
        if (file == null || file.isEmpty()) return null;

        String originalFileName = file.getOriginalFilename();

        // 1. 파일 저장 준비 (고유한 저장 파일명 생성)
        String storedFileName = UUID.randomUUID().toString() + "_" + originalFileName;
        // OS 독립적인 경로 설정을 위해 Path.of를 사용
        Path targetPath = Paths.get(uploadDir, storedFileName).normalize();
        String savePath = targetPath.toString();

        // 저장 디렉토리가 없으면 생성
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // 2. 실제 파일 저장
        File targetFile = targetPath.toFile();
        file.transferTo(targetFile);

        // 3. 파일의 실제 MIME Type 확인 (UX 개선의 핵심)
        String mimeType = Files.probeContentType(targetFile.toPath());
        if (mimeType == null) {
            // MIME Type을 확인할 수 없는 경우 기본 값 설정
            mimeType = "application/octet-stream";
        }

        // 4. DB에 저장할 NoticesFile Entity 생성
        NoticesFile fileEntity = new NoticesFile();
        fileEntity.setOriginalFileName(originalFileName);
        fileEntity.setStoredFileName(storedFileName);
        fileEntity.setFilePath(savePath);
        fileEntity.setFileSize(file.getSize());
        fileEntity.setMimeType(mimeType); // 💡 MIME Type 저장
        fileEntity.setNotices(notices); // 커스텀 setNotices() 메서드 (양방향 연결) 호출

        return noticesFileRepository.save(fileEntity);
    }

    //---------------------------------------------------------
    // 2. 파일 다운로드 로직 (통합 메서드)
    //---------------------------------------------------------

    /**
     * 파일 ID로 DB 정보를 조회하고, 실제 파일을 Resource로 로드할 준비를 합니다.
     * @param fileId 다운로드할 파일의 고유 ID
     * @return 파일 엔티티
     */
    @Transactional(readOnly = true)
    public NoticesFile downloadFile(Long fileId) {
        // 1. DB에서 파일 정보(NoticesFile 엔티티)를 조회
        NoticesFile fileEntity = noticesFileRepository.findById(fileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found with id: " + fileId));

        // 2. 파일 경로를 기반으로 Resource 로드 및 검증
        Resource resource = getFileResource(fileEntity.getFilePath());

        if (!resource.exists() || !resource.isReadable()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not readable or found on disk: " + fileEntity.getStoredFileName());
        }

        return fileEntity;
    }


    //---------------------------------------------------------
    // 3. 파일 Resource 로드 (다운로드 실행)
    //---------------------------------------------------------

    /**
     * 파일 경로를 기반으로 실제 파일 시스템에서 파일을 Resource 형태로 로드합니다.
     * @param filePath NoticesFile Entity에 저장된 파일 시스템 경로
     * @return 실제 파일 데이터를 담고 있는 Spring Resource 객체
     */
    public Resource getFileResource(String filePath) {
        try {
            // 경로를 정규화하여 보안상 문제 방지
            Path path = Paths.get(filePath).normalize();
            Resource resource = new UrlResource(path.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not read file: " + filePath);
            }
        } catch (MalformedURLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "File path error: " + filePath, e);
        }
    }

    //---------------------------------------------------------
    // 4. 파일 삭제 로직
    //---------------------------------------------------------

    // [헬퍼 메서드] 물리적 파일 삭제 로직: Files.deleteIfExists를 사용하여 예외 처리 개선
    private void deleteFile(NoticesFile fileEntity) {
        try {
            Path filePath = Paths.get(fileEntity.getFilePath());
            if (Files.exists(filePath) && Files.deleteIfExists(filePath)) {
                log.info("File successfully deleted from disk: {}", fileEntity.getFilePath());
            } else {
                log.warn("Failed to delete file from disk or file not found: {}", fileEntity.getFilePath());
            }
        } catch (Exception e) {
            log.error("Error deleting file: {}", fileEntity.getFilePath(), e);
        }
    }

    /**
     * 특정 파일 ID를 사용하여 DB 레코드를 삭제하고, 파일 시스템에서도 파일을 삭제합니다.
     * (주로 개별 파일 수정/삭제 시 사용)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteFileById(Long fileId) {
        NoticesFile fileEntity = noticesFileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found with id: " + fileId));

        // 1. 파일 시스템에서 삭제 (물리적 삭제)
        deleteFile(fileEntity);

        // 2. DB 레코드 삭제
        noticesFileRepository.delete(fileEntity);
        log.info("File record deleted from DB: fileId={}", fileId);
    }

    /**
     * [컴파일 에러 해결 및 데이터 정합성 보장] 공지사항 삭제 시 연관된 모든 파일 삭제 로직
     * @param notices 삭제할 NoticesEntity
     */
    @Transactional(propagation = Propagation.REQUIRED) // 상위 트랜잭션과 동일하게 동작
    public void deleteFilesByNotices(NoticesEntity notices) {
        // [JPA 관례 적용] NoticesEntity 객체를 사용하여 연결된 파일 목록 조회
        List<NoticesFile> filesToDelete = noticesFileRepository.findByNotices(notices);

        if (filesToDelete.isEmpty()) {
            log.info("Notices ID {}에 연결된 삭제할 파일이 없습니다.", notices.getId());
            return;
        }

        log.info("Notices ID {}에 연결된 파일 {}개를 삭제합니다.", notices.getId(), filesToDelete.size());

        for (NoticesFile file : filesToDelete) {
            // 1. 파일 시스템에서 삭제 (물리적 삭제)
            deleteFile(file);
        }

        // 2. DB 레코드 일괄 삭제 (상위 트랜잭션의 성공/실패에 따라 같이 처리됨)
        noticesFileRepository.deleteAll(filesToDelete);
        log.info("Notices ID {}에 연결된 파일 레코드 {}개 삭제 완료.", notices.getId(), filesToDelete.size());
    }
}