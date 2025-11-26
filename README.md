# mergeTest

- security관련 오류 발생 시 auth.requestMatchers("/api/sales/summary").permitAll(); 삽입 시도 추천

```
apiController - SalesSummaryRestController/ config - DataInitializer/ domain>dto>SalesSummaryResponseDto/
entity - RawSalesData, SalesSummary/ repository - SalesSummaryRepository / Service - SalesSummaryService/
resources - csv파일 확인
```
