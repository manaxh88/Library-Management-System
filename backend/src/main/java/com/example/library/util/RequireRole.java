package com.example.library.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限检查注解
 * 使用方式: @RequireRole({0, 1}) 表示仅管理员和教师可访问
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    /**
     * 允许访问的角色列表
     * 0: 管理员, 1: 教师, 2: 学生
     */
    int[] roles() default {};
}
