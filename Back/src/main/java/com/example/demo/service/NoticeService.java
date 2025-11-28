package com.example.demo.service;

import com.example.demo.domain.dto.BoardDto;
import com.example.demo.domain.dto.BoardFileDto;
import com.example.demo.domain.dto.NoticeDto;
import com.example.demo.domain.dto.NoticeFileDto;
import com.example.demo.domain.entity.BoardEntity;
import com.example.demo.domain.entity.BoardFileEntity;
import com.example.demo.domain.entity.NoticeEntity;
import com.example.demo.domain.entity.NoticeFileEntity;
import com.example.demo.domain.repository.BoardFileRepository;
import com.example.demo.domain.repository.BoardRepository;
import com.example.demo.domain.repository.NoticeFileRepository;
import com.example.demo.domain.repository.NoticeRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final NoticeFileRepository noticeFileRepository;

    @Value("${noticeFile.dir}")       // 파일 저장 경로
    private String noticeFileDir;

    public NoticeService(NoticeRepository boardRepository, NoticeFileRepository noticeFileRepository) {
        this.noticeRepository = boardRepository;
        this.noticeFileRepository = noticeFileRepository;
    }

    // 공지사항 저장
    @Transactional
    public NoticeDto save(NoticeDto noticeDto) throws IOException {

        // 1. HTML 태그 클리닝 및 정리(IMG태그를 포함한 서식 태그 허용)
        if (noticeDto.getNoticeContents() != null) {

            // 텍스트 서식 태그와 img태그를 허용하고 나머지는 제거
            String cleanText = Jsoup.clean(noticeDto.getNoticeContents(), Safelist.basicWithImages());

            noticeDto.setNoticeContents(cleanText);
        }

        // 2. 파일 미첨부 시
        if (noticeDto.getNoticeFileUpload() == null || noticeDto.getNoticeFileUpload().isEmpty()) {
            NoticeEntity noticeEntity = NoticeEntity.toSaveEntity(noticeDto);
            noticeRepository.save(noticeEntity);
            return noticeDto.toNoticeDto(noticeEntity); // Lazy Loading 문제 없음
        } else {
            // 3. 파일 첨부 시 (Lazy Loading 문제 해결 포함)


            // NoticeEntity 저장 (fileAttached = 1로 설정된 Entity)
            NoticeEntity noticeEntity = NoticeEntity.toSaveFileEntity(noticeDto);
            noticeRepository.save(noticeEntity);

            List<NoticeFileEntity> savedFileEntityList = new ArrayList<>();

            for (MultipartFile noticeFile : noticeDto.getNoticeFileUpload()) {
                String noticeOriginalFilename = noticeFile.getOriginalFilename();
                String noticeStoredFilename = UUID.randomUUID() + "_" + noticeOriginalFilename;
                String savePath = noticeFileDir + noticeStoredFilename;

                long fileSize = noticeFile.getSize();


                // 파일 시스템에 저장
                noticeFile.transferTo(new File(savePath));

                // NoticeFileEntity 생성 및 관계 설정
                NoticeFileEntity noticeFileEntity = NoticeFileEntity.toNoticeFileEntity(noticeEntity, noticeOriginalFilename, noticeStoredFilename, fileSize);

                // NoticeFileEntity 저장
                noticeFileRepository.save(noticeFileEntity);

                // 메모리상의 리스트에 추가
                savedFileEntityList.add(noticeFileEntity);
            }

            // 이렇게 하면 toNoticeDto 호출 시 DB 접근 없이 메모리의 파일 리스트를 사용
            noticeEntity.setNoticeFileEntityList(savedFileEntityList);

            // c. DTO 변환 및 반환
            return NoticeDto.toNoticeDto(noticeEntity);
        }
    }

    // 게시글 전부 찾기
    @Transactional
    public List<NoticeDto> findAll(){

        List<NoticeEntity> noticeEntityList = noticeRepository.findAll();

        List<NoticeDto> noticeDtoList = new ArrayList<>();

        for(NoticeEntity noticeEntity: noticeEntityList){
            noticeDtoList.add(NoticeDto.toNoticeDto(noticeEntity));
        };

        return noticeDtoList;
    }

    // 조회수 올리기
    @Transactional
    public void updateHits(Long id){
        noticeRepository.updateHits(id);
    }

    @Transactional
    // 게시글 클릭 시 DB에 있는 내용 보여주기
    public NoticeDto findById(Long id){
        Optional<NoticeEntity> optionalNoticeEntity = noticeRepository.findById(id);
        if(optionalNoticeEntity.isPresent()){
//            return BoardDto.toBoardDto(optionalBoardEntity.get());

            NoticeEntity noticeEntity = optionalNoticeEntity.get();
            NoticeDto noticeDto = NoticeDto.toNoticeDto(noticeEntity);
            return noticeDto;
        }else {
            return null;
        }
    }

    // 첨부파일 다운로드
    @Transactional
    public NoticeFileDto fileDownloadByIndex(Long noticeId, int fileIndex){

        // 게시글 조회
        NoticeDto noticeDto = findById(noticeId);

        // 2. 유효성 검사
        if(noticeDto == null || noticeDto.getNoticeFileAttached() != 1 ||
                noticeDto.getNoticeFileDtoList() == null ||
                noticeDto.getNoticeFileDtoList().size() <= fileIndex
        ){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "파일을 찾을 수 없습니다");
        }

        // 3. 해당 인덱스의 파일 DTO 반환
        return noticeDto.getNoticeFileDtoList().get(fileIndex);

    }

    // ################################################################
    // 게시글 수정하고싶다
    // ################################################################

    @Transactional
    public NoticeDto update(NoticeDto noticeDto, List<MultipartFile> newFiles, List<Long> deleteFileIds) throws IOException {
        // 1. 기존 게시글 조회
        NoticeEntity noticeEntity = noticeRepository.findById(noticeDto.getId())
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));


        // 3. 텍스트 정보 업데이트 (제목, 내용)
        // 불필요한 HTML 태그 제거
        if (noticeDto.getNoticeContents() != null) {
            String cleanText = Jsoup.clean(noticeDto.getNoticeContents(), Safelist.basicWithImages());

            noticeEntity.updateText(noticeDto.getNoticeTitle(), cleanText);
        }

        // 4. 파일 삭제 처리
        if (deleteFileIds != null && !deleteFileIds.isEmpty()) {
            for (Long fileId : deleteFileIds) {
                // DB에서 파일 정보 조회
                NoticeFileEntity fileEntity = noticeFileRepository.findById(fileId).orElse(null);

                if (fileEntity != null) {   // 삭제하는 파일이 파일엔티티에 담겨있다면?
                    // 로컬 디스크에서 파일 삭제
                    String savePath = noticeFileDir + fileEntity.getNoticeStoredFilename();
                    File file = new File(savePath);
                    if (file.exists()) {
                        if(!file.delete()){
                            log.error("파일 삭제 실패: {}", savePath);
                        }
                    }
                    // DB에서 파일 데이터 삭제
                    noticeFileRepository.delete(fileEntity);
                }
            }
        }

        // 5. 새 파일 추가 처리
        if (newFiles != null && !newFiles.isEmpty()) {
            for (MultipartFile noticeFile : newFiles) {
                if (!noticeFile.isEmpty()) {
                    String originalFilename = noticeFile.getOriginalFilename();
                    String storedFilename = System.currentTimeMillis() + "_" + originalFilename;
                    String savePath = noticeFileDir + storedFilename;

                    Long fileSize = noticeFile.getSize();    // 파일 크기 가져오기

                    noticeFile.transferTo(new File(savePath));

                    NoticeFileEntity noticeFileEntity = NoticeFileEntity.toNoticeFileEntity(noticeEntity, originalFilename, storedFilename, fileSize);
                    noticeFileRepository.save(noticeFileEntity);
                }
            }
        }

        // 6. 파일 첨부 여부(fileAttached) 상태 업데이트
        // 현재 이 게시글에 연결된 파일 개수 확인
        // 변경 사항이 즉시 DB에 반영되도록 saveAndFlush를 호출
        // 삭제 및 추가된 파일 정보가 모두 DB에 담기기 위해서
        noticeRepository.saveAndFlush(noticeEntity); // 변경 사항 즉시 반영 (혹시 모를 지연 처리 방지)

        long currentFileCount = noticeFileRepository.countByNoticeEntityId(noticeEntity.getId());

        if(currentFileCount == 0){
            noticeEntity.updateFileAttached(0);
        }else{
            noticeEntity.updateFileAttached(1);
        }

        return noticeDto.toNoticeDto(noticeEntity);
    }



    // 페이징 기능
    @Transactional
    public Page<NoticeDto> paging(Pageable pageable){
        int page = pageable.getPageNumber() - 1;
        int pageLimit = 10;  // 한 페이지에 보여줄 글 개수

        // 한 페이지당 pageLimit만큼 글을 보여주고 정렬 기준은 id 기준으로 내림차순 정렬
        // noticeEntities -> 스프링부트JPA에서 제공하는 듯
        // page 위치에 있는 값은 0부터 시작하니까 page변수 설정 시 -1 해놓음
        Page<NoticeEntity> noticeEntities =
                noticeRepository.findAll(PageRequest.of(page, pageLimit, Sort.by(Sort.Direction.DESC, "id")));
        // page -> 몇 페이지?, pageLimit -> 한 페이지에 몇 개?, Sort.By -> 정렬기준은?

        // 필요한 정보들
        System.out.println("noticeEntities.getContent(): " + noticeEntities.getContent());  // 요청 페이지에 해당하는 글들
        System.out.println("noticeEntities.getTotalElements(): " + noticeEntities.getTotalElements()); // 전체 글 갯수
        System.out.println("noticeEntities.getNumber(): " + noticeEntities.getNumber()); // DB로 요청한 페이지 번호
        System.out.println("noticeEntities.getTotalPages(): " + noticeEntities.getTotalPages()); // 전체 페이지 갯수
        System.out.println("noticeEntities.getSize(): " + noticeEntities.getSize()); // 한 페이지에 보여지는 글 갯수
        System.out.println("noticeEntities.hasPrevious(): " + noticeEntities.hasPrevious()); // 이전 페이지 존재 여부
        System.out.println("noticeEntities.isFirst()" + noticeEntities.isFirst()); // 첫 페이지 여부
        System.out.println("noticeEntities.isLast()" + noticeEntities.isLast());    // 마지막 페이지 여부

        // 목록에서 보일 것들 -> id, writer, title, hits, createdTime
        Page<NoticeDto> noticeDtos = noticeEntities.map(notice -> new NoticeDto(notice.getId(), notice.getNoticeWriter(), notice.getNoticeTitle(), notice.getNoticeHits(), notice.getCreatedTime()));  // Page 객체에서 제공하는 map메서드 -> 안의 거를 하나씩 꺼내는 역할
        return noticeDtos;
    }

    // 삭제 기능
    @Transactional
    public void delete(Long id){
        noticeRepository.deleteById(id);
    }




}
