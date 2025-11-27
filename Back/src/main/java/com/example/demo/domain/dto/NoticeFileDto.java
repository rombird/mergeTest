//package com.example.demo.domain.dto;
//
//import com.example.demo.domain.entity.BoardFileEntity;
//import lombok.*;
//
//@Getter
//@Setter
//@ToString
//@NoArgsConstructor
//@AllArgsConstructor
//public class NoticeFileDto {
//    private Long id;
//    private String originalFilename;
//    private String storedFilename;
//    private Long fileSize;
//
//
//    // Entity -> Dto 변환 메서드
//    public static NoticeFileDto toBoardFileDto(BoardFileEntity boardFileEntity){
//        NoticeFileDto boardFileDto = new NoticeFileDto();
//
//        boardFileDto.setId(boardFileEntity.getId());
//        boardFileDto.setOriginalFilename(boardFileEntity.getOriginalFilename());
//        boardFileDto.setStoredFilename(boardFileEntity.getStoredFilename());
//        boardFileDto.setFileSize(boardFileEntity.getFileSize());
//
//        return boardFileDto;
//    }
//
//
//
//}
