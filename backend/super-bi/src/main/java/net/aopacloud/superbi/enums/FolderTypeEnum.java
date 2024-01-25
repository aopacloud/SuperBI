package net.aopacloud.superbi.enums;

public enum FolderTypeEnum {

    ALL("ALL"),
    PERSONAL("PERSONAL");

    private String name;

    FolderTypeEnum(String name) {
        this.name = name;
    }

    public static FolderTypeEnum folderTye(String name) {
        for (FolderTypeEnum value : FolderTypeEnum.values()) {
            if (value.getName().equals(name)) {
                return value;
            }
        }
        return ALL;
    }

    public String getName() {
        return name;
    }
}
