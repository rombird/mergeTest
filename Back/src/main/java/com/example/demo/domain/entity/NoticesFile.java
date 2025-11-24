package com.example.demo.domain.entity;

import com.example.demo.domain.dto.NoticesFileDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import lombok.NoArgsConstructor;


@Entity
@Table(name = "notices_file")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoticesFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 클라이언트가 업로드한 파일 이름 (예: cat.jpg)
    @Column(nullable = false)
    private String originalFileName;


    // 서버에 실제로 저장된 중복 방지 파일 이름 (예: UUID_cat.jpg)
    @Column(nullable = false)
    private String storedFileName;

    @Column(nullable = false)
    private String filePath; // 파일 저장 경로

    @Column(nullable = false)
    private Long fileSize; // 파일 크기 (바이트)

    @Column(nullable = true) //  UX 개선을 위해 추가된 MIME Type 필드
    private String mimeType; // 파일의 타입 정보 (예: image/jpeg, application/pdf)

    // N:1 관계 설정: 여러 파일이 하나의 공지사항에 속함
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notices_id", nullable = false)
    private NoticesEntity notices;

    // FileService에서 사용될 수 있도록 NoticesEntity를 연결하는 메서드
    public void setNotices(NoticesEntity notices) {
        this.notices = notices;

        // 양방향 관계 설정: NoticesEntity의 noticesFiles 리스트에 자신을 추가
        // 단, 이미 리스트에 포함되어 있는지 확인하는 것이 좋습니다.
        if (notices.getNoticesFiles() != null && !notices.getNoticesFiles().contains(this)) {
            notices.getNoticesFiles().add(this);
        }
    }

    // DTO 변환 (선택 사항이지만, 일관성을 위해 추가)
    public NoticesFileDto toDto() {
        return NoticesFileDto.toDto(this);
    }
}