package com.example.demo.domain.dto;

import com.example.demo.domain.entity.NoticesEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class NoticesDto {
    private Long id;
    private String author;
    private String noticesTitle;
    private String noticesContents;
    private int noticesView;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;

    // 충돌 방지를 위해 필드 이름을 attachedFiles로 변경
    private List<NoticesFileDto> attachedFiles;

    // DTO에 파일 정보 추가하는 메서드(Service에서 사용)
    public NoticesDto addFiles(List<NoticesFileDto> fileList){
        this.attachedFiles = fileList;
        return this;
    }

    //공지사항 수정 시 삭제할 파일의 ID 목록을 받기위한 필드
    private List<Long> deletedFileIds;

}
