package framework;

import framework.mg.itu.annotation.Url;
import framework.PackageScanner;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

public class FrontServlet extends HttpServlet {
    private HashMap<String, Mapping> mappingUrls = new HashMap<>();

    @Override
    public void init() throws ServletException {
        try {
            // 1. On récupère le chemin physique du dossier des classes
            String path = getServletContext().getRealPath("/WEB-INF/classes");
            // 2. On utilise le scanner pour trouver les contrôleurs
            // Le nouvel appel correspondant à votre méthode getControllers
// Remplacez "votre.package.controleurs" par le vrai nom du package 
// où se trouvent vos classes annotées @Controller
List<Class<?>> controllers = PackageScanner.getControllers("mg.itu.controllers");

            // 3. On remplit la Map avec les URLs
            for (Class<?> clazz : controllers) {
                for (Method m : clazz.getDeclaredMethods()) {
                    if (m.isAnnotationPresent(Url.class)) {
                        String url = m.getAnnotation(Url.class).value();
                        mappingUrls.put(url, new Mapping(clazz.getName(), m.getName()));
                    }
                }
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    protected void processRequest(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String url = req.getRequestURI().substring(req.getContextPath().length());
        
        if (mappingUrls.containsKey(url)) {
            try {
                Mapping map = mappingUrls.get(url);
                Class<?> clazz = Class.forName(map.getClassName());
                Object instance = clazz.getDeclaredConstructor().newInstance();
                clazz.getDeclaredMethod(map.getMethodName()).invoke(instance);
                res.getWriter().println("Execution reussie pour : " + url);
            } catch (Exception e) {
                res.sendError(500, e.getMessage());
            }
        } else {
            res.sendError(404, "Page non trouvee");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException { processRequest(req, res); }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException { processRequest(req, res); }
}