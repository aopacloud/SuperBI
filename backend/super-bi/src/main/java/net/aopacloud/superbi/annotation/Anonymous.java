package net.aopacloud.superbi.annotation;

import java.lang.annotation.*;

/**
 * @Description:
 * @Author: rick
 * @date: 2023/11/29 23:02
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Anonymous {
}
