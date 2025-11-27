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
}
