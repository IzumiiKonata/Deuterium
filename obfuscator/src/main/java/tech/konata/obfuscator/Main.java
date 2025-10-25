package tech.konata.obfuscator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import lombok.SneakyThrows;
import tech.konata.obfuscator.transformers.obfuscators.ParameterHider;
import tritium.ncm.math.DigestUtils;
import tech.konata.obfuscator.exclusions.Exclusion;
import tech.konata.obfuscator.exclusions.ExclusionManager;
import tech.konata.obfuscator.transformers.obfuscators.flow.AggressiveBlockSplitter;
import tech.konata.obfuscator.transformers.obfuscators.miscellaneous.*;
import tech.konata.obfuscator.utils.IOUtils;
import tech.konata.utils.ObfDictGen;
import tritium.Tritium;
import tritium.utils.other.info.Version;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Main {

    public static final String PROPAGANDA_GARBAGE = "BiliBili @ IzumiKonata";
    public static final String REPACKAGE_NAME = "MatrixShield";

    public Main() {

    }

    @SneakyThrows
    public void run() {
        File base = new File(".");

        File releasesDir = new File(base, "Releases");

        if (!releasesDir.exists())
            releasesDir.mkdirs();

        Version version = Tritium.getVersion();

        String ver = version.getMajor() + "." + version.getMinor() + "." + version.getPatch();
        File workingDir = new File(releasesDir, ver);

        int count = 2;
        while (workingDir.exists()) {
            workingDir = new File(releasesDir, version.getMajor() + "." + version.getMinor() + "." + version.getPatch() + "_" + count);
            count++;
        }

        workingDir.mkdir();

        File artifact = new File("out\\artifacts\\Tritium\\Tritium.jar");

        if (!artifact.exists())
            throw new RuntimeException("?");

        File input = new File(workingDir, "Tritium_input.jar");
        Files.copy(artifact.toPath(), input.toPath());

        File obfnames = new File(workingDir, "obfuscate_names.txt");
        List<String> strings = ObfDictGen.gen();

        Files.write(obfnames.toPath(), String.join("\n", strings).getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE_NEW);


        List<String> cfg = this.getProguardConfigTemplate();

        File generatedCfg = new File(workingDir, "proguard.cfg");
        File mappings = new File(workingDir, "mappings_v" + ver + ".txt");
        File shrinked = new File(workingDir, "shrinked_v" + ver + ".txt");

        if (generatedCfg.exists()) {
            try {
                generatedCfg.delete();
                generatedCfg.createNewFile();
            } catch (Exception e) {
                System.err.println("生成高级护卫配置失败。");
                e.printStackTrace();
            }
        }

        File proguardObfuscated = new File(workingDir, "Tritium_proguard.jar");

        if (proguardObfuscated.exists()) {
            proguardObfuscated.delete();
        }

        PrintWriter pw;
        try {
            pw = new PrintWriter(generatedCfg, "utf-8");
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        List<File> dependencies = Stream.of("C:\\Program Files\\Java\\jdk-1.8\\jre\\lib\\rt.jar", "C:\\Program Files\\Java\\jdk-1.8\\jre\\lib\\jce.jar", "C:\\Program Files\\Java\\jdk-1.8\\jre\\lib\\ext\\jfxrt.jar").map(File::new).collect(Collectors.toList());

        File depsDir = new File("target\\dependency");

        dependencies.addAll(Arrays.asList(depsDir.listFiles()));

        String depsString = dependencies.stream().map(f -> "-libraryjars '" + f.getAbsolutePath() + "'").collect(Collectors.joining("\n"));

        for (String s : cfg) {

            s = s.replace("(injar)", input.getName());
            s = s.replace("(outjar)", proguardObfuscated.getName());
            s = s.replace("(libraries)", depsString);
            s = s.replace("(repackage)", Main.REPACKAGE_NAME);
            s = s.replace("(keepattributes)", "");
            s = s.replace("(mapping)", mappings.getName());
            s = s.replace("(shrinked)", shrinked.getName());

            pw.print(s);
        }
        pw.flush();
        pw.close();

        this.runProcessBlocking(workingDir, "E:\\Proguard\\bin\\proguard.bat", "@" + generatedCfg.getAbsolutePath(), "-forceprocessing");

        SessionInfo radonCfg = new SessionInfo();

        File obfuscated = new File(workingDir, "Tritium.jar");

        if (obfuscated.exists()) {
            obfuscated.delete();
        }

        radonCfg.setInput(proguardObfuscated);
        radonCfg.setOutput(obfuscated);

        radonCfg.setLibraries(dependencies);

        ExclusionManager ex = new ExclusionManager();
        ex.addExclusion(new Exclusion("ingameime.*"));
        ex.addExclusion(new Exclusion("org.lwjgl.*"));
        ex.addExclusion(new Exclusion("today.opai.api.*"));
        radonCfg.setExclusions(ex);

        radonCfg.setDictionaryType(Dictionaries.ALPHANUMERIC);

        radonCfg.setNoAnnotations(true);

        radonCfg.setTransformers(
            new ArrayList<>(
                Arrays.asList(
//                        new ParameterHider(),
//                        new CodeHider(),
                        new AggressiveBlockSplitter(),
                        new ClassFolder(),
                        new CRCFucker(),
                        new TimeManipulator(),
                        new LocalVariables(true)
                )
            )
        );

        Obfuscator obfuscator = new Obfuscator(radonCfg);
        obfuscator.run();

        File jsonOrig = new File(releasesDir, "Tritium.json");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        JsonObject jsonObject = gson.fromJson(new FileReader(jsonOrig), JsonObject.class);

        JsonObject downloads = jsonObject.getAsJsonObject("downloads");
        JsonObject client = downloads.getAsJsonObject("client");
        String sha1 = DigestUtils.sha1Hex(Files.newInputStream(obfuscated.toPath()));
        client.addProperty("sha1", sha1);
        client.addProperty("size", obfuscated.length());

        File json = new File(workingDir, "Tritium.json");
        try (FileWriter fw = new FileWriter(json)) {
            gson.toJson(jsonObject, fw);
            fw.flush();
        }

        File zip = new File(workingDir, "Tritium " + ver + ".zip");
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zip.toPath()))) {
            zos.setLevel(9);
            zos.putNextEntry(new ZipEntry("Tritium\\Tritium.jar"));
            zos.write(Files.readAllBytes(obfuscated.toPath()));

            zos.putNextEntry(new ZipEntry("Tritium\\Tritium.json"));
            zos.write(Files.readAllBytes(json.toPath()));

            List<String> comments = Arrays.asList(
                    "Tritium " + ver,
                    "Build Time: " + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()) + " (" + System.currentTimeMillis() + ")",
                    "Tritium.jar SHA1: " + sha1,
                    "Made with <3 by IzumiKonata",
                    "https://space.bilibili.com/357605683"
            );

            zos.setComment(String.join("\n", comments));
        }
    }

    private List<String> getProguardConfigTemplate() {

        InputStream is = Main.class.getResourceAsStream("/proguard_cfg_template.cfg");

        byte[] byteArray = IOUtils.toByteArray(is);

        return new ArrayList<>(Arrays.asList(new String(byteArray, StandardCharsets.UTF_8).split("\n")));

    }

    @SneakyThrows
    public static void main(String[] args) {
        new Main().run();
    }

    @SneakyThrows
    private void runProcessBlocking(File runningDir, String... args) {
        ProcessBuilder pb = new ProcessBuilder(args);

        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);

        pb.directory(runningDir);

        pb.start().waitFor();
    }
}
