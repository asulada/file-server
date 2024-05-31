package com.asuala.file.server.es.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @description:
 * @create: 2022/09/18
 **/
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface DocId {
}