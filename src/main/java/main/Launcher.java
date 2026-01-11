package main;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class Launcher {
    private static final String EMBEDDED_LIB_DIR = "javafx-libs/";

    private Launcher() {
    }

    public static void main(String[] args) throws Exception {
        URL jarUrl = getCurrentJarUrl();
        List<URL> urls = new ArrayList<>();
        urls.add(jarUrl);

        String platformClassifier = resolvePlatformClassifier();
        Path tempDir = Files.createTempDirectory("MineSweeper-Lion-javafx-");
        tempDir.toFile().deleteOnExit();
        extractEmbeddedJars(jarUrl, tempDir, urls, platformClassifier);

        URLClassLoader appClassLoader = new URLClassLoader(
                urls.toArray(new URL[0]),
                ClassLoader.getSystemClassLoader().getParent()
        );
        Thread.currentThread().setContextClassLoader(appClassLoader);

        Class<?> mainClass = Class.forName("main.MainFx", true, appClassLoader);
        Method mainMethod = mainClass.getMethod("main", String[].class);
        mainMethod.invoke(null, (Object) args);
    }

    private static URL getCurrentJarUrl() {
        CodeSource source = Launcher.class.getProtectionDomain().getCodeSource();
        if (source == null) {
            throw new IllegalStateException("Cannot locate application jar.");
        }
        return source.getLocation();
    }

    private static void extractEmbeddedJars(URL jarUrl, Path tempDir, List<URL> urls, String platformClassifier)
            throws IOException {
        Path jarPath;
        try {
            jarPath = Path.of(jarUrl.toURI());
        } catch (Exception e) {
            throw new IOException("Failed to resolve jar path.", e);
        }

        if (!jarPath.toString().endsWith(".jar")) {
            Path devLibDir = Path.of("target", "classes", EMBEDDED_LIB_DIR);
            if (Files.isDirectory(devLibDir)) {
                try (var stream = Files.list(devLibDir)) {
                    stream.filter(p -> p.toString().endsWith(".jar"))
                            .filter(p -> shouldIncludeJar(p.getFileName().toString(), platformClassifier))
                            .forEach(p -> addUrl(p, tempDir, urls));
                }
            }
            return;
        }

        try (JarFile jarFile = new JarFile(jarPath.toFile())) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (!name.startsWith(EMBEDDED_LIB_DIR) || !name.endsWith(".jar")) {
                    continue;
                }
                String fileName = name.substring(EMBEDDED_LIB_DIR.length());
                if (!shouldIncludeJar(fileName, platformClassifier)) {
                    continue;
                }
                Path out = tempDir.resolve(fileName);
                try (InputStream in = jarFile.getInputStream(entry)) {
                    Files.copy(in, out, StandardCopyOption.REPLACE_EXISTING);
                }
                out.toFile().deleteOnExit();
                urls.add(out.toUri().toURL());
            }
        }
    }

    private static String resolvePlatformClassifier() {
        String osName = System.getProperty("os.name", "").toLowerCase();
        String osArch = System.getProperty("os.arch", "").toLowerCase();

        if (osName.contains("mac")) {
            if (osArch.contains("aarch64") || osArch.contains("arm64")) {
                return "mac-aarch64";
            }
            return "mac";
        }
        if (osName.contains("win")) {
            return "win";
        }
        if (osName.contains("linux")) {
            return "linux";
        }
        return "";
    }

    private static boolean shouldIncludeJar(String fileName, String platformClassifier) {
        if (!fileName.endsWith(".jar")) {
            return false;
        }
        if (fileName.contains("-mac-aarch64.jar")) {
            return "mac-aarch64".equals(platformClassifier);
        }
        if (fileName.contains("-mac.jar")) {
            return "mac".equals(platformClassifier);
        }
        if (fileName.contains("-win.jar")) {
            return "win".equals(platformClassifier);
        }
        if (fileName.contains("-linux.jar")) {
            return "linux".equals(platformClassifier);
        }
        return true;
    }

    private static void addUrl(Path jarPath, Path tempDir, List<URL> urls) {
        try {
            Path out = tempDir.resolve(jarPath.getFileName().toString());
            Files.copy(jarPath, out, StandardCopyOption.REPLACE_EXISTING);
            out.toFile().deleteOnExit();
            urls.add(out.toUri().toURL());
        } catch (IOException e) {
            throw new RuntimeException("Failed to prepare JavaFX jar: " + jarPath, e);
        }
    }
}
