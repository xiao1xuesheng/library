package org.hejinggang.elibrary.common.enums;

public enum UserType {
    NORMAL(3, 7),
    VIP(10, 30);

    private final int maxBorrowLimit;
    private final int borrowDurationDays;

    UserType(int maxBorrowLimit, int borrowDurationDays) {
        this.maxBorrowLimit = maxBorrowLimit;
        this.borrowDurationDays = borrowDurationDays;
    }

    public int getMaxBorrowLimit() {
        return maxBorrowLimit;
    }

    public int getBorrowDurationDays() {
        return borrowDurationDays;
    }
}