package com.example.demo.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter; // JPA/Hibernate 구성을 위해 Setter는 유지
import lombok.experimental.SuperBuilder;

/**
 * 집계된 분기별 매출 요약 정보를 담는 엔티티.
 * 컬럼명은 RawSalesData와 유사하게 한글명을 사용하여 매핑하며, 기본 키(id)를 포함
 */
@Entity
@Table(name = "sales_gu_summary")
@Getter
@Setter // JPA/Hibernate 구성을 위해 Setter는 유지
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SalesSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // DB가 ID를 자동 생성하도록 설정 (JDBC 쿼리에서는 ROW_NUMBER()로 대체됨)
    private Long id;

    // RawData 컬럼명 그대로 사용 (기준 컬럼)
    @Column(name = "기준_년분기_코드", nullable = false)
    private String qtrCode;

    // RawData 컬럼명 그대로 사용 (기준 컬럼)
    @Column(name = "자치구_코드_명", nullable = false)
    private String guName;

    // SUM을 통해 계산된 컬럼명 (구분을 위해 새로운 한글명 사용)
    @Column(name = "분기_총_매출_금액", nullable = false)
    private Double quarterlyTotalSales;

    // 계산된 컬럼명 (구분을 위해 새로운 한글명 사용)
    @Column(name = "전분기_대비_증감액")
    private Double qoqChange;

    // 계산된 컬럼명 (구분을 위해 새로운 한글명 사용)
    @Column(name = "전년동분기_대비_증감액")
    private Double yoyChange;
}