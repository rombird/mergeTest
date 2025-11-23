package com.example.demo.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.autoconfigure.web.WebProperties;


// ====================================================================
// 부모 - board_table, 자식 - board_file_table의 정리
// 외래키는 해쉬값 붙여서 자동생성, 부모가 없어지면 자식도 없어짐, cascade도 함
// ====================================================================
//create table board_table
//        (
//                id             bigint auto_increment primary key,
//                created_time   datetime     null,
//                updated_time   datetime     null,
//                board_contents varchar(500) null,
//board_hits     int          null,
//board_pass     varchar(255) null,
//board_title    varchar(255) null,
//board_writer   varchar(20)  not null,
//file_attached  int          null
//        );
//
//create table board_file_table
//        (
//                id                 bigint auto_increment primary key,
//                created_time       datetime     null,
//                updated_time       datetime     null,
//                original_file_name varchar(255) null,
//stored_file_name   varchar(255) null,
//board_id           bigint       null,
//constraint FKcfxqly70ddd02xbou0jxgh4o3
//foreign key (board_id) references board_table (id) on delete cascade
//);



@Entity
@Getter
@ToString
@Setter
@Table(name = "board_file_table")
public class BoardFileEntity extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_file_name")
    private String originalFilename;

    @Column(name = "stored_file_name")
    private String storedFilename;

    // N:1관계(board_file_table입장에서)
    @ManyToOne(fetch = FetchType.LAZY) // Eager -> 부모테이블 조회시 자식 테이블도 같이 다 조회, Lazy -> 부모테이블 조회 시 필요한 상황에만 호출
    @JoinColumn(name = "board_id")  // 만들어질 컬럼 이름
    @ToString.Exclude
    private BoardEntity boardEntity;    // 부모 엔티티 타입으로 받아야함
                                        // 참조값을 PK값이 아닌 entity로 넘김

    public static BoardFileEntity toBoardFileEntity
                                                    (BoardEntity boardEntity,
                                                     String originalFilename,
                                                    String storedFilename)
    {
        BoardFileEntity boardFileEntity = new BoardFileEntity();
        boardFileEntity.setOriginalFilename(originalFilename);
        boardFileEntity.setStoredFilename(storedFilename);
        boardFileEntity.setBoardEntity(boardEntity);
        return boardFileEntity;
    }

}

