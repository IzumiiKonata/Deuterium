package tritium.launch;

import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import tritium.utils.network.HttpUtils;
import tritium.utils.other.WrappedInputStream;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author IzumiiKonata
 * Date: 2025/10/14 09:22
 */
public class DependencyDownloader {

    final File tritiumDir = new File("Tritium");
    final File depsDir = new File(tritiumDir, "deps");

    public DependencyDownloader() {

    }

    public void run(String[] args) {
        if (!tritiumDir.exists()) {
            tritiumDir.mkdirs();
        }

        if (!depsDir.exists()) {
            depsDir.mkdirs();
        }
        DownloadProgressWindow window = new DownloadProgressWindow();
        window.setVisible(true);
        this.parseAndDownloadDeps(window);
        
        window.dispose();
        
        // 重新启动一个 java 并且将依赖塞进去
        this.launch(args);
    }

    @SneakyThrows
    private void launch(String[] args) {

        List<URL> paths = new ArrayList<>();
        for (File file : depsDir.listFiles()) {
            paths.add(file.toURI().toURL());
        }

        String classPath = System.getProperty("java.class.path");
        if (classPath != null && !classPath.isEmpty()) {
            String[] p = classPath.split(File.pathSeparator);
            for (String path : p) {
                try {
                    File file = new File(path);
                    paths.add(file.toURI().toURL());
                    System.out.println(file.getAbsolutePath());
                } catch (MalformedURLException e) {
                    System.err.println("无法转换路径: " + path);
                }
            }
        }

//        URLClassLoader classLoader = new ChildFirstURLClassLoader(paths.toArray(URL[]::new), Thread.currentThread().getContextClassLoader());
//
//        Class<?> aClass = classLoader.loadClass(Launcher.class.getName());
//        aClass.getMethod("main", String[].class).invoke(null, new Object[]{args});
//        classLoader.close();

        StringBuilder libs = new StringBuilder();

        for (File file : depsDir.listFiles()) {
            libs.append(file.getAbsolutePath()).append(File.pathSeparator);
        }

        String classPathBuilder = "\"" + System.getProperty("java.class.path") + File.pathSeparator + libs + "\"";

        ArrayList<String> jvmArgs = new ArrayList<>();

        String javaHome = System.getProperty("java.home");
        File javaBinDir = new File(javaHome, "bin");
        File javaExecutable = findJavaExecutable(javaBinDir);

        if (javaExecutable == null) {
            System.err.println("无法找到Java可执行文件，退出...");
            System.err.println("Cannot find Java executable, exiting...");
            System.exit(-1);
        }

        jvmArgs.add(javaExecutable.getAbsolutePath());

        // dbg
        if (args == null || args.length == 0) {
            args = new String[] {
                "--version", "mcp",
                "--accessToken", "0",
                "--assetsDir", "assets",
                "--assetIndex", "1.8",
                "--userProperties", "{}"
            };
        }

        jvmArgs.add("-Dfile.encoding=UTF-8");
        jvmArgs.add("-cp");
        jvmArgs.add(classPathBuilder);
        jvmArgs.add("tritium.launch.Launcher");
        jvmArgs.addAll(Arrays.asList(args));

//        System.out.println("Args: " + String.join(" ", jvmArgs));

        ProcessBuilder game = new ProcessBuilder(jvmArgs);
        Process proc = game.inheritIO().start();
        proc.waitFor();
    }

    /**
     * 找 java 可执行程序
     * @param javaBinDir Java的bin目录
     * @return Java 可执行文件，如果找不到则返回 null
     */
    private File findJavaExecutable(File javaBinDir) {
        String[] possibleExecutables = {"java.exe", "java", "javaw.exe", "javaw"};
        
        for (String executable : possibleExecutables) {
            File file = new File(javaBinDir, executable);
            if (file.exists()) {
                return file;
            }
        }
        
        return null;
    }

    /**
     * 从阿里云 maven 镜像仓库下载依赖
     *
     * @param window
     */
    @SneakyThrows
    private void parseAndDownloadDeps(DownloadProgressWindow window) {

        List<String> validFileNames = new ArrayList<>();

        // read deps
        InputStream is = DependencyDownloader.class.getResourceAsStream("/deps.txt");
        byte[] byteArray = IOUtils.toByteArray(is);
        is.close();
        String depsContent = new String(byteArray);

        String[] lines = depsContent.split("\n");

        for (String line : lines) {
            if (line.startsWith("#") || line.isBlank()) {
                continue;
            }

            String fullDownloadUrl = buildDownloadUrl(line);
            String jarName = fullDownloadUrl.substring(fullDownloadUrl.lastIndexOf("/") + 1);

            File jarFile = new File(depsDir, jarName);
            validFileNames.add(jarName);

            if (!jarFile.exists()) {
                window.setStatusText(jarName);
                System.out.println("URL: " + fullDownloadUrl);
                
                try {
                    InputStream stream = new WrappedInputStream(HttpUtils.download(fullDownloadUrl), new WrappedInputStream.ProgressListener() {
                        @Override
                        public void onProgress(double progress) {
                            window.setDownloadProgress((int) (progress * 100));
                        }

                        @Override
                        public void bytesRead(int bytesRead) {
                            // nop
                        }
                    });

                    OutputStream os = Files.newOutputStream(
                            jarFile.toPath(),
                            StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING,
                            StandardOpenOption.WRITE
                    );

                    writeTo(stream, os);
                } catch (Exception e) {
                    window.setStatusText("下载失败: " + jarName);
                    e.printStackTrace();
                }
            }
        }

        for (File file : depsDir.listFiles()) {
            if (file.isFile() && !validFileNames.contains(file.getName())) {
                try {
                    file.delete();
                } catch (Exception ignored) {
                }
            }
        }
    }

    private static String buildDownloadUrl(String line) {
        String[] parts = line.split(":");

        String baseUrl = "https://maven.aliyun.com/repository/central/";
        String downloadUrl;

        String groupIdPath = parts[0].replaceAll("\\.", "/");
        if (parts.length > 3) {
            // classifier
            downloadUrl = baseUrl + groupIdPath + "/" + parts[1] + "/" + parts[3] + "/" + parts[1] + "-" + parts[3] + "-" + parts[2];
        } else {
            downloadUrl = baseUrl + groupIdPath + "/" + parts[1] + "/" + parts[2] + "/" + parts[1] + "-" + parts[2];
        }

        return downloadUrl.replaceAll("\r", "") + ".jar";
    }

    @SneakyThrows
    public static void writeTo(InputStream src, OutputStream dest) {
        byte[] buffer = new byte[8192];
        int len;
        while ((len = src.read(buffer)) != -1) {
            dest.write(buffer, 0, len);
        }
        dest.flush();
    }

}