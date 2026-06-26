package framework;

import framework.mg.itu.annotation.UrlMapping;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

public class FrontServlet extends HttpServlet {
    private HashMap<String, String[]> mappingUrls = new HashMap<>();

    @Override
    public void init() throws ServletException {
        try {
            // 1. Récupération dynamique du package à scanner via le web.xml
            String packageName = getInitParameter("packageToScan");
            if (packageName == null || packageName.isEmpty()) {
                throw new ServletException("Le paramètre 'packageToScan' est obligatoire dans web.xml");
            }

            // 2. Scan des contrôleurs via l'outil dédié
            List<Class<?>> controllers = PackageScanner.getControllers(packageName);

            // 3. Enregistrement des mappings (URL -> Classe/Méthode)
            for (Class<?> clazz : controllers) {
                for (Method m : clazz.getDeclaredMethods()) {
                    if (m.isAnnotationPresent(UrlMapping.class)) {
                        String url = m.getAnnotation(UrlMapping.class).value();
                        String[] mappingInfo = {clazz.getName(), m.getName()};
                        mappingUrls.put(url, mappingInfo);
                    }
                }
            }
        } catch (Exception e) {
            throw new ServletException("Erreur lors de l'initialisation du framework : " + e.getMessage(), e);
        }
    }

    protected void processRequest(HttpServletRequest req, HttpServletResponse res) throws IOException {
    // 1. Récupération de l'URL relative (ex: /index.html ou /clients)
    String url = req.getRequestURI().substring(req.getContextPath().length());

    // 2. Vérification : est-ce un fichier physique existant ?
    // Si le fichier existe et qu'il n'est pas géré par une route de votre framework
    if (getServletContext().getResource(url) != null && !url.equals("/") && !mappingUrls.containsKey(url)) {
        // C'est un fichier statique (HTML, CSS, JS), on arrête le framework ici
        // et on laisse Tomcat servir le fichier normalement.
        return; 
    }

    // 3. Logique du Framework : gestion des routes annotées
    if (mappingUrls.containsKey(url)) {
        try {
            String[] mapInfo = mappingUrls.get(url);
            String className = mapInfo[0];
            String methodName = mapInfo[1];

            // Chargement dynamique de la classe contrôleur
            Class<?> clazz = Class.forName(className);
            Object instance = clazz.getDeclaredConstructor().newInstance();
            
            // Exécution de la méthode correspondante
            clazz.getDeclaredMethod(methodName).invoke(instance);
            
            // Réponse de succès
            res.getWriter().println("Execution reussie pour : " + url);
        } catch (Exception e) {
            res.sendError(500, "Erreur lors de l'exécution : " + e.getMessage());
        }
    } else {
        // 4. Si ce n'est ni un fichier, ni une route définie, c'est une 404
        res.sendError(404, "Page non trouvee");
    }
}

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException { processRequest(req, res); }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException { processRequest(req, res); }
}