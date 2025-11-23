package com.example.demo.domain.entity;


<<<<<<< HEAD
import com.fasterxml.jackson.annotation.JsonFormat;
=======
>>>>>>> origin/막내
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Collate;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public class BaseEntity {

<<<<<<< HEAD

=======
>>>>>>> origin/막내
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdTime;

<<<<<<< HEAD

=======
>>>>>>> origin/막내
    @UpdateTimestamp
    @Column(insertable = false)
    private LocalDateTime updatedTime;
}
