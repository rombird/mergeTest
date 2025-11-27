package com.example.demo.domain.dto;

import com.example.demo.domain.entity.BoardFileEntity;
import com.example.demo.domain.entity.NoticeFileEntity;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class NoticeFileDto {
    private Long id;
    private String originalFilename;
    private String storedFilename;
    private Long fileSize;


    // Entity -> Dto 변환 메서드
    public static NoticeFileDto toNoticeFileDto(NoticeFileEntity noticeFileEntity){
        NoticeFileDto noticeFileDto = new NoticeFileDto();

        noticeFileDto.setId(noticeFileEntity.getId());
        noticeFileDto.setOriginalFilename(noticeFileEntity.getNoticeOriginalFilename());
        noticeFileDto.setStoredFilename(noticeFileEntity.getNoticeStoredFilename());
        noticeFileDto.setFileSize(noticeFileEntity.getNoticeFileSize());

        return noticeFileDto;
    }



}
