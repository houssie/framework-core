package framework.mg.itu.annotation;

import java.lang.reflect.Method;

public class UrlMapping {
    private Class<?> controllerClass;
    private Method controllerMethod ;

    public UrlMapping (Class<?> controllerClass , Method controllerMethod){
        this.controllerClass = controllerClass;
        this.controllerMethod= controllerMethod;
    }

    public Class<?> getControllerClass() {
        return controllerClass;
    }

    public Method getControllerMethod() {
        return controllerMethod;
    }

}