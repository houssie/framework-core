package framework;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import framework.mg.itu.annotation.Controller;

public class PackageScanner {

    /**
     * Scanne un ou plusieurs packages (séparés par des virgules)
     * pour trouver toutes les classes annotées avec @Controller.
     *
     * Exemple : "com.test.controllers,mg.itu.controllers"
     */
    public static List<Class<?>> getControllers(String packageNames) throws Exception {
        List<Class<?>> controllers = new ArrayList<>();

        if (packageNames == null || packageNames.isEmpty()) {
            return controllers;
        }

        // On découpe sur la virgule
        String[] packages = packageNames.split(",");

        for (String pkg : packages) {
            pkg = pkg.trim();
            if (pkg.isEmpty()) continue;
            controllers.addAll(getControllersFromPackage(pkg));
        }

        return controllers;
    }

    /**
     * Scanne un seul package.
     */
    private static List<Class<?>> getControllersFromPackage(String packageName) throws Exception {
        List<Class<?>> controllers = new ArrayList<>();

        String path = packageName.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL resource = classLoader.getResource(path);

        if (resource == null) {
            System.out.println("⚠️ Package introuvable (ignoré) : " + packageName);
            return controllers;
        }

        File directory = new File(resource.getFile());
        if (!directory.exists()) {
            return controllers;
        }

        for (File file : directory.listFiles()) {
            if (file.getName().endsWith(".class")) {
                String className = packageName + "." + file.getName().replace(".class", "");
                Class<?> clazz = Class.forName(className);

                if (clazz.isAnnotationPresent(Controller.class)) {
                    controllers.add(clazz);
                }
            }
        }

        return controllers;
    }
}