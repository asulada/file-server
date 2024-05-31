package com.asuala.file.server.es.annotation;

import com.asuala.file.server.es.enums.EsDataType;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface EsField {
    //默认属性名
    String name() default "";

    //数据类型
    EsDataType type();

    //分词
    String analyzer() default "";

    //搜索分词
    String searchAnalyzer() default "";
}
