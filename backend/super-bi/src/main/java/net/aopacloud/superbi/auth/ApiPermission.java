package net.aopacloud.superbi.auth;


import net.aopacloud.superbi.annotation.Logical;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface ApiPermission {

    String[] value() default {};

    Logical logical() default Logical.AND;

}
