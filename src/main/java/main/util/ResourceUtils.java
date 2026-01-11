package main.util;

import java.io.InputStream;
import java.net.URL;

public final class ResourceUtils {
    private ResourceUtils() {
    }

    public static URL url(Class<?> context, String path) {
        String normalized = normalize(path);
        if (normalized == null) {
            System.err.println("Resource path is null.");
            return null;
        }
        URL url = context.getResource(normalized);
        if (url == null) {
            System.err.println("Resource not found: " + normalized);
        }
        return url;
    }

    public static String externalForm(Class<?> context, String path) {
        URL url = url(context, path);
        return url != null ? url.toExternalForm() : null;
    }

    public static InputStream stream(Class<?> context, String path) {
        String normalized = normalize(path);
        if (normalized == null) {
            System.err.println("Resource path is null.");
            return null;
        }
        InputStream is = context.getResourceAsStream(normalized);
        if (is == null) {
            System.err.println("Resource not found: " + normalized);
        }
        return is;
    }

    public static String normalize(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        return path.startsWith("/") ? path : "/" + path;
    }
}
