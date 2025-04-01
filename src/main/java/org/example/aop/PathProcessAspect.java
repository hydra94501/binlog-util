package org.example.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.example.aop.annotations.PathProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

/**
 * @author Alan_
 */
@Aspect
@Component
public class PathProcessAspect {
    private static final Logger log = LoggerFactory.getLogger(PathProcessAspect.class);

    @Around("execution(* org.example..*(..))")
    public Object processPath(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        // 处理方法参数
        Parameter[] parameters = ((MethodSignature) joinPoint.getSignature()).getMethod().getParameters();
        for (int i = 0; i < parameters.length; i++) {
            PathProcess annotation = AnnotationUtils.findAnnotation(parameters[i], PathProcess.class);
            if (annotation != null && args[i] instanceof String) {
                String argValue = (String) args[i];
                if (argValue.startsWith(annotation.value())) {
                    args[i] = argValue.substring(annotation.value().length());
                }
            }
        }
        // 处理参数对象的字段
        for (Object arg : args) {
            processFields(arg, true);
        }
        // 执行目标方法
        Object result = joinPoint.proceed(args);
        // 处理返回值对象的字段
        processFields(result, false);
        return result;
    }

    private void processFields(Object obj, boolean isBefore) {
        if (obj == null) {
            return;
        }
        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            PathProcess annotation = AnnotationUtils.findAnnotation(field, PathProcess.class);
            if (annotation != null) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(obj);
                    if (value instanceof String) {
                        String path = getString(isBefore, (String) value, annotation);
                        field.set(obj, path);
                    }
                } catch (IllegalAccessException e) {
                    log.error("处理字段时出错", e);
                }
            }
        }
    }

    private static String getString(boolean isBefore, String value, PathProcess annotation) {
        String path = value;
        if (isBefore) {
            // 方法执行前，去除前缀
            if (path.startsWith(annotation.value())) {
                path = path.substring(annotation.value().length());
            }
        } else {
            // 方法执行后，添加前缀
            if (!path.startsWith(annotation.value())) {
                path = annotation.value() + path;
            }
        }
        return path;
    }
}
