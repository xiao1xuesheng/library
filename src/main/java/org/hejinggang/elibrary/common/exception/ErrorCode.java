package org.hejinggang.elibrary.common.exception;

public class ErrorCode {
    public static final int SUCCESS = 0;
    public static final int VALIDATION_ERROR = 1001;
    public static final int NOT_FOUND = 1002;
    public static final int CONTENT_EMPTY = 4001;
    public static final int SYSTEM_ERROR = 5000;
    public static final int REQUEST_PROCESSING = 5001;

    public static final int USERNAME_EXISTS = 2000;
    public static final int USER_NOT_FOUND = 2001;
    public static final int USER_DISABLED = 2002;
    public static final int USER_MUTED = 2003;
    public static final int PASSWORD_INCORRECT = 2004;
    public static final int NOT_YOUR_REVIEW = 2005;
    public static final int NOT_YOUR_NOTE = 2006;
    public static final int NO_READ_PERMISSION = 2007;

    public static final int BOOK_OFF_SHELF = 3001;
    public static final int ALREADY_BORROWED = 3002;
    public static final int BORROW_LIMIT_EXCEEDED = 3003;
    public static final int NO_BORROW_PERMISSION = 3004;
    public static final int ALREADY_RETURNED = 3005;
    public static final int BALANCE_INSUFFICIENT = 3006;
    public static final int RENEW_LIMIT_EXCEEDED = 3007;
    public static final int BORROW_RECORD_NOT_FOUND = 3008;
    public static final int REVIEW_NOT_FOUND = 3009;
    public static final int NOTE_NOT_FOUND = 3010;

    public static final int ADMIN_NOT_FOUND = 4001;
    public static final int ADMIN_DISABLED = 4002;
    public static final int ADMIN_NO_PERMISSION = 4003;

    public static final int ORDER_NOT_FOUND = 5001;
    public static final int ORDER_EXPIRED = 5002;
    public static final int PARAM_ERROR = 5003;
}