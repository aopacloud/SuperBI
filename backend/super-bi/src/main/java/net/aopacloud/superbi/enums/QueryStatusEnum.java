package net.aopacloud.superbi.enums;

/**
 * 查询状态
 */
public enum QueryStatusEnum {
    SUCCESS,FAILED,RUNNING,NO_PRIVILEGE;

    public boolean isSuccess() {
        return this == SUCCESS;
    }
}