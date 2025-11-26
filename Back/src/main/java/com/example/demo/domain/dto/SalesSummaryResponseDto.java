package com.example.demo.domain.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 프론트엔드 (React)에 응답할 최종 데이터를 담는 DTO (Data Transfer Object)
 * DB 엔티티(SalesSummary)의 데이터를 받아 화면 표시에 필요한 형태로 가공하여 제공
 */
@Data // Getter, Setter, Builder 등을 포함 (기존 @Getter 대신 @Data 사용)
@Builder // Builder 패턴 사용
@NoArgsConstructor // Lombok Builder 사용 시 필요
@AllArgsConstructor // Lombok Builder 사용 시 필요
public class SalesSummaryResponseDto {

    // 식별자 필드 복원 (서비스 코드에서 사용됨)
    private String guName; // 자치구 이름
    private String qtrCode; // 기준 년분기 코드

    // 타입 변경: 소수점 이하 데이터 손실 방지를 위해 long 대신 Double 사용
    private Double monthlyAverageSales; // 월평균_매출액 (분기 총합 / 3)
    private Double qoqChange;           // 전분기_대비_증감액
    private Double yoyChange;           // 전년_동분기_대비_증감액

    // 추가: 하단에 표시할 이전 분기 절대값들
    private Double previousQtrSales;    // 전분기 월평균 매출액
    private Double previousYearSales;   // 전년 동분기 월평균 매출액
}