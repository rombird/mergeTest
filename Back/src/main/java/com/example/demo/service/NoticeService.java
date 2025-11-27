package com.example.demo.service;

import com.example.demo.domain.dto.NoticeDto;
import com.example.demo.domain.repository.BoardFileRepository;
import com.example.demo.domain.repository.BoardRepository;
import com.example.demo.domain.repository.NoticeFileRepository;
import com.example.demo.domain.repository.NoticeRepository;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NoticeService {

    private final NoticeRepository boardRepository;
    private final NoticeFileRepository noticeFileRepository;

    @Value("${noticeFile.dir}")       // 파일 저장 경로
    private String noticeFileDir;

    public NoticeService(NoticeRepository boardRepository, NoticeFileRepository noticeFileRepository) {
        this.boardRepository = boardRepository;
        this.noticeFileRepository = noticeFileRepository;
    }

    public NoticeDto save(NoticeDto noticeDto){

        // 1. HTML 태그 클리닝 및 정리(IMG태그를 포함한 서식 태그 허용)
        if(noticeDto.getNoticeContents() != null){

            // 텍스트 서식 태그와 img태그를 허용하고 나머지는 제거
            String cleanText = Jsoup.clean(noticeDto.getNoticeContents(), Safelist.basicWithImages());

            noticeDto.setNoticeContents(cleanText);
        }




        return null;
    }

}
