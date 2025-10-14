package tritium.launch;

import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import tritium.utils.network.HttpUtils;
import tritium.utils.other.WrappedInputStream;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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

        if (!tritiumDir.exists())
            tritiumDir.mkdirs();

        if (!depsDir.exists())
            depsDir.mkdirs();

        DownloadProgressWindow window = new DownloadProgressWindow();
        window.setVisible(true);

        this.parseAndDownloadDeps(window);
        window.dispose();
        this.launch(args);
    }

    @SneakyThrows
    private void launch(String[] args) {

        StringBuilder libs = new StringBuilder();

        for (File file : depsDir.listFiles()) {
            libs.append(file.getAbsolutePath()).append(File.pathSeparator);
        }

        String classPathBuilder = "\"" + System.getProperty("java.class.path") + File.pathSeparator + libs + "\"";

        ArrayList<String> jvmArgs = new ArrayList<>();

        String javaHome = System.getProperty("java.home");
        File f = new File(javaHome);
        f = new File(f, "bin");
        File javaExecutable = new File(f, "java.exe");

        if (!javaExecutable.exists()) {
            javaExecutable = new File(f, "java");

            if (!javaExecutable.exists()) {
                javaExecutable = new File(f, "javaw.exe");

                if (!javaExecutable.exists()) {
                    javaExecutable = new File(f, "javaw");

                    if (!javaExecutable.exists()) {
                        System.err.println("无法找到java可执行文件，退出...");
                        System.exit(-1);
                    }
                }
            }
        }

        jvmArgs.add(javaExecutable.getAbsolutePath());
        if (args == null || args.length == 0) {
            args = new String[]{"--version", "mcp", "--accessToken", "0", "--assetsDir", "assets", "--assetIndex", "1.8", "--userProperties", "{}"};
        }

        jvmArgs.add("-Dfile.encoding=UTF-8");

        jvmArgs.add("-cp");
        jvmArgs.add(classPathBuilder);
        jvmArgs.add("tritium.launch.Launcher");
        jvmArgs.addAll(Arrays.asList(args));

        System.out.println("启动参数: " + String.join(" ", jvmArgs));
        ProcessBuilder game = new ProcessBuilder(jvmArgs);
        Process process = game.inheritIO().start();
    }

    @SneakyThrows
    private void parseAndDownloadDeps(DownloadProgressWindow window) {
        InputStream is = DependencyDownloader.class.getResourceAsStream("/deps.txt");
        byte[] byteArray = IOUtils.toByteArray(is);
        is.close();
        String parse = new String(byteArray);

        String[] split = parse.split("\n");

        for (String s : split) {
            if (s.startsWith("#"))
                continue;

            String[] sp = s.split(":");

            String s1 = "";

            if (sp.length > 3) {
                s1 = "https://maven.aliyun.com/repository/central/" + sp[0].replaceAll("\\.", "/") + "/" + sp[1] + "/" + sp[3] + "/" + sp[1] + "-" + sp[3] + "-" + sp[2];
            } else {
                s1 = "https://maven.aliyun.com/repository/central/" + sp[0].replaceAll("\\.", "/") + "/" + sp[1] + "/" + sp[2] + "/" + sp[1] + "-" + sp[2];
            }

            String downloadUrl = s1.replaceAll("\r", "") + ".jar";
            String jarName = downloadUrl.substring(downloadUrl.lastIndexOf("/") + 1);

            File jarFile = new File(depsDir, jarName);

            if (!jarFile.exists()) {
                window.setStatusText(jarName);
                System.out.println(downloadUrl);
                try {
                    InputStream stream = new WrappedInputStream(HttpUtils.download(downloadUrl), new WrappedInputStream.ProgressListener() {
                        @Override
                        public void onProgress(double progress) {
                            window.setDownloadProgress((int) (progress * 100));
                        }

                        @Override
                        public void bytesRead(int bytesRead) {

                        }
                    });

                    OutputStream os = Files.newOutputStream(jarFile.toPath(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);

                    writeTo(stream, os);
                } catch (Exception e) {
                    window.setStatusText("下载失败: " + jarName);
                }
            }
        }
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

    public static void main(String[] args) {
        new DependencyDownloader().run(args);
    }

}
