package net.aopacloud.superbi.enums;

import java.util.List;

public enum PermissionEnum {

    NONE(0), EXPIRED(1), READ(3), WRITE(7);

    private int code;

    PermissionEnum(int code) {
        this.code = code;
    }

    public boolean hasReadPermission() {
        return this.code >= 3;
    }

    public boolean hasWritePermission() {
        return this.code >= 7;
    }

    public PermissionEnum and(PermissionEnum permission) {
        int result = this.code & permission.code;
        return ofCode(result);
    }

    public PermissionEnum and(List<PermissionEnum> permissions) {

        int result = 0;
        for (PermissionEnum permission : permissions) {
            result &= permission.code;
        }
        return ofCode(result);
    }

    public PermissionEnum or(PermissionEnum permission) {
        int result = this.code | permission.code;
        return ofCode(result);
    }

    public PermissionEnum or(List<PermissionEnum> permissions) {

        int result = 0;
        for (PermissionEnum permission : permissions) {
            result |= permission.code;
        }
        return ofCode(result);
    }

    public PermissionEnum ofCode(int code) {

        if (code >= 7) {
            return WRITE;
        }
        if (code >= 3) {
            return READ;
        }
        if (code == 1) {
            return EXPIRED;
        }
        return NONE;
    }
}
