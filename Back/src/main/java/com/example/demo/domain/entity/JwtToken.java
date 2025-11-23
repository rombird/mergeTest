package com.example.demo.domain.entity;

<<<<<<< HEAD

=======
>>>>>>> origin/막내
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
<<<<<<< HEAD
=======
import org.hibernate.annotations.CreationTimestamp;
>>>>>>> origin/막내

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
<<<<<<< HEAD
@Builder
@Entity
=======
@Entity
@Builder
>>>>>>> origin/막내
public class JwtToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
<<<<<<< HEAD

    @Column(name="accessToken",columnDefinition = "TEXT",nullable = false)
    private String accessToken;

    @Column(name="refreshToken",columnDefinition = "TEXT",nullable = false)
    private String refreshToken;

    @Column(name="username",nullable = false)
    private String username;

    @Column(name="createdAt",columnDefinition = "DATETIME",nullable = true)
    private LocalDateTime createdAt;

}
=======
    @Column(columnDefinition = "TEXT",nullable = false)
    private String accessToken;
    @Column(columnDefinition = "TEXT",nullable = false)
    private String refreshToken;
    @Column
    private String username;
    @Column
    private String auth; // "ROLE_USER,ROLE_ADMIN"
    @CreationTimestamp
    private LocalDateTime createdAt;
}
>>>>>>> origin/막내
