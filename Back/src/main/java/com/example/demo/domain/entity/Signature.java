package com.example.demo.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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
public class Signature {
    @Id
    @Column(name="signKey")
    private byte[] keyBytes;
    @Column(name="createAt")
    private LocalDate createAt;
<<<<<<< HEAD
}
=======
}
>>>>>>> origin/막내
