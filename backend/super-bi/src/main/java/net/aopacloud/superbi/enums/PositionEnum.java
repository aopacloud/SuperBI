package net.aopacloud.superbi.enums;

/**
 * @Author shinnie
 * @Description
 * @Date 16:23 2023/8/10
 */
public enum PositionEnum {
    DATASOURCE("DATASOURCE"),
    DATASET("DATASET"),
    REPORT("REPORT"),
    DASHBOARD("DASHBOARD");

    private String name;

    PositionEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
