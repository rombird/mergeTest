package com.example.demo.domain.dto;


import com.example.demo.domain.entity.BoardEntity;
<<<<<<< HEAD
=======
<<<<<<< HEAD
>>>>>>> parent of e8b61b6 (Delete Back directory)
import com.example.demo.domain.entity.BoardFileEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
<<<<<<< HEAD
=======
=======
import lombok.*;

import java.time.LocalDateTime;

@Data
>>>>>>> origin/막내
>>>>>>> parent of e8b61b6 (Delete Back directory)
@NoArgsConstructor
@AllArgsConstructor
public class BoardDto {
    private Long id;
<<<<<<< HEAD
=======
<<<<<<< HEAD
>>>>>>> parent of e8b61b6 (Delete Back directory)
    private String boardWriter;
    private String boardPass;
    private String boardTitle;
    private String boardContents;
    private int boardHits;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime boardCreateTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime boardUpdateTime;

    // 파일 업로드 관련
    private List<MultipartFile> fileUpload; // 파일 담는 용도
    private List<BoardFileDto> boardFileDtoList;

    private int fileAttached;   // 파일 첨부 여부(첨부 1, 미첨부 0)


    public BoardDto(Long id, String boardWriter, String boardTitle, int boardHits, LocalDateTime boardCreateTime) {
        this.id = id;
        this.boardWriter = boardWriter;
        this.boardTitle = boardTitle;
        this.boardHits = boardHits;
        this.boardCreateTime = boardCreateTime;

    }


    public static BoardDto toBoardDto(BoardEntity boardEntity){
        BoardDto boardDto = new BoardDto();

        // 텍스트 영역
        boardDto.setId(boardEntity.getId());
        boardDto.setBoardWriter(boardEntity.getBoardWriter());
        boardDto.setBoardPass(boardEntity.getBoardPass());
<<<<<<< HEAD
=======
=======
//    private String boardWriter;
//    private String boardPass;
    private String boardTitle;
    private String boardContents;
    private int boardHits;
    private LocalDateTime boardCreateTime;
    private LocalDateTime boardUpdateTime;

    public static BoardDto toBoardDto(BoardEntity boardEntity){
        BoardDto boardDto = new BoardDto();

        boardDto.setId(boardEntity.getId());
>>>>>>> origin/막내
>>>>>>> parent of e8b61b6 (Delete Back directory)
        boardDto.setBoardContents(boardEntity.getBoardContents());
        boardDto.setBoardHits(boardEntity.getBoardHits());
        boardDto.setBoardTitle(boardEntity.getBoardTitle());
        boardDto.setBoardCreateTime(boardEntity.getCreatedTime());
        boardDto.setBoardUpdateTime(boardEntity.getUpdatedTime());

<<<<<<< HEAD
=======
<<<<<<< HEAD
>>>>>>> parent of e8b61b6 (Delete Back directory)
        // 첨부파일 유무 체크
        if (boardEntity.getFileAttached() == 0) {
            boardDto.setFileAttached(0);
        } else {
            // 첨부파일이 있으면
            boardDto.setFileAttached(1);

            // 파일 정보 변환 (Entity -> DTO List)
            List<BoardFileEntity> fileEntities = boardEntity.getBoardFileEntityList();
            List<BoardFileDto> fileDtoList = new ArrayList<>();

            for (BoardFileEntity fileEntity : fileEntities) {
                // BoardFileDto 생성하고 리스트 추가
                fileDtoList.add(BoardFileDto.toBoardFileDto(fileEntity));
            }
            boardDto.setBoardFileDtoList(fileDtoList);
        }

<<<<<<< HEAD
=======
=======
>>>>>>> origin/막내
>>>>>>> parent of e8b61b6 (Delete Back directory)
        return boardDto;
    }

}
