package net.aopacloud.superbi.enums;

public enum PrivilegeTypeEnum {
    /**
     * table privilege
     */
    TABLE,
    /**
     * column privilege
     */
    COLUMN,
    /**
     * row privilege
     */
    ROW,
    /**
     * row and column privilege
     */
    COLUMN_ROW;

    public boolean hasAllColumnPrivilege() {
        return this == TABLE || this == ROW;
    }

    public boolean hasColumnPrivilege() {
        return this == COLUMN || this == COLUMN_ROW;
    }

    public boolean hasRowPrivilege() {
        return this == ROW || this == COLUMN_ROW;
    }

    public boolean isTablePrivilege() {
        return this == TABLE;
    }

    public boolean isColumnRowPrivilege() {
        return this == COLUMN_ROW;
    }

    public boolean isColumnPrivilege() {
        return this == COLUMN;
    }

    public boolean isRowPrivilege() {
        return this == ROW;
    }
}
