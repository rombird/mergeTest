package com.example.demo.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 애플리케이션 시작 시 (1) CSV Raw 데이터 로드, (2) 집계 쿼리 실행을 순차적으로 수행
 * - Raw 데이터는 CSV 파일의 모든 30개 컬럼을 그대로 DB에 저장
 * - 집계 시 Raw 테이블의 한글 컬럼명을 직접 사용하여 데이터를 추출하고,
 * Summary 테이블의 컬럼명도 한글명으로 정의하며, 'id' 필드에 값을 할당
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final ResourceLoader resourceLoader;

    private static final String CSV_FILE_PATH = "classpath:추정매출_자치구.csv";
    private static final String RAW_TABLE_NAME = "sales_gu_raw";
    private static final String SUMMARY_TABLE_NAME = "sales_gu_summary";
    private long currentId = 1; // RAW 데이터 삽입을 위한 임시 ID 카운터

    // CSV 헤더의 모든 컬럼 이름 (총 30개)
    private static final List<String> ALL_COLUMN_NAMES = Arrays.asList(
            "기준_년분기_코드", "자치구_코드", "자치구_코드_명", "서비스_업종_코드",
            "서비스_업종_코드_명", "당월_매출_금액", "당월_매출_건수", "주중_매출_금액",
            "주말_매출_금액", "월요일_매출_금액", "화요일_매출_금액", "수요일_매출_금액",
            "목요일_매출_금액", "금요일_매출_금액", "토요일_매출_금액", "일요일_매출_금액",
            "시간대_00~06_매출_금액", "시간대_06~11_매출_금액", "시간대_11~14_매출_금액",
            "시간대_14~17_매출_금액", "시간대_17~21_매출_금액", "시간대_21~24_매출_금액",
            "남성_매출_금액", "여성_매출_금액", "연령대_10_매출_금액", "연령대_20_매출_금액",
            "연령대_30_매출_금액", "연령대_40_매출_금액", "연령대_50_매출_금액", "연령대_60_이상_매출_금액"
    );
    private static final int NUM_COLUMNS = ALL_COLUMN_NAMES.size(); // 30

    public DataInitializer(JdbcTemplate jdbcTemplate, ResourceLoader resourceLoader) {
        this.jdbcTemplate = jdbcTemplate;
        this.resourceLoader = resourceLoader;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println("🚀 데이터 초기화 전체 프로세스 시작...");
        if (loadRawDataFromCsv()) {
            aggregateSalesData();
            System.out.println("🎉 데이터 초기화 전체 프로세스 성공적으로 완료.");
        } else {
            System.err.println("❌ RAW 데이터 로드 실패로 인해 집계 프로세스가 중단되었습니다. CSV 파일과 경로를 확인하십시오.");
        }
    }

    // ====================================================================
    // 1단계: CSV 파일 읽어 DB에 RAW 데이터 전체 삽입
    // ====================================================================
    private boolean loadRawDataFromCsv() {
        System.out.println("\n--- 1단계: RAW 데이터 전체 컬럼 로드 시작 ---");

        Resource resource = resourceLoader.getResource(CSV_FILE_PATH);
        if (!resource.exists()) {
            System.err.println("❌ 오류: CSV 파일을 찾을 수 없습니다. 파일을 'src/main/resources/' 위치에 두세요.");
            return false;
        }

        try {
            jdbcTemplate.execute("TRUNCATE TABLE " + RAW_TABLE_NAME);
            System.out.println("✅ " + RAW_TABLE_NAME + " 테이블 초기화 완료.");
        } catch (DataAccessException e) {
            System.err.println("⚠️ " + RAW_TABLE_NAME + " TRUNCATE 오류. 테이블 스키마 자동 생성 확인 필요.");
        }
        currentId = 1; // ID 카운터 초기화

        // 인코딩 재시도 로직 (UTF-8 -> CP949)
        try {
            System.out.println("✨ UTF-8로 CSV 파일 읽기를 시도합니다.");
            if (processCsvFile(resource, StandardCharsets.UTF_8)) return true;
        } catch (Exception e) {
            System.err.println("❌ UTF-8 파일 읽기 실패: " + e.getMessage());
        }

        try {
            System.out.println("✨ CP949(EUC-KR)로 CSV 파일 읽기를 시도합니다.");
            if (processCsvFile(resource, Charset.forName("CP949"))) return true;
        } catch (Exception e) {
            System.err.println("❌ CP949로도 CSV 파일 읽기 실패: " + e.getMessage());
            return false;
        }

        System.out.println("------------------------------------");
        return false;
    }

    // 전체 30개 컬럼에 대한 INSERT SQL 생성
    private String createInsertSql() {
        // `id` 컬럼을 포함한 전체 컬럼 리스트
        List<String> columns = new ArrayList<>();
        columns.add("id");
        columns.addAll(ALL_COLUMN_NAMES);

        String columnList = String.join("`, `", columns);
        String questionMarks = String.join(", ", java.util.Collections.nCopies(columns.size(), "?"));

        return String.format(
                "INSERT INTO %s (`%s`) VALUES (%s)",
                RAW_TABLE_NAME, columnList, questionMarks
        );
    }

    // CSV 데이터를 파싱하여 DB에 배치 삽입
    private boolean processCsvFile(Resource resource, Charset charset) throws Exception {
        List<Object[]> batchArgs = new ArrayList<>();
        int rowsProcessed = 0;

        String insertSql = createInsertSql();

        // 데이터 타입별 컬럼 인덱스 (0-based, id 제외)
        // 당월_매출_건수(Integer): 6
        // 나머지 매출 금액 컬럼(Double): 5, 7~29
        List<Integer> integerIndices = Arrays.asList(6);
        List<Integer> doubleIndices = Arrays.asList(5, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), charset))) {
            reader.readLine(); // 헤더 라인 스킵

            String line;
            int lineNumber = 1;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                // CSV 필드 파싱 (쉼표로 분리)
                String[] values = line.split(",");

                if (values.length >= NUM_COLUMNS) {
                    try {
                        // DB에 삽입할 Object 배열 (id + 30개 컬럼 = 31개)
                        Object[] rowData = new Object[NUM_COLUMNS + 1];
                        rowData[0] = currentId++; // 임시 ID 추가

                        // 30개 CSV 필드를 순서대로 Object 배열에 추가 (index + 1)
                        for (int i = 0; i < NUM_COLUMNS; i++) {
                            String value = values[i].trim().replaceAll("\"", "");

                            if (integerIndices.contains(i)) {
                                // 당월_매출_건수 (정수)
                                rowData[i + 1] = Integer.parseInt(value.replaceAll("[^0-9]", ""));
                            } else if (doubleIndices.contains(i)) {
                                // 매출 금액 (실수)
                                rowData[i + 1] = Double.parseDouble(value.replaceAll("[^0-9.]", ""));
                            } else {
                                // 문자열
                                rowData[i + 1] = value;
                            }
                        }

                        batchArgs.add(rowData);
                        rowsProcessed++;
                    } catch (NumberFormatException e) {
                        System.err.println(String.format("⚠️ [줄 %d - 인코딩: %s] 숫자 형식 오류 발생 (값: %s). 이 행은 건너뜁니다.", lineNumber, charset.name(), line));
                    } catch (Exception e) {
                        System.err.println(String.format("⚠️ [줄 %d - 인코딩: %s] 파싱 중 알 수 없는 오류 발생: %s", lineNumber, charset.name(), e.getMessage()));
                    }
                }

                if (batchArgs.size() >= 1000) { // 배치 크기 조정
                    jdbcTemplate.batchUpdate(insertSql, batchArgs);
                    batchArgs.clear();
                }
            }

            // 남은 데이터 배치 실행
            if (!batchArgs.isEmpty()) {
                jdbcTemplate.batchUpdate(insertSql, batchArgs);
            }

            if (rowsProcessed > 0) {
                System.out.println(String.format("✅ RAW 데이터 %d 건 %s 테이블에 삽입 완료 (인코딩: %s).", rowsProcessed, RAW_TABLE_NAME, charset.name()));
                return true;
            } else {
                System.err.println(String.format("❌ CSV 파일 내용 파싱 결과 유효 데이터가 0건입니다. 파일 포맷을 확인하세요. (인코딩: %s)", charset.name()));
                return false;
            }

        } catch (DataAccessException e) {
            System.err.println("❌ DB 삽입 중 오류 발생: " + e.getMessage());
            if (e.getMessage().contains("Data truncation")) {
                System.err.println("   - ⚠️ 원인 추정: DB 테이블의 컬럼 길이가 부족합니다. 엔티티 파일(RawSalesData.java)의 String 컬럼 크기를 늘려야 합니다.");
            }
            return false;
        }
    }


    // ====================================================================
    // 2단계: RAW 데이터를 기반으로 집계 테이블 생성 및 채우기 (한글 컬럼명 및 ID 사용)
    // ====================================================================
    private void aggregateSalesData() {
        System.out.println("\n--- 2단계: 집계 쿼리 실행 시작 (ID 포함) ---");

        try {
            jdbcTemplate.execute("TRUNCATE TABLE " + SUMMARY_TABLE_NAME);
            System.out.println("✅ " + SUMMARY_TABLE_NAME + " 테이블 초기화 완료.");
        } catch (DataAccessException e) {
            System.err.println("⚠️ TRUNCATE 오류. 테이블 스키마 자동 생성 확인 필요.");
        }


        String aggregationQuery =
                """
                INSERT INTO sales_gu_summary (
                    id,                       -- ID 필드 추가 (오류 해결)
                    기준_년분기_코드,          
                    자치구_코드_명,           
                    분기_총_매출_금액,        
                    전분기_대비_증감액,       
                    전년동분기_대비_증감액     
                )
                SELECT
                    -- 1. 집계된 결과에 순번을 매겨 ID로 사용
                    ROW_NUMBER() OVER (
                        ORDER BY A.`기준_년분기_코드` ASC, A.`자치구_코드_명` ASC
                    ) AS id,
                    A.`기준_년분기_코드`,
                    A.`자치구_코드_명`,
                    SUM(A.`당월_매출_금액`) AS `분기_총_매출_금액`,
                    
                    -- 2. 전분기 대비 증감액 계산
                    (
                        SUM(A.`당월_매출_금액`) - 
                        LAG(SUM(A.`당월_매출_금액`), 1) OVER (
                            PARTITION BY A.`자치구_코드_명`
                            ORDER BY A.`기준_년분기_코드` ASC
                        )
                    ) AS `전분기_대비_증감액`,
                    
                    -- 3. 전년 동분기 대비 증감액 계산
                    (
                        SUM(A.`당월_매출_금액`) - 
                        LAG(SUM(A.`당월_매출_금액`), 4) OVER (
                            PARTITION BY A.`자치구_코드_명`
                            ORDER BY A.`기준_년분기_코드` ASC
                        )
                    ) AS `전년동분기_대비_증감액`
                    
                FROM sales_gu_raw AS A
                WHERE
                    -- 외식업종 코드 필터링 예시 ('CS100001'에서 'CS100010'까지 외식 업종으로 가정)
                    A.`서비스_업종_코드` BETWEEN 'CS100001' AND 'CS100010'
                GROUP BY
                    A.`기준_년분기_코드`, A.`자치구_코드_명`
                -- 순서 보장
                ORDER BY A.`기준_년분기_코드` ASC, A.`자치구_코드_명` ASC;
                """;

        try {
            int rows = jdbcTemplate.update(aggregationQuery);
            System.out.println("✅ 데이터 집계 및 삽입 쿼리 실행 완료. 삽입된 행 수: " + rows);

        } catch (DataAccessException e) {
            System.err.println("❌ 데이터 집계 쿼리 실행 중 오류 발생: " + e.getMessage());
            System.err.println("   - 원인: RAW 데이터 로드 실패, DB 연결 오류, 혹은 MySQL 버전(8.0 이상) 미지원.");
        }

        System.out.println("------------------------------------");
    }
}