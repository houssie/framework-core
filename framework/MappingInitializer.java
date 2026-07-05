package framework;

import framework.mg.itu.annotation.Url;
import framework.mg.itu.annotation.UrlMapping;
import framework.mg.itu.annotation.UrlMethod;
import jakarta.servlet.ServletException;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class MappingInitializer {

    public static void getMap(String packageName, Map<UrlMethod, UrlMapping> mappingUrls, List<Class<?>> scannedControllers) throws Exception {
        if (packageName == null || packageName.isEmpty()) {
            throw new ServletException("Le package à scanner ne peut pas être nul ou vide.");
        }

        List<Class<?>> controllers = PackageScanner.getControllers(packageName);
        scannedControllers.addAll(controllers); // On remplit la liste passée en paramètre

        for (Class<?> clazz : controllers) {
            for (Method m : clazz.getDeclaredMethods()) {
                if (m.isAnnotationPresent(Url.class)) {
                    Url urlAnnotation = m.getAnnotation(Url.class);
                    UrlMethod key = new UrlMethod(urlAnnotation.value(), urlAnnotation.method());
                    if (mappingUrls.containsKey(key)) {
                        throw new ServletException("Conflit de route : L'URL " + key + " est déjà définie.");
                    }
                    mappingUrls.put(key, new UrlMapping(clazz, m));
                }
            }
        }
    }
}