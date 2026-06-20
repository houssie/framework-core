package framework.mg.itu.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// @Target dit où on a le droit de mettre l'annotation. Ici, TYPE = sur une Classe.
@Target(ElementType.TYPE)
// @Retention dit combien de temps l'annotation survit. RUNTIME = elle est visible pendant l'exécution du programme (crucial pour la Réflexion).
@Retention(RetentionPolicy.RUNTIME)
public @interface Controller {
    // Une annotation simple sans paramètres
}



