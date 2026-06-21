package framework;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import framework.mg.itu.annotation.Controller;

public class PackageScanner {

    /**
     * Scanne un package donné pour trouver toutes les classes 
     * annotées avec @Controller.
     */
    public static List<Class<?>> getControllers(String packageName) throws Exception {
        List<Class<?>> controllers = new ArrayList<>();
        
        // 1. Traduction du package en chemin système (ex: mg/itu/controllers)
        String path = packageName.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL resource = classLoader.getResource(path);
        
        if (resource == null) {
            throw new Exception("Le package " + packageName + " est introuvable.");
        }

        File directory = new File(resource.getFile());
        if (directory.exists()) {
            for (File file : directory.listFiles()) {
                if (file.getName().endsWith(".class")) {
                    // 2. Création du nom complet de la classe (ex: mg.itu.controllers.TestController)
                    String className = packageName + "." + file.getName().replace(".class", "");
                    Class<?> clazz = Class.forName(className);
                    
                    // 3. Filtrage par Réflexion : Est-ce un @Controller ?
                    if (clazz.isAnnotationPresent(Controller.class)) {
                        controllers.add(clazz);
                    }
                }
            }
        }
        return controllers;
    }
}