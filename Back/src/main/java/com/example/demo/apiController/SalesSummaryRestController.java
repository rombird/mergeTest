package com.example.demo.apiController;

import com.example.demo.domain.dto.SalesSummaryResponseDto;
import com.example.demo.service.SalesSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000") // React 개발 서버 포트 허용
public class SalesSummaryRestController {

    private final SalesSummaryService salesSummaryService;

    /**
     * GET /api/sales/summary?guName=강남구&qtrCode=20191
     * 자치구와 분기를 기준으로 매출 요약 정보를 반환
     */
    @GetMapping("/summary")
    public SalesSummaryResponseDto getSalesSummary(
            @RequestParam("guName") String guName,
            @RequestParam("qtrCode") String qtrCode) {

        return salesSummaryService.getSummary(guName, qtrCode);
    }
}
