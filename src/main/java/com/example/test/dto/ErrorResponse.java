package com.example.test.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Ответ с ошибкой
 */
@Getter
@Setter
public class ErrorResponse {

    /**
     * Класс исключения.
     */
    private String exceptionType;
    /**
     * Корневое сообщение об ошибке
     */
    private String message;
}
