package com.sky.aspect;


import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;

/**
 * 自动填充切面类：通过AOP技术，在执行特定方法前自动填充公共字段
 */
@Component // 标记为Spring组件，使其被容器管理
@Aspect // 标记为切面类，用于定义切入点和通知
@Slf4j
public class AutoFillAspect {

    /**
     * 定义切入点：指定哪些方法需要被拦截
     * execution(* com.sky.mapper.*.*(..))：匹配com.sky.mapper包下所有类的所有方法
     *
     * @annotation(com.sky.annotation.AutoFill)：匹配被@AutoFill注解标记的方法 两者通过&&连接，表示"同时满足"才会被拦截
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointcut() {
        // 切入点方法无需实现逻辑，仅作为标记
    }

    /**
     * 前置通知：在切入点方法执行前执行该方法
     *
     * @param joinPoint 连接点对象，包含被拦截方法的信息（如参数、方法签名等）
     */
    @Before("autoFillPointcut()") // 关联到上面定义的切入点
    public void autoFill(JoinPoint joinPoint) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        log.info("开始自动填充数据");

        // 1. 获取方法签名（包含方法的元数据）
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        // 2. 从方法签名中获取@AutoFill注解（该注解标记了操作类型：新增/修改）
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);
        // 3. 获取注解中指定的操作类型（INSERT或UPDATE）
        OperationType operationType = autoFill.value();

        // 4. 获取被拦截方法的参数列表（假设第一个参数是实体对象）
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return; // 没有参数则无需处理
        }
        // 假设方法的第一个参数是需要填充字段的实体对象（如Employee、Category等）
        Object entity = args[0];

        // 5. 准备需要填充的公共字段值
        LocalDateTime now = LocalDateTime.now(); // 当前时间（用于创建/修改时间）
        Long currentId = BaseContext.getCurrentId(); // 当前登录用户ID（从ThreadLocal中获取）

        // 6. 根据操作类型（新增/修改），通过反射调用实体的setter方法设置字段值
        if (operationType == OperationType.INSERT) {
            // 新增操作：需要填充创建时间、创建人、修改时间、修改人
            // 反射获取实体的setCreateTime方法，并调用（参数为当前时间）
            entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class)
                    .invoke(entity, now);
            // 反射调用setCreateUser方法（参数为当前用户ID）
            entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class)
                    .invoke(entity, currentId);
            // 新增时，修改时间和创建时间一致
            entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class)
                    .invoke(entity, now);
            // 新增时，修改人和创建人一致
            entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class)
                    .invoke(entity, currentId);
        } else if (operationType == OperationType.UPDATE) {
            // 修改操作：只需填充修改时间和修改人
            entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class)
                    .invoke(entity, now);
            entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class)
                    .invoke(entity, currentId);
        }
    }
}