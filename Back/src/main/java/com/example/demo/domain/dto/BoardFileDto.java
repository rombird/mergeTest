package com.example.demo.domain.dto;

import com.example.demo.domain.entity.BoardFileEntity;
import lombok.*;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BoardFileDto {
    private Long id;
    private String originalFilename;
    private String storedFilename;
<<<<<<< HEAD
    private Long fileSize;
=======
>>>>>>> parent of e8b61b6 (Delete Back directory)


    // Entity -> Dto 변환 메서드
    public static BoardFileDto toBoardFileDto(BoardFileEntity boardFileEntity){
        BoardFileDto boardFileDto = new BoardFileDto();

        boardFileDto.setId(boardFileEntity.getId());
        boardFileDto.setOriginalFilename(boardFileEntity.getOriginalFilename());
        boardFileDto.setStoredFilename(boardFileEntity.getStoredFilename());
<<<<<<< HEAD
        boardFileDto.setFileSize(boardFileEntity.getFileSize());
=======
>>>>>>> parent of e8b61b6 (Delete Back directory)

        return boardFileDto;
    }



}
