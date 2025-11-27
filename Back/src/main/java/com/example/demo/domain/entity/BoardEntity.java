package com.example.demo.domain.entity;

import com.example.demo.domain.dto.BoardDto;
import com.example.demo.domain.dto.CommentDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

// Board DB의 테이블 역할을 하는 클래스
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "board_table")
public class BoardEntity extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동으로 ID 생성
    private Long id;

    @Column(name = "boardWriter", nullable = false)
    private String boardWriter;

    @Column(name = "bardPass", nullable = false)
    private String boardPass;

    @Column(name = "boardTitle", nullable = false)
    private String boardTitle;

    @Column(name = "boardContents", nullable = false, length = 500)
    private String boardContents;

    @Column(name = "boardHits")
    private int boardHits;

    @Column(name = "fileAttached")
    private Integer fileAttached; // 1 or 0


    // 1 대 N
    // mappedBy 차례대로 어떤 것(boardEntity)과 매칭되느냐?, cascade 하고, onDelete 하나, fetch 속성은 Lazy
    @OneToMany(mappedBy = "boardEntity", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<BoardFileEntity> boardFileEntityList = new ArrayList<>();

//    public void setBoardFileEntityList(List<BoardFileEntity> boardFileEntityList) {
//        this.boardFileEntityList = boardFileEntityList;
//    }

    @OneToMany(mappedBy = "boardEntity", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CommentEntity> commentEntityList = new ArrayList<>();

    public static BoardEntity toSaveEntity(BoardDto boardDto){
        BoardEntity boardEntity = new BoardEntity();

        boardEntity.setBoardWriter(boardDto.getBoardWriter());
        boardEntity.setBoardPass(boardDto.getBoardPass());
        boardEntity.setBoardHits(0);
        boardEntity.setBoardTitle(boardDto.getBoardTitle());
        boardEntity.setBoardContents(boardDto.getBoardContents());
        boardEntity.setFileAttached(0); // 파일 없음,

        return boardEntity;
    }

    // JPA는 id값의 유무에 따라 update인지 insert인지 결정한다
    public static BoardEntity toUpdateEntity(BoardDto boardDto){
        BoardEntity boardEntity = new BoardEntity();

        boardEntity.setId(boardDto.getId());
        boardEntity.setBoardWriter(boardDto.getBoardWriter());
        boardEntity.setBoardPass(boardDto.getBoardPass());
        boardEntity.setBoardHits(boardDto.getBoardHits());
        boardEntity.setBoardTitle(boardDto.getBoardTitle());
        boardEntity.setBoardContents(boardDto.getBoardContents());

        return boardEntity;
    }

    public static BoardEntity toSaveFileEntity(BoardDto boardDto) {
        BoardEntity boardEntity = new BoardEntity();

        boardEntity.setBoardWriter(boardDto.getBoardWriter());
        boardEntity.setBoardPass(boardDto.getBoardPass());
        boardEntity.setBoardHits(0);
        boardEntity.setBoardTitle(boardDto.getBoardTitle());
        boardEntity.setBoardContents(boardDto.getBoardContents());
        boardEntity.setFileAttached(1); // 파일 있음,

        return boardEntity;
    }

    // 게시글 텍스트 내용을 업데이트하는 메서드
    public void updateText(String boardTitle, String boardContents){
        this.boardTitle = boardTitle;
        this.boardContents = boardContents;
    }

    // 게시글 파일 첨부 상태를 업데이트하는 메서드 추가
    public void updateFileAttached(Integer fileAttached){
        this.fileAttached = fileAttached;
    }

}
