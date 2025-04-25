package com.example.boot17crudtemplate.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommonResponse<T> {
    private Boolean success;
    private String message;
    private T data;

    public static <T> CommonResponse<T> ok(T data) {
        return new CommonResponse<>(true, "요청 성공", data);
    }
    public static CommonResponse<?> fail(String message) {
        return new CommonResponse<>(false, message, null);
    }
}
