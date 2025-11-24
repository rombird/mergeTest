package com.example.demo.domain.entity;


<<<<<<< HEAD
import com.fasterxml.jackson.annotation.JsonFormat;
=======
<<<<<<< HEAD
import com.fasterxml.jackson.annotation.JsonFormat;
=======
>>>>>>> origin/막내
>>>>>>> parent of e8b61b6 (Delete Back directory)
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
<<<<<<< HEAD

=======
>>>>>>> origin/막내
>>>>>>> parent of e8b61b6 (Delete Back directory)
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdTime;

<<<<<<< HEAD

=======
<<<<<<< HEAD

=======
>>>>>>> origin/막내
>>>>>>> parent of e8b61b6 (Delete Back directory)
    @UpdateTimestamp
    @Column(insertable = false)
    private LocalDateTime updatedTime;
}
