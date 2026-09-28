package org.hejinggang.elibrary.common.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BusinessExceptionTest {

    @Test
    void constructor_setsCodeAndMessage() {
        BusinessException ex = new BusinessException(ErrorCode.USER_NOT_FOUND, "User not found");

        assertEquals(ErrorCode.USER_NOT_FOUND, ex.getCode());
        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void getCode_returnsCorrectCode() {
        BusinessException ex = new BusinessException(404, "Not found");

        assertEquals(404, ex.getCode());
    }
}