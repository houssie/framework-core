package framework;

import framework.mg.itu.annotation.UrlMapping;
import framework.mg.itu.annotation.UrlMethod;
import framework.mg.itu.annotation.Url;
import framework.mg.itu.annotation.RestApi;
import framework.mg.itu.view.ModelAndView;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

public class FrontServlet extends HttpServlet {
    private Map<UrlMethod, UrlMapping> mappingUrls = new HashMap<>();
    private String viewPrefix;
    private String viewSuffix;

    @Override
    public void init() throws ServletException {
        // ===== Récupération de la table de routage depuis le ServletContext =====
        Object mappingUrlsObject = getServletContext().getAttribute("mappingUrls");

        if (mappingUrlsObject instanceof Map) {
            this.mappingUrls = (Map<UrlMethod, UrlMapping>) mappingUrlsObject;
        } else {
            throw new ServletException("ERREUR: La table de routage (mappingUrls) est introuvable dans le ServletContext. L'AppListener a-t-il échoué ?");
        }

        // ===== Récupération des paramètres de configuration pour les vues =====
        // ⚠️ IMPORTANT : on utilise getServletContext().getInitParameter(...)
        // pour lire les <context-param> du web.xml
        // (et NON getInitParameter(...) qui lit les <init-param> du <servlet>)
        this.viewPrefix = getServletContext().getInitParameter("view.prefix");
        this.viewSuffix = getServletContext().getInitParameter("view.suffix");

        System.out.println("FrontServlet: Récupération de " + this.mappingUrls.size() + " routes.");
        System.out.println("FrontServlet: viewPrefix = [" + this.viewPrefix + "], viewSuffix = [" + this.viewSuffix + "]");
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
            html.append("<h2>Routes Enregistrées</h2><ul>");
            mappingUrls.forEach((k, v) -> html.append("<li><b>")
                    .append(k.getHttpMethod()).append("</b> ")
                    .append(k.getUrl()).append(" &rarr; ")
                    .append(v.getControllerMethod().getName()).append("()</li>"));
            html.append("</ul></body></html>");
            res.getWriter().println(html.toString());
            return;
        }

        // 2. Vérification : est-ce un fichier physique existant ?
        if (getServletContext().getResource(url) != null && !mappingUrls.containsKey(key)) {
            // C'est un fichier statique (HTML, CSS, JS), on laisse Tomcat servir
            return;
        }

        // 3. Logique du Framework : gestion des routes annotées
        UrlMapping mapping = mappingUrls.get(key);
        if (mapping != null) {
            try {
                Class<?> clazz = mapping.getControllerClass();
                Method method = mapping.getControllerMethod();

                // On crée une nouvelle instance du contrôleur
                Object instance = clazz.getDeclaredConstructor().newInstance();

                // ===== SPRINT 7 : injection des paramètres =====
                Object[] args = ParameterResolver.resolve(method, req.getParameterMap());
                Object result = method.invoke(instance, args);
                // ===============================================

                // ===== SPRINT 6 : détection @RestApi → renvoie du JSON =====
                if (method.isAnnotationPresent(RestApi.class)) {
                    res.setContentType("application/json;charset=UTF-8");
                    String json = new Gson().toJson(result);
                    res.getWriter().println(json);
                    return;
                }
                // ===========================================================

                // --- GESTION DU RETOUR ---
                if (result instanceof ModelAndView) {
                    ModelAndView mv = (ModelAndView) result;

                    // 1. Injecter les données du modèle dans les attributs de la requête
                    mv.getData().forEach(req::setAttribute);

                    // 2. Construire le chemin de la vue et faire un forward
                    String viewPath = this.viewPrefix + mv.getView() + this.viewSuffix;
                    System.out.println("FrontServlet: forward vers " + viewPath);
                    req.getRequestDispatcher(viewPath).forward(req, res);

                } else if (result instanceof String) {
                    // Si c'est une chaîne, on l'affiche
                    res.setContentType("text/html;charset=UTF-8");
                    res.getWriter().println(result);
                }

            } catch (Exception e) {
                e.printStackTrace();
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