package net.aopacloud.superbi.annotation;

/**
 * @Description: 权限注解的验证模式
 * @Author: rick
 * @date: 2023/11/28 12:15
 */
public enum Logical {
    /**
     * 必须具有所有的元素
     */
    AND,

    /**
     * 只需具有其中一个元素
     */
    OR
}
