package com.finance.enums;

public enum LiabilityGroup {
    LOAN("贷款");

    private final String label;

    LiabilityGroup(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
