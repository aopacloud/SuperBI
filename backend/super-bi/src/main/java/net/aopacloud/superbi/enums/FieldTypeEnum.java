package net.aopacloud.superbi.enums;

public enum FieldTypeEnum {

    ORIGIN,
    ADD;

    public boolean isNewAdd() {
        return this == ADD;
    }
}
