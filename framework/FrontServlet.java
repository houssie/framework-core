package framework;

import framework.mg.itu.annotation.RestApi;
import framework.mg.itu.annotation.Url;
import framework.mg.itu.annotation.UrlMapping;
import framework.mg.itu.annotation.UrlMethod;
import framework.mg.itu.view.ModelAndView;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

@MultipartConfig
public class FrontServlet extends HttpServlet {
    private Map<UrlMethod, UrlMapping> mappingUrls = new HashMap<>();
    private String viewPrefix;
    private String viewSuffix;

    @Override
    public void init() throws ServletException {
        Object mappingUrlsObject = getServletContext().getAttribute("mappingUrls");

        if (mappingUrlsObject instanceof Map) {
            this.mappingUrls = (Map<UrlMethod, UrlMapping>) mappingUrlsObject;
        } else {
            throw new ServletException("ERREUR: La table de routage (mappingUrls) est introuvable dans le ServletContext. L'AppListener a-t-il échoué ?");
        }

        this.viewPrefix = getServletContext().getInitParameter("view.prefix");
        this.viewSuffix = getServletContext().getInitParameter("view.suffix");

        System.out.println("FrontServlet: Récupération de " + this.mappingUrls.size() + " routes.");
        System.out.println("FrontServlet: viewPrefix = [" + this.viewPrefix + "], viewSuffix = [" + this.viewSuffix + "]");
    }

    protected void processRequest(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String url = req.getRequestURI().substring(req.getContextPath().length());
        String httpMethod = req.getMethod();
        UrlMethod key = new UrlMethod(url, httpMethod);

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

        if (getServletContext().getResource(url) != null && !mappingUrls.containsKey(key)) {
            return;
        }

        UrlMapping mapping = mappingUrls.get(key);
        if (mapping != null) {
            try {
                Class<?> clazz = mapping.getControllerClass();
                Method method = mapping.getControllerMethod();

                Object instance = clazz.getDeclaredConstructor().newInstance();

                Object[] args = ParameterResolver.resolve(method, req);
                Object result = method.invoke(instance, args);

                if (method.isAnnotationPresent(RestApi.class)) {
                    res.setContentType("application/json;charset=UTF-8");
                    String json = new Gson().toJson(result);
                    res.getWriter().println(json);
                    return;
                }

                if (result instanceof ModelAndView) {
                    ModelAndView mv = (ModelAndView) result;
                    mv.getData().forEach(req::setAttribute);
                    String viewPath = this.viewPrefix + mv.getView() + this.viewSuffix;
                    System.out.println("FrontServlet: forward vers " + viewPath);
                    req.getRequestDispatcher(viewPath).forward(req, res);
                } else if (result instanceof String) {
                    res.setContentType("text/html;charset=UTF-8");
                    res.getWriter().println(result);
                }

            } catch (Exception e) {
                e.printStackTrace();
                res.sendError(500, "Erreur lors de l'exécution de la méthode du contrôleur : " + e.getMessage());
            }
        } else {
            res.sendError(404, "Page non trouvee pour l'URL: " + url);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException { processRequest(req, res); }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException { processRequest(req, res); }
}
