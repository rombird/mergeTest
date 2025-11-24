package com.example.demo.domain.entity;


import com.example.demo.domain.dto.NoticesDto;
import com.example.demo.domain.dto.NoticesFileDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


//공지사항 TBL 역할
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "notices_table")
@Builder
@EntityListeners(AuditingEntityListener.class)
public class NoticesEntity{ //RDBMS TB 표현, JPA가 이 클래스를 테이블로 인식하여 관리가능

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String author;

    @Column(nullable=false)
    private String noticesTitle;

    @Lob //긴 텍스트 저장
    @Column(nullable=false)
    private String noticesContents;

    //조회수
    @Column(nullable= true)
    private int noticesViews;

    @CreatedDate
    @Column(updatable = false) // 최초 생성 후 업데이트 방지
    private LocalDateTime createdTime;

    @LastModifiedDate
    private LocalDateTime updatedTime;

    //파일 업로드
    //1:N 관계 설정: 하나의 공지사항은 여러 파일 첨부 가능
    @OneToMany(mappedBy = "notices", cascade = CascadeType.ALL, orphanRemoval = true)
    // cascade = CascadeType.ALL: 공지사항 삭제 시 첨부된 파일 정보(DB)도 함께 삭제
    // orphanRemoval = true: 컬렉션에서 파일 제거 시 DB에서도 자동 삭제
    @Builder.Default // 빌더 패턴 사용 시 초기화되도록 설정
    private List<NoticesFile> noticesFiles = new ArrayList<>();


    //Dto -> Entity 변환
    public static NoticesEntity fromDto(NoticesDto noticesDto) {
        return NoticesEntity.builder()
                .author(noticesDto.getAuthor())
                .noticesTitle(noticesDto.getNoticesTitle())
                .noticesContents(noticesDto.getNoticesContents())
                .noticesViews(0) //새 공지 작성 시 조회수는 0 초기화
                .build();
    }

    // title, Contents만 외부에서 변경가능하도록(조회수, 생성일은 X)
    public void updateFromDto(NoticesDto dto){
        this.noticesTitle = dto.getNoticesTitle();
        this.noticesContents = dto.getNoticesContents();
        //updatedTime은 @UpdateTimestamp를 통해 자동 갱신
    }

    // Entity -> DTO 변환 (toDto()는 NoticesService에서 사용됩
    public NoticesDto toDto() {

        // 1. NoticesFile Entity 목록을 NoticesFile DTO 목록으로 변환
        List<NoticesFileDto> fileDtoList = (this.noticesFiles != null && !this.noticesFiles.isEmpty()) ?
                this.noticesFiles.stream()
                        // NoticesFileDto의 toDto() 메서드 사용
                        .map(NoticesFileDto::toDto)
                        .collect(Collectors.toList()) :
                null; // 파일이 없으면 null 반환

        return NoticesDto.builder()
                .id(this.id)
                .author(this.author)
                .noticesTitle(this.noticesTitle)
                .noticesContents(this.noticesContents)
                .noticesView(this.noticesViews)
                // BaseEntity의 필드
                .createdTime(this.getCreatedTime())
                .updatedTime(this.getUpdatedTime())
//              변환된 파일 DTO 리스트를 DTO의 'attachedFiles' 필드에 삽입
                .attachedFiles(fileDtoList)
                .build();
    }



    }
