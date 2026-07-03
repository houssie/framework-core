package framework.mg.itu.annotation;

import java.util.Objects;

public class UrlMethod {
    private String url;
    private String httpMethod;

    public UrlMethod(String url, String httpMethod) {
        this.url = url;
        this.httpMethod = httpMethod;
    }

    public String getUrl() {
        return url;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UrlMethod urlMethod = (UrlMethod) o;
        return Objects.equals(url, urlMethod.url) &&
               Objects.equals(httpMethod, urlMethod.httpMethod);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, httpMethod);
    }

    @Override
    public String toString() {
        return "UrlMethod{" + "url='" + url + '\'' + ", httpMethod='" + httpMethod + '\'' + '}';
    }
}