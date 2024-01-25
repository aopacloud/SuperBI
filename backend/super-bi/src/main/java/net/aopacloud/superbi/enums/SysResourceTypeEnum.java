package net.aopacloud.superbi.enums;

/**
 * @Author shinnie
 * @Description
 * @Date 15:22 2023/10/11
 */
public enum SysResourceTypeEnum {

    MENU, FUNCTION, BUTTON, ANALYSE_SCOPE;

    public static SysResourceTypeEnum sysResourceType(String type) {
        for (SysResourceTypeEnum value : SysResourceTypeEnum.values()) {
            if (value.name().equals(type)) {
                return value;
            }
        }
        return null;
    }

}
