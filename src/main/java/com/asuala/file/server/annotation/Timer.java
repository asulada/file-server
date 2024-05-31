package com.asuala.file.server.annotation;

import java.lang.annotation.*;

/**
 * @description:
 * @create: 2024/02/03
 **/
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Timer {

    String value();

    int index() default 0;
}