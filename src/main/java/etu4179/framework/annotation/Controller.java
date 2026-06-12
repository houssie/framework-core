package etu4179.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


    // etiquette pour les classes 
    @Target(ElementType.TYPE)

    // garder l etiquette visible pendant l execution du programme
    @Retention(RetentionPolicy.RUNTIME)

    public @interface Controller {

    }




