package com.finance.enums;

public enum AssetGroup {
    LIQUID("活钱"),
    PROTECTION("保障"),
    INVESTMENT("投资");

    private final String label;

    AssetGroup(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
