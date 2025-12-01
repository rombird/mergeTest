package com.example.demo.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@ToString
@Setter
@Table(name = "notice_file_table")
@NoArgsConstructor
@AllArgsConstructor
public class NoticeFileEntity extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "notice_original_file_name")
    private String noticeOriginalFilename;

    @Column(name = "notice_stored_file_name")
    private String noticeStoredFilename;

    @Column(name = "notice_file_size") // 파일 크기
    private Long noticeFileSize;

    // N:1관계(board_file_table입장에서)
    @ManyToOne(fetch = FetchType.LAZY) // Eager -> 부모테이블 조회시 자식 테이블도 같이 다 조회, Lazy -> 부모테이블 조회 시 필요한 상황에만 호출
    @JoinColumn(name = "notice_id")  // 만들어질 컬럼 이름
    @ToString.Exclude
    private NoticeEntity noticeEntity;    // 부모 엔티티 타입으로 받아야함
                                        // 참조값을 PK값이 아닌 entity로 넘김

    public static NoticeFileEntity toNoticeFileEntity
                                                    (NoticeEntity noticeEntity,
                                                     String noticeOriginalFilename,
                                                    String noticeStoredFilename,
                                                    Long noticeFileSize)
    {
        NoticeFileEntity noticeFileEntity = new NoticeFileEntity();
        noticeFileEntity.setNoticeOriginalFilename(noticeOriginalFilename);
        noticeFileEntity.setNoticeStoredFilename(noticeStoredFilename);
        noticeFileEntity.setNoticeEntity(noticeEntity);
        noticeFileEntity.setNoticeFileSize(noticeFileSize);

        return noticeFileEntity;
    }

}

