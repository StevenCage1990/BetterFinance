package com.finance.enums;

public enum IncomeType {
    SALARY("工资"),
    FUND("公积金"),
    BONUS("奖金"),
    DIVIDEND("股权"),
    OTHER("其他");

    private final String label;

    IncomeType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
