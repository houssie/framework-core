package etu4179.framework.annotation;

import java.lang.annotation.Target;
import java.lang.annotation.ElemenType;
import java.lang.annoation.Retention;
import java.lang.annotation.RetentionPolicy;

@Target(ElementType.METHOD)


@Retention(RetentionPolicy.RUNTIME)

public @interface Url{
    String value();
}