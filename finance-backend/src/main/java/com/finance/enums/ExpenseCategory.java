package com.finance.enums;

public enum ExpenseCategory {
    PROTECTION("人生保障"),
    LEISURE("休闲玩乐"),
    DAILY("日常开销"),
    OTHER("其他");

    private final String label;

    ExpenseCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
