package framework;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

/**
 * Sprint 7 : convertit les paramètres HTTP d'une requête
 * en arguments typés pour une méthode de contrôleur.
 *
 * Ne gère QUE les types primitifs, leurs wrappers, et String.
 */
public class ParameterResolver {

    /**
     * Construit le tableau d'arguments à passer à method.invoke().
     *
     * @param method la méthode du contrôleur
     * @param params map des paramètres HTTP (nom → valeurs[])
     * @return tableau d'arguments prêt à l'emploi
     */
    public static Object[] resolve(Method method, Map<String, String[]> params) {
        Parameter[] methodParams = method.getParameters();
        Object[] args = new Object[methodParams.length];

        for (int i = 0; i < methodParams.length; i++) {
            Parameter p = methodParams[i];
            String name = p.getName();          // nécessite -parameters à la compilation
            String[] values = params.get(name);

            if (values == null || values.length == 0) {
                args[i] = defaultValueFor(p.getType());
            } else {
                args[i] = convert(values[0], p.getType());
            }
        }
        return args;
    }

    /**
     * Valeur par défaut pour un type primitif quand le paramètre est absent.
     */
    public static Object defaultValueFor(Class<?> type) {
        if (!type.isPrimitive()) return null;
        if (type == int.class)     return 0;
        if (type == long.class)    return 0L;
        if (type == double.class)  return 0.0d;
        if (type == float.class)   return 0.0f;
        if (type == boolean.class) return false;
        if (type == short.class)   return (short) 0;
        if (type == byte.class)    return (byte) 0;
        if (type == char.class)    return '\0';
        return null;
    }

    /**
     * Convertit une String HTTP en type cible.
     * En cas d'échec : retourne la valeur par défaut (pas d'exception).
     */
    public static Object convert(String value, Class<?> type) {
        if (value == null) return defaultValueFor(type);

        try {
            if (type == String.class) return value;

            if (type == int.class || type == Integer.class)     return Integer.parseInt(value);
            if (type == long.class || type == Long.class)       return Long.parseLong(value);
            if (type == double.class || type == Double.class)   return Double.parseDouble(value);
            if (type == float.class || type == Float.class)     return Float.parseFloat(value);
            if (type == boolean.class || type == Boolean.class) return Boolean.parseBoolean(value);
            if (type == short.class || type == Short.class)     return Short.parseShort(value);
            if (type == byte.class || type == Byte.class)       return Byte.parseByte(value);
            if (type == char.class || type == Character.class) {
                return value.isEmpty() ? '\0' : value.charAt(0);
            }
        } catch (NumberFormatException e) {
            return defaultValueFor(type);
        }

        // Type non supporté (POJO, Date, etc.) → null
        return null;
    }
}