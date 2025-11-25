package com.example.demo.config.auth.exceptionHandler;

// 404 NOT FOUND 상태 코드를 반환하도록 지정
// 회원을 찾을 수 없을 때 사용할 예외 클래스
public class ResourceNotFoundException extends RuntimeException{

    private String resourceName;
    private String fieldName;
    private Object fieldValue;

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue){
        super(String.format("%S를 찾을 수 없습니다, %s : '%s'", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }
}
