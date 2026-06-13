package etu4179.framework;

import java.io.IOException;
import java.util.HashMap;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontServlet extends HttpServlet {
    
    private HashMap<String, Mapping> mappingUrls;
  
    @Override
    public void init() throws ServletException {
        this.mappingUrls = new HashMap<>();

       try {
            // 1. Récupérer le chemin absolu du dossier /WEB-INF/classes de l'application de test
            String path = this.getServletContext().getRealPath("/WEB-INF/classes");
            
            if (path == null) {
                System.out.println("Attention : Impossible de localiser le dossier WEB-INF/classes.");
                return;
            }

            java.io.File classesDir = new java.io.File(path);
            
            // 2. Vérifier si le dossier existe
            if (classesDir.exists() && classesDir.isDirectory()) {
                // Lancer le parcours récursif pour trouver tous les fichiers .class
                scanDirectory(classesDir, "");
            }
            
            System.out.println("Scan terminé ! Nombre de routes chargées : " + this.mappingUrls.size());
            
        } catch (Exception e) {
            throw new ServletException("Erreur lors de l'initialisation du scanner de composants", e);
        }
    }

    private void scanDirectory(java.io.File directory, String packageName) throws ClassNotFoundException {
        java.io.File[] files = directory.listFiles();
        if (files == null) return;

        for (java.io.File file : files) {
            if (file.isDirectory()) {
                // Si c'est un sous-dossier, on construit le nom du package associé
                String subPackage = packageName.isEmpty() ? file.getName() : packageName + "." + file.getName();
                scanDirectory(file, subPackage); // Appel récursif
            } else if (file.getName().endsWith(".class")) {
                // Si c'est un fichier .class, on extrait le nom de la classe
                String className = file.getName().substring(0, file.getName().length() - 6);
                String fullClassName = packageName.isEmpty() ? className : packageName + "." + className;

                // Charger la classe dynamiquement grâce à la Réflexion
                Class<?> clazz = Class.forName(fullClassName);

                // Vérifier si la classe possède notre annotation @Controller
                if (clazz.isAnnotationPresent(etu4179.framework.annotation.Controller.class)) {
                    
                    // Parcourir toutes les méthodes de cette classe
                    java.lang.reflect.Method[] methods = clazz.getDeclaredMethods();
                    for (java.lang.reflect.Method method : methods) {
                        
                        // Vérifier si la méthode possède l'annotation @Url
                        if (method.isAnnotationPresent(etu4179.framework.annotation.Url.class)) {
                            // Récupérer la valeur de l'URL (ex: "/aaa/client")
                            etu4179.framework.annotation.Url urlAnnotation = method.getAnnotation(etu4179.framework.annotation.Url.class);
                            String urlValue = urlAnnotation.value();

                            // Créer notre Mapping et l'ajouter à la HashMap
                            Mapping mapping = new Mapping(fullClassName, method.getName());
                            this.mappingUrls.put(urlValue, mapping);
                            
                            System.out.println("Route enregistrée : " + urlValue + " -> " + fullClassName + "." + method.getName() + "()");
                        }
                    }
                }
            }
        }
    }

    protected void processRequest (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
         response.setContentType("text/html;charset=UTF-8");
         try {
            // 1. Extraire et nettoyer l'URL demandée
            // Si l'URL complète est "http://localhost:8080/test-app/aaa/client"
            // request.getRequestURI() donne -> "/test-app/aaa/client"
            // request.getContextPath() donne -> "/test-app"
            // En faisant le substring, urlTraquee devient -> "/aaa/client"
            String urlTraquee = request.getRequestURI().substring(request.getContextPath().length());

            // 2. Chercher la route dans notre HashMap
            if (this.mappingUrls.containsKey(urlTraquee)) {
                Mapping mapping = this.mappingUrls.get(urlTraquee);

                // 3. Récupérer le nom de la classe et charger la classe en mémoire
                String className = mapping.getClassName();
                Class<?> clazz = Class.forName(className);

                // 4. Instancier dynamiquement la classe (équivalent de: Object instance = new MonControleur())
                // .getDeclaredConstructor().newInstance() est la méthode moderne en Java
                Object instance = clazz.getDeclaredConstructor().newInstance();

                // 5. Récupérer la méthode par son nom (sans paramètres pour l'instant)
                String methodName = mapping.getMethodName();
                java.lang.reflect.Method method = clazz.getDeclaredMethod(methodName);

                // 6. Exécuter la méthode sur notre instance (équivalent de: instance.maMethode())
                method.invoke(instance);

                // Petit message temporaire de confirmation dans le navigateur
                response.getWriter().println("<p style='color: green;'>[Framework] Route " + urlTraquee + " exécutée avec succès ! Regarde la console Tomcat.</p>");
                
            } else {
                // Si l'URL n'est pas dans la HashMap, on renvoie une vraie erreur 404
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "La route " + urlTraquee + " n'a pas été trouvée dans ce framework.");
            }

        } catch (Exception e) {
            // En cas de crash (problème d'instanciation, de méthode...), on affiche l'erreur
            e.printStackTrace(response.getWriter());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    
         
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);  
    }
}
