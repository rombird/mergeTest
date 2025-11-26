package com.example.demo.service;

import com.example.demo.domain.dto.SalesSummaryResponseDto;
import com.example.demo.domain.entity.SalesSummary;
import com.example.demo.repository.SalesSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class SalesSummaryService {

    private final SalesSummaryRepository salesSummaryRepository;

    public SalesSummaryResponseDto getSummary(String guName, String qtrCode) {

        // 1. 현재 분기 데이터 조회
        SalesSummary entity = salesSummaryRepository.findByGuNameAndQtrCode(guName, qtrCode)
                .orElseThrow(() -> new NoSuchElementException(
                        "Sales summary data not found for guName: " + guName + " and qtrCode: " + qtrCode)
                );

        // 2. 전분기 코드 계산 및 데이터 조회
        String previousQtrCode = calculatePreviousQtrCode(qtrCode);
        Double previousQtrSales = null;
        try {
            SalesSummary previousQtr = salesSummaryRepository
                    .findByGuNameAndQtrCode(guName, previousQtrCode)
                    .orElse(null);
            if (previousQtr != null) {
                previousQtrSales = previousQtr.getQuarterlyTotalSales() / 3.0;
            }
        } catch (Exception e) {
            // 데이터가 없으면 null로 유지
        }

        // 3. 전년 동분기 코드 계산 및 데이터 조회
        String previousYearQtrCode = calculatePreviousYearQtrCode(qtrCode);
        Double previousYearSales = null;
        try {
            SalesSummary previousYearQtr = salesSummaryRepository
                    .findByGuNameAndQtrCode(guName, previousYearQtrCode)
                    .orElse(null);
            if (previousYearQtr != null) {
                previousYearSales = previousYearQtr.getQuarterlyTotalSales() / 3.0;
            }
        } catch (Exception e) {
            // 데이터가 없으면 null로 유지
        }

        // 4. DTO 생성 - 모든 값을 /3.0으로 월평균 계산
        SalesSummaryResponseDto dto = SalesSummaryResponseDto.builder()
                .guName(entity.getGuName())
                .qtrCode(entity.getQtrCode())
                .monthlyAverageSales(entity.getQuarterlyTotalSales() / 3.0)
                .qoqChange(entity.getQoqChange() / 3.0)  // 증감액도 /3.0
                .yoyChange(entity.getYoyChange() / 3.0)  // 증감액도 /3.0
                .previousQtrSales(previousQtrSales)
                .previousYearSales(previousYearSales)
                .build();

        return dto;
    }

    /**
     * 전분기 코드 계산 (예: 20252 -> 20251, 20251 -> 20244)
     */
    private String calculatePreviousQtrCode(String qtrCode) {
        if (qtrCode == null || qtrCode.length() != 5) {
            throw new IllegalArgumentException("Invalid qtrCode format: " + qtrCode);
        }

        int year = Integer.parseInt(qtrCode.substring(0, 4));
        int quarter = Integer.parseInt(qtrCode.substring(4, 5));

        if (quarter == 1) {
            return String.format("%d4", year - 1);
        } else {
            return String.format("%d%d", year, quarter - 1);
        }
    }

    /**
     * 전년 동분기 코드 계산 (예: 20252 -> 20242)
     */
    private String calculatePreviousYearQtrCode(String qtrCode) {
        if (qtrCode == null || qtrCode.length() != 5) {
            throw new IllegalArgumentException("Invalid qtrCode format: " + qtrCode);
        }

        int year = Integer.parseInt(qtrCode.substring(0, 4));
        String quarter = qtrCode.substring(4, 5);

        return String.format("%d%s", year - 1, quarter);
    }
}