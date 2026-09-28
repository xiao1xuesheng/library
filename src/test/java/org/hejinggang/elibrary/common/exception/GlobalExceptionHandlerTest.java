package org.hejinggang.elibrary.common.exception;

import org.hejinggang.elibrary.common.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleBusinessException() {
        BusinessException ex = new BusinessException(ErrorCode.USER_NOT_FOUND, "User not found");

        ApiResponse<Void> response = handler.handleBusinessException(ex);

        assertEquals(ErrorCode.USER_NOT_FOUND, response.getCode());
        assertEquals("User not found", response.getMessage());
    }

    @Test
    void handleValidationException() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "field", "must not be null");
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

        ApiResponse<Void> response = handler.handleValidationException(ex);

        assertEquals(ErrorCode.VALIDATION_ERROR, response.getCode());
        assertEquals("must not be null", response.getMessage());
    }

    @Test
    void handleGenericException() {
        RuntimeException ex = new RuntimeException("Unexpected error");

        ApiResponse<Void> response = handler.handleException(ex);

        assertEquals(ErrorCode.SYSTEM_ERROR, response.getCode());
        assertTrue(response.getMessage().contains("System internal error"));
    }
}