package org.hejinggang.elibrary.common.response;

import org.hejinggang.elibrary.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @Test
    void success_withData() {
        ApiResponse<String> response = ApiResponse.success("Hello");

        assertEquals(0, response.getCode());
        assertEquals("success", response.getMessage());
        assertEquals("Hello", response.getData());
    }

    @Test
    void success_withMessageAndData() {
        ApiResponse<String> response = ApiResponse.success("Operation successful", "Result");

        assertEquals(0, response.getCode());
        assertEquals("Operation successful", response.getMessage());
        assertEquals("Result", response.getData());
    }

    @Test
    void success_withList() {
        List<String> items = List.of("item1", "item2");
        ApiResponse<List<String>> response = ApiResponse.success(items);

        assertEquals(0, response.getCode());
        assertEquals(2, response.getData().size());
    }

    @Test
    void error_withCodeAndMessage() {
        ApiResponse<Void> response = ApiResponse.error(400, "Bad request");

        assertEquals(400, response.getCode());
        assertEquals("Bad request", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void error_withErrorCode() {
        ApiResponse<Void> response = ApiResponse.error(ErrorCode.USER_NOT_FOUND, "User not found");

        assertEquals(ErrorCode.USER_NOT_FOUND, response.getCode());
        assertEquals("User not found", response.getMessage());
    }
}