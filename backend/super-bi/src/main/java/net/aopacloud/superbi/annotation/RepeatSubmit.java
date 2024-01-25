package net.aopacloud.superbi.annotation;

import java.lang.annotation.*;

/**
 * @Description:
 * @Author: rick
 * @date: 2023/11/29 22:52
 */
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RepeatSubmit
{
    /**
     * 间隔时间(ms)，小于此时间视为重复提交
     */
    public long interval() default 5000L;

    /**
     * 提示消息
     */
    public String message() default "不允许重复提交，请稍后再试";
}
