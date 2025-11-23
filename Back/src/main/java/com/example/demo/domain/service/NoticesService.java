package com.example.demo.domain.service;

import com.example.demo.domain.dto.NoticesDto;
import com.example.demo.domain.entity.NoticesEntity;
import com.example.demo.domain.repository.NoticesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


@Service
@RequiredArgsConstructor
public class NoticesService {

    // 💡 NoticesFileService: 선생님께서 사용하신 파일 서비스 이름 유지
    private final NoticesRepository noticesRepository;
    private final NoticesFileService noticesFileService;

    //---------------------------------------------------------
    // 1. 검색 + 페이지네이션 적용 목록조회
    //---------------------------------------------------------
    @Transactional(readOnly = true)
    public Page<NoticesDto> findNoticesWithPagingAndSearch(int page, int size, String keyword) {

        // 💡 Pageable 생성 시 정렬 기준(예: id 내림차순) 추가
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<NoticesEntity> entityPage;

        if (keyword != null && !keyword.isBlank()) {
            // 검색어가 있을 경우
            // Repository는 Page<NoticesEntity>를 반환하도록 변경됨 (아래 3번 참고)
            entityPage = noticesRepository.findByNoticesTitleContainingOrNoticesContentsContaining(keyword, keyword, pageable);
        } else {
            // 검색어 없으면 전체 조회 (Page<NoticesEntity> 반환)
            entityPage = noticesRepository.findAll(pageable);
        }

        // 💡 Page 객체의 map() 메서드를 사용하여 DTO로 변환
        return entityPage.map(NoticesEntity::toDto);
    }

    //---------------------------------------------------------
    // 2. 상세 조회 및 조회수 증가
    //---------------------------------------------------------
    @Transactional
    public NoticesDto findNoticesDetail(Long id){
        // 조회수 증가 쿼리 호출 (DB에서 1증가 처리)
        noticesRepository.updateViews(id);

        NoticesEntity entity = noticesRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notices not found with id : " + id));

        // 💡 NoticesEntity의 toDto() 메서드 사용
        return entity.toDto();
    }

    //---------------------------------------------------------
    // 3. 작성(Create) - 파일 업로드 포함 (가장 최신 버전)
    //---------------------------------------------------------

    /**
     * 공지사항 등록 (파일 업로드 포함)
     * @param dto 공지사항 텍스트 데이터 (title, content 등)
     * @param files 클라이언트에서 받은 파일 목록
     * @return 저장된 NoticesDto
     */
    @Transactional
    public NoticesDto saveNotices(NoticesDto dto, List<MultipartFile> files) throws IOException {

        // 1. 작성자 자동 삽입 로직 (기존 saveNotices(dto)에서 통합)
        String currentAdminUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        dto.setAuthor(currentAdminUsername);

        // 2. DTO를 NoticesEntity로 변환 후 DB에 저장 (ID 생성)
        NoticesEntity noticesEntity = noticesRepository.save(NoticesEntity.fromDto(dto));

        // 3. 파일 목록이 있다면 NoticesFileService를 통해 파일 저장
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                // FileService 호출: 실제 파일 저장 및 NoticesFile DB 정보 저장
                noticesFileService.saveFile(file, noticesEntity);
            }
        }

        // 4. 저장된 Entity를 DTO로 변환하여 반환
        // 💡 NoticesEntity의 toDto() 메서드 사용
        return noticesEntity.toDto();
    }


    //---------------------------------------------------------
    // 4. 수정(Update) - 파일 업로드 포함
    //---------------------------------------------------------

    /**
     * 공지사항 수정 (새 파일 추가 포함)
     */
    @Transactional
    public NoticesDto updateNotices(Long id, NoticesDto dto, List<MultipartFile> newFiles) throws IOException {
        NoticesEntity trueEntity = noticesRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notices not found with id : " + id));

        // 1. 공지사항 내용 업데이트 (Dirty Checking)
        trueEntity.updateFromDto(dto); // title(제목), contents(내용) 변경

        // 2. 기존 파일 삭제 처리 (NoticesDto의 deletedFileIds 사용)
        if (dto.getDeletedFileIds() != null && !dto.getDeletedFileIds().isEmpty()) {
            for (Long fileId : dto.getDeletedFileIds()) {
                // DB 레코드와 파일 시스템 파일을 모두 삭제합니다.
                noticesFileService.deleteFileById(fileId);
            }

            // 💡 [updatedTime 갱신 해결] 파일 삭제만 일어난 경우, Auditing 갱신을 강제하기 위해
            // 부모 엔티티의 필드를 명시적으로 업데이트합니다. (JPA Dirty Checking 발동)
            trueEntity.updateFromDto(dto);
        }

        // 3. 새 파일 추가 처리
        if (newFiles != null && !newFiles.isEmpty()) {
            for (MultipartFile file : newFiles) {
                // FileService 호출: 새 파일을 기존 엔티티에 연결
                noticesFileService.saveFile(file, trueEntity);
            }

            // [updatedTime 갱신 해결] 파일 추가 후, Auditing 갱신을 강제하기 위해
            // 부모 엔티티의 필드를 명시적으로 업데이트(JPA Dirty Checking 발동)
            trueEntity.updateFromDto(dto);
        }

        // 3. 수정된 Entity를 DTO로 변환하여 반환
        // 💡 NoticesEntity의 toDto() 메서드 사용
        return trueEntity.toDto();
    }


    //---------------------------------------------------------
    // 5. 삭제(Delete) - 파일 삭제 포함
    //---------------------------------------------------------
    @Transactional
    public void deleteNotices(Long id){
        //Entity 조회(파일 목록 가져오기)
        NoticesEntity noticesEntity = noticesRepository.findById(id)
                        .orElseThrow(()->new IllegalArgumentException("Notices not found with id : " + id));

        //연결된 파일들 시스템에서 삭제
        noticesFileService.deleteFilesByNotices(noticesEntity);

        //공지사항 Entity 삭제(NoticesFile 레코드도 DB에서 CASCADE로 삭제
        noticesRepository.deleteById(id);
    }
}