package com.example.demo.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * 원본 CSV 파일의 모든 컬럼 (30개)을 그대로 담기 위한 엔티티 (RAW 데이터).
 * 컬럼명은 CSV 헤더의 한글명을 그대로 사용
 */
@Entity
@Table(name = "sales_gu_raw")
@Data
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RawSalesData {

    // JPA를 위한 기본 키 (DataInitializer에서 임시 ID로 사용)
    @Id
    @Column(name = "id")
    private Long id;

    // 실제 CSV 파일의 컬럼명과 순서를 반영하여 전체 30개 컬럼을 정의
    // [0] 기준_년분기_코드
    @Column(name = "기준_년분기_코드", nullable = false)
    private String qtrCode;

    // [1] 자치구_코드
    @Column(name = "자치구_코드", nullable = false)
    private String guCode;

    // [2] 자치구_코드_명
    @Column(name = "자치구_코드_명", nullable = false)
    private String guName;

    // [3] 서비스_업종_코드
    @Column(name = "서비스_업종_코드", nullable = false)
    private String serviceCode;

    // [4] 서비스_업종_코드_명
    @Column(name = "서비스_업종_코드_명", nullable = false)
    private String serviceName;

    // [5] 당월_매출_금액 (Double)
    @Column(name = "당월_매출_금액", nullable = false)
    private Double monthlySalesAmount;

    // [6] 당월_매출_건수 (Integer)
    @Column(name = "당월_매출_건수", nullable = false)
    private Integer monthlySalesCount;

    // --- 주중/주말 매출 금액 (Double) ---
    // [7] 주중_매출_금액
    @Column(name = "주중_매출_금액", nullable = false)
    private Double weekdaySalesAmount;

    // [8] 주말_매출_금액
    @Column(name = "주말_매출_금액", nullable = false)
    private Double weekendSalesAmount;

    // --- 요일별 매출 금액 (Double) ---
    // [9] 월요일_매출_금액
    @Column(name = "월요일_매출_금액", nullable = false)
    private Double monSalesAmount;

    // [10] 화요일_매출_금액
    @Column(name = "화요일_매출_금액", nullable = false)
    private Double tueSalesAmount;

    // [11] 수요일_매출_금액
    @Column(name = "수요일_매출_금액", nullable = false)
    private Double wedSalesAmount;

    // [12] 목요일_매출_금액
    @Column(name = "목요일_매출_금액", nullable = false)
    private Double thuSalesAmount;

    // [13] 금요일_매출_금액
    @Column(name = "금요일_매출_금액", nullable = false)
    private Double friSalesAmount;

    // [14] 토요일_매출_금액
    @Column(name = "토요일_매출_금액", nullable = false)
    private Double satSalesAmount;

    // [15] 일요일_매출_금액
    @Column(name = "일요일_매출_금액", nullable = false)
    private Double sunSalesAmount;

    // --- 시간대별 매출 금액 (Double) ---
    // [16] 시간대_00~06_매출_금액
    @Column(name = "시간대_00~06_매출_금액", nullable = false)
    private Double time0006SalesAmount;

    // [17] 시간대_06~11_매출_금액
    @Column(name = "시간대_06~11_매출_금액", nullable = false)
    private Double time0611SalesAmount;

    // [18] 시간대_11~14_매출_금액
    @Column(name = "시간대_11~14_매출_금액", nullable = false)
    private Double time1114SalesAmount;

    // [19] 시간대_14~17_매출_금액
    @Column(name = "시간대_14~17_매출_금액", nullable = false)
    private Double time1417SalesAmount;

    // [20] 시간대_17~21_매출_금액
    @Column(name = "시간대_17~21_매출_금액", nullable = false)
    private Double time1721SalesAmount;

    // [21] 시간대_21~24_매출_금액
    @Column(name = "시간대_21~24_매출_금액", nullable = false)
    private Double time2124SalesAmount;

    // --- 성별 매출 금액 (Double) ---
    // [22] 남성_매출_금액
    @Column(name = "남성_매출_금액", nullable = false)
    private Double maleSalesAmount;

    // [23] 여성_매출_금액
    @Column(name = "여성_매출_금액", nullable = false)
    private Double femaleSalesAmount;

    // --- 연령대별 매출 금액 (Double) ---
    // [24] 연령대_10_매출_금액
    @Column(name = "연령대_10_매출_금액", nullable = false)
    private Double age10SalesAmount;

    // [25] 연령대_20_매출_금액
    @Column(name = "연령대_20_매출_금액", nullable = false)
    private Double age20SalesAmount;

    // [26] 연령대_30_매출_금액
    @Column(name = "연령대_30_매출_금액", nullable = false)
    private Double age30SalesAmount;

    // [27] 연령대_40_매출_금액
    @Column(name = "연령대_40_매출_금액", nullable = false)
    private Double age40SalesAmount;

    // [28] 연령대_50_매출_금액
    @Column(name = "연령대_50_매출_금액", nullable = false)
    private Double age50SalesAmount;

    // [29] 연령대_60_이상_매출_금액
    @Column(name = "연령대_60_이상_매출_금액", nullable = false)
    private Double age60SalesAmount;

    // 참고: CSV 파일 헤더에 없지만, 스니펫 마지막에 보이는 '주중_매출_건수' 등은 일단 제외하고,
    // 헤더에 명확히 보이는 30개 컬럼까지만 매핑했습니다. 필요하면 추가하세요.
}