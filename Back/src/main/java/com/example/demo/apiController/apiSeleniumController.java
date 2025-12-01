package com.example.demo.apiController;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/crawl")
public class apiSeleniumController {

    // 네이버 크롤링
    @GetMapping("/naver")
    public String crawlNaverSearch() {
        WebDriver driver = null;
        String resultMessage;

        // 1. WebDriverManager 설정 및 Headless 옵션 설정
        // 이 과정은 서버 환경에서 브라우저 드라이버를 쉽게 사용할 수 있게 합니다.
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
//        options.addArguments("--headless");             // 필수: GUI 없이 실행
//        options.addArguments("--no-sandbox");           // 리눅스 환경에서 권장
//        options.addArguments("--disable-dev-shm-usage"); // 메모리 관련 최적화
//        options.addArguments("--disable-gpu");
//        options.addArguments("--window-size=1920,1080");

        try {
            // 2. WebDriver 인스턴스 생성 (옵션 적용)
            driver = new ChromeDriver(options);

            // 3. 네이버 접속
            driver.get("https://www.naver.com/");

            // 4. 검색창 요소 찾기 및 검색어 입력
            WebElement searchBox = driver.findElement(By.id("query"));
            String searchKeyword = "경북 여행";

            // 5. 검색어 입력
            searchBox.sendKeys(searchKeyword);
            log.info("검색어 입력 완료: ", searchKeyword );

            // 6 검색 버튼 클릭
            WebElement searchButton = driver.findElement(By.cssSelector("button.btn_search")); // 검색 버튼의 css선택자 지정
            searchButton.click();   // 클릭되면 좋겠다

            // Enter로 처리하고 싶으면 .submit()도 가능한가봄

            // 7 검색결과 받아오기
            resultMessage = "네이버 검색완료. 현재 페이지 타이틀: " + driver.getTitle();
            log.info(resultMessage);







            // ====================================
            // 인스타처럼 로그인 폼안에 input이 여러개 있는 경우
            // ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓

//            // 4. 검색창 요소 찾기 및 검색어 입력
//            // 네이버의 검색창 요소는 일반적으로 'query'라는 name을 가집니다.
//            WebElement loginForm = driver.findElement(By.id("loginform"));
//            // 2. 부모 요소 내부에서 모든 'input' 태그를 찾음
//            List<WebElement> inputElements = loginForm.findElements(By.tagName("input"));
//
//            // 3. 찾은 input 요소들을 순회하며 작업 수행 (예: 속성 출력)
//            for (WebElement input : inputElements) {
//                String type = input.getAttribute("type");
//                String name = input.getAttribute("name");
//                System.out.println("Input Found - Type: " + type + ", Name: " + name);
//                if ("username".equals(name) && "password".equals(name)) {    // username의 input태그에 abcd@naver.com 입력
//                    input.sendKeys("abcd@naver.com");
//                      input.sendKeys("내 비밀번호");
//                    // 다른 작업을 할 필요가 없다면, 여기서 루프를 종료해도 됩니다.
//                    // break;
//                }
//            }

        } catch (Exception e) {
            // 크롤링 중 오류 발생 시
            e.printStackTrace();
            resultMessage = "크롤링 중 오류가 발생했습니다: " + e.getMessage();

        } finally {
            // 6. WebDriver 종료 (매우 중요: 자원 누수 방지)
            if (driver != null) {
                driver.quit();
            }
        }

        return null;
    }
}
