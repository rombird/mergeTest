package com.example.demo.domain.repository;

import com.example.demo.domain.entity.SalesSummary; // <--- 이 부분을 수정했습니다. (entity -> domain.entity)
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SalesSummaryRepository extends JpaRepository<SalesSummary, Long> {

    /**
     * 자치구 이름(guName)과 분기 코드(qtrCode)를 기준으로 요약 데이터를 조회합니다.
     * DB의 sales_gu_summary 테이블에 매핑됩니다.
     */
    @Query("SELECT s FROM SalesSummary s WHERE s.guName = :guName AND s.qtrCode = :qtrCode")
    Optional<SalesSummary> findByGuNameAndQtrCode(
            @Param("guName") String guName,
            @Param("qtrCode") String qtrCode
    );
}