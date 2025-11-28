package com.example.demo.service;

import com.example.demo.domain.dto.BoardDto;
import com.example.demo.domain.dto.NoticeDto;
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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
}
