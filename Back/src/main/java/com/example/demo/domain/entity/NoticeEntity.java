package com.example.demo.domain.entity;

import com.example.demo.domain.dto.BoardDto;
import com.example.demo.domain.dto.NoticeDto;
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
@Table(name = "notice_table")
public class NoticeEntity extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동으로 ID 생성
    private Long id;

    @Column(name = "noticeWriter", nullable = false)
    private String noticeWriter;

    @Column(name = "noticeTitle", nullable = false)
    private String noticeTitle;

    @Column(name = "noticeContents", nullable = false, length = 500)
    private String noticeContents;

    @Column(name = "noticeHits")
    private int noticeHits;

    @Column(name = "noticeFileAttached")
    private Integer noticeFileAttached; // 1 or 0


    // 1 대 N
    // mappedBy 차례대로 어떤 것(boardEntity)과 매칭되느냐?, cascade 하고, onDelete 하나, fetch 속성은 Lazy
    @OneToMany(mappedBy = "noticeEntity", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<NoticeFileEntity> NoticeFileEntityList = new ArrayList<>();

//    public void setBoardFileEntityList(List<BoardFileEntity> boardFileEntityList) {
//        this.boardFileEntityList = boardFileEntityList;
//    }


    public static NoticeEntity toSaveEntity(NoticeDto noticeDto){
        NoticeEntity noticeEntity = new NoticeEntity();

        noticeEntity.setNoticeWriter(noticeDto.getNoticeWriter());
        noticeEntity.setNoticeHits(0);
        noticeEntity.setNoticeTitle(noticeDto.getNoticeTitle());
        noticeEntity.setNoticeContents(noticeDto.getNoticeContents());
        noticeEntity.setNoticeFileAttached(0); // 파일 없음,

        return noticeEntity;
    }

    // JPA는 id값의 유무에 따라 update인지 insert인지 결정한다 -> update 시 필요함
    public static NoticeEntity toUpdateEntity(NoticeDto noticeDto){
        NoticeEntity noticeEntity = new NoticeEntity();

        noticeEntity.setId(noticeDto.getId());
        noticeEntity.setNoticeWriter(noticeDto.getNoticeWriter());
        noticeEntity.setNoticeHits(noticeDto.getNoticeHits());
        noticeEntity.setNoticeTitle(noticeDto.getNoticeTitle());
        noticeEntity.setNoticeContents(noticeDto.getNoticeContents());

        return noticeEntity;
    }

    public static NoticeEntity toSaveFileEntity(NoticeDto noticeDto) {
        NoticeEntity noticeEntity = new NoticeEntity();

        noticeEntity.setNoticeWriter(noticeDto.getNoticeWriter());
        noticeEntity.setNoticeHits(0);
        noticeEntity.setNoticeTitle(noticeDto.getNoticeTitle());
        noticeEntity.setNoticeContents(noticeDto.getNoticeContents());
        noticeEntity.setNoticeFileAttached(1); // 파일 있음,

        return noticeEntity;
    }

    // 게시글 텍스트 내용을 업데이트하는 메서드
    public void updateText(String noticeTitle, String noticeContents){
        this.noticeTitle = noticeTitle;
        this.noticeContents = noticeContents;
    }

    // 게시글 파일 첨부 상태를 업데이트하는 메서드 추가
    public void updateFileAttached(Integer fileAttached){
        this.noticeFileAttached = noticeFileAttached;
    }

}
