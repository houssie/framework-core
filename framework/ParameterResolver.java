package framework;

import framework.mg.itu.annotation.RequestFile;
import framework.mg.itu.files.MapFile;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ParameterResolver {

    public static Object[] resolve(Method method, HttpServletRequest request) throws Exception {
        Parameter[] methodParams = method.getParameters();
        Object[] args = new Object[methodParams.length];

        boolean isMultipart = isMultipartRequest(request);

        Map<String, String[]> params = null;
        if (!isMultipart) {
            params = request.getParameterMap();
        }

        for (int i = 0; i < methodParams.length; i++) {
            Parameter p = methodParams[i];
            Class<?> paramType = p.getType();

            if (p.isAnnotationPresent(RequestFile.class)) {
                RequestFile rf = p.getAnnotation(RequestFile.class);
                String name = rf.value().isEmpty() ? p.getName() : rf.value();
                args[i] = extractMapFile(request, name, isMultipart);
                continue;
            }

            if (paramType.equals(MapFile.class)) {
                args[i] = extractMapFile(request, p.getName(), isMultipart);
                continue;
            }

            String paramName = p.getName();

            String value = null;
            if (isMultipart) {
                value = getMultipartTextPart(request, paramName);
            } else {
                String[] valuesArr = params != null ? params.get(paramName) : null;
                if (valuesArr != null && valuesArr.length > 0) {
                    value = valuesArr[0];
                }
            }

            if (value == null) {
                args[i] = defaultValueFor(paramType);
            } else {
                args[i] = convert(value, paramType);
            }
        }
        return args;
    }

    private static boolean isMultipartRequest(HttpServletRequest request) {
        String contentType = request.getContentType();
        if (contentType == null) return false;
        return contentType.toLowerCase().startsWith("multipart/");
    }

    private static String getMultipartTextPart(HttpServletRequest request, String name) throws IOException, ServletException {
        try {
            for (Part part : request.getParts()) {
                if (part.getName().equals(name)) {
                    String submitted = part.getSubmittedFileName();
                    if (submitted == null || submitted.isEmpty()) {
                        byte[] bytes = part.getInputStream().readAllBytes();
                        if (bytes.length > 0) {
                            return new String(bytes, StandardCharsets.UTF_8);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static MapFile extractMapFile(HttpServletRequest request, String name, boolean isMultipart) throws IOException, ServletException {
        if (!isMultipart) return null;
        try {
            for (Part part : request.getParts()) {
                if (part.getName().equals(name)) {
                    String submittedFileName = part.getSubmittedFileName();
                    if (submittedFileName == null || submittedFileName.isEmpty()) {
                        continue;
                    }
                    InputStream is = part.getInputStream();
                    byte[] bytes = is.readAllBytes();
                    String contentType = part.getContentType();
                    MapFile mf = new MapFile(submittedFileName, contentType, bytes);
                    return mf;
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static Object defaultValueFor(Class<?> type) {
        if (!type.isPrimitive()) return null;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == double.class) return 0.0d;
        if (type == float.class) return 0.0f;
        if (type == boolean.class) return false;
        if (type == short.class) return (short) 0;
        if (type == byte.class) return (byte) 0;
        if (type == char.class) return '\0';
        return null;
    }

    public static Object convert(String value, Class<?> type) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValueFor(type);
        }
        try {
            if (type == String.class) return value;
            if (type == int.class || type == Integer.class) return Integer.parseInt(value);
            if (type == long.class || type == Long.class) return Long.parseLong(value);
            if (type == double.class || type == Double.class) return Double.parseDouble(value);
            if (type == float.class || type == Float.class) return Float.parseFloat(value);
            if (type == boolean.class || type == Boolean.class) return Boolean.parseBoolean(value);
            if (type == short.class || type == Short.class) return Short.parseShort(value);
            if (type == byte.class || type == Byte.class) return Byte.parseByte(value);
            if (type == char.class || type == Character.class) return value.charAt(0);
        } catch (Exception e) {
            return defaultValueFor(type);
        }
        return null;
    }
}
