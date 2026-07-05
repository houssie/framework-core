package framework;

import framework.mg.itu.annotation.UrlMapping;
import framework.mg.itu.annotation.UrlMethod;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebListener
public class AppListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("Framework: Initialisation du contexte de l'application...");
        ServletContext context = sce.getServletContext();

        try {
            String packageName = context.getInitParameter("packageToScan");
            Map<UrlMethod, UrlMapping> mappingUrls = new HashMap<>();
            List<Class<?>> scannedControllers = new ArrayList<>();

            MappingInitializer.getMap(packageName, mappingUrls, scannedControllers);

            context.setAttribute("mappingUrls", mappingUrls);
            context.setAttribute("scannedControllers", scannedControllers);
            System.out.println("Framework: " + mappingUrls.size() + " routes et " + scannedControllers.size() + " contrôleurs initialisés.");
        } catch (Exception e) {
            // En cas d'erreur grave au démarrage, on log et on arrête tout.
            throw new RuntimeException("Erreur critique lors de l'initialisation du framework", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Logique de nettoyage si nécessaire
    }
}