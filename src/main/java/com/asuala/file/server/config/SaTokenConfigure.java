package com.asuala.file.server.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.strategy.SaStrategy;
import cn.dev33.satoken.util.SaFoxUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @description:
 * @create: 2023/08/16
 **/
@Configuration
@Slf4j
public class SaTokenConfigure implements WebMvcConfigurer {
    /**
     * 重写 Sa-Token 框架内部算法策略
     */
    @Autowired
    public void rewriteSaStrategy() {
        // 重写 Token 生成策略
        SaStrategy.instance.createToken = (loginId, loginType) -> {
            return SaFoxUtil.getRandomString(60);    // 随机60位长度字符串
        };
    }

    // 注册 Sa-Token 拦截器，打开注解式鉴权功能
//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        // 注册 Sa-Token 拦截器，打开注解式鉴权功能
//        registry.addInterceptor(new SaInterceptor()).addPathPatterns("/**");
//    }

    // 注册拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器，校验规则为 StpUtil.checkLogin() 登录校验。
        registry.addInterceptor(new SaInterceptor(handle -> {
            // 指定一条 match 规则
            SaRouter
                    .match("/**")    // 拦截的 path 列表，可以写多个 */
//                    .notMatch("/user/doLogin")        // 排除掉的 path 列表，可以写多个
                    .check(r -> StpUtil.checkLogin());
        }))
                .addPathPatterns("/**")
                .excludePathPatterns("/", "/index.html", "/login", "/pass", "/video/**");
    }

}