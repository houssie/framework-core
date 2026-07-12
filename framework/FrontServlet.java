package framework;

import framework.mg.itu.annotation.UrlMapping;
import framework.mg.itu.annotation.UrlMethod;
import framework.mg.itu.annotation.Url;
import framework.mg.itu.view.ModelAndView;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;                                                                   
import java.util.List;
import java.util.Map;

public class FrontServlet extends HttpServlet {
    private Map<UrlMethod, UrlMapping > mappingUrls = new HashMap<>();
    private String viewPrefix;
    private String viewSuffix;

    @Override
    public void init() throws ServletException {
        // La logique de scan est maintenant dans AppListener.
        // On récupère simplement les données depuis le ServletContext.
        Object mappingUrlsObject = getServletContext().getAttribute("mappingUrls");
        
        if (mappingUrlsObject instanceof Map) {
            this.mappingUrls = (Map<UrlMethod, UrlMapping>) mappingUrlsObject;
        } else {
            // Si la map est introuvable, c'est une erreur critique.
            throw new ServletException("ERREUR: La table de routage (mappingUrls) est introuvable dans le ServletContext. L'AppListener a-t-il échoué ?");
        }

        // Récupération des paramètres de configuration pour les vues
        this.viewPrefix = getInitParameter("view.prefix");
        this.viewSuffix = getInitParameter("view.suffix");
        System.out.println("FrontServlet: Récupération de " + this.mappingUrls.size() + " routes.");
    }

    protected void processRequest(HttpServletRequest req, HttpServletResponse res) throws IOException {
    // 1. Récupération de l'URL relative (ex: /index.html ou /clients)
    String url = req.getRequestURI().substring(req.getContextPath().length());
    String httpMethod = req.getMethod();
    UrlMethod key = new UrlMethod(url, httpMethod);

    // Interception de la racine pour afficher la page de bienvenue/debug
    if (url.equals("/")) {
        res.setContentType("text/html;charset=UTF-8");
        String projectName = req.getContextPath().substring(1);
        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>Welcome to ").append(projectName).append("</title></head><body>");
        html.append("<h1>").append(projectName).append("</h1>");
        html.append("<p>Votre framework est opérationnel.</p>");

        // La liste des contrôleurs n'est plus affichée ici pour alléger la servlet.

        html.append("<h2>Routes Enregistrées</h2><ul>");
        mappingUrls.forEach((k, v) -> html.append("<li><b>").append(k.getHttpMethod()).append("</b> ").append(k.getUrl()).append(" &rarr; ").append(v.getControllerMethod().getName()).append("()</li>"));
        html.append("</ul></body></html>");
        res.getWriter().println(html.toString());
        return;
    }
    // 2. Vérification : est-ce un fichier physique existant ?
    // Si le fichier existe et qu'il n'est pas géré par une route de votre framework
    if (getServletContext().getResource(url) != null && !url.equals("/") && !mappingUrls.containsKey(key)) { // Note: containsKey is fine here
        // C'est un fichier statique (HTML, CSS, JS), on arrête le framework ici
        // et on laisse Tomcat servir le fichier normalement.
        return; 
    }

    // 3. Logique du Framework : gestion des routes annotées
    UrlMapping mapping = mappingUrls.get(key);
    if (mapping != null) {
        try {
            // On récupère la classe et la méthode directement depuis l'objet Mapping
            Class<?> clazz = mapping.getControllerClass();
            Method method = mapping.getControllerMethod();
            
            // On crée une nouvelle instance du contrôleur
            Object instance = clazz.getDeclaredConstructor().newInstance();
            
            // On exécute la méthode
            Object result = method.invoke(instance);
            
            // --- GESTION DU RETOUR ---
            if (result instanceof ModelAndView) {
                ModelAndView mv = (ModelAndView) result;
                // 1. Injecter les données du modèle dans les attributs de la requête
                mv.getData().forEach(req::setAttribute);

                // 2. Construire le chemin de la vue et faire un forward
                String viewPath = this.viewPrefix + mv.getView() + this.viewSuffix;
                req.getRequestDispatcher(viewPath).forward(req, res);

            } else if (result instanceof String) {
                // Si c'est une chaîne, on l'affiche
                res.setContentType("text/html;charset=UTF-8");
                res.getWriter().println(result);
            }
            // (Dans le futur, on pourra ajouter des 'else if' pour gérer d'autres types comme ModelView, JSON, etc.)
        } catch (Exception e) {
            e.printStackTrace(); // Utile pour le débogage
            res.sendError(500, "Erreur lors de l'exécution de la méthode du contrôleur : " + e.getMessage());
        }
    } else {
        // 4. Si ce n'est ni un fichier, ni une route définie, c'est une 404
        res.sendError(404, "Page non trouvee pour l'URL: " + url);
    }
}

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException { processRequest(req, res); }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException { processRequest(req, res); }
}