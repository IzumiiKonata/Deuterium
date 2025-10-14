import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import lombok.SneakyThrows;
import tech.konata.commons.ncm.math.DigestUtils;
import tech.konata.obfuscator.Dictionaries;
import tech.konata.obfuscator.Obfuscator;
import tech.konata.obfuscator.SessionInfo;
import tech.konata.obfuscator.exclusions.Exclusion;
import tech.konata.obfuscator.exclusions.ExclusionManager;
import tech.konata.obfuscator.transformers.obfuscators.ParameterHider;
import tech.konata.obfuscator.transformers.obfuscators.flow.*;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Tests {

    public Tests() {

    }

    @SneakyThrows
    public void run() {
        File base = new File(".");

        File testDir = new File(base, "Tests");

        if (!testDir.exists())
            testDir.mkdirs();

        Version version = Tritium.getVersion();

        String ver = version.getMajor() + "." + version.getMinor() + "." + version.getPatch();
        File workingDir = new File(testDir, ver);

        int count = 2;
        while (workingDir.exists()) {
            workingDir = new File(testDir, version.getMajor() + "." + version.getMinor() + "." + version.getPatch() + "_" + count);
            count++;
        }

        workingDir.mkdir();

        File artifact = new File("out\\artifacts\\Tritium\\Tritium.jar");

        if (!artifact.exists())
            throw new RuntimeException("?");

        File input = new File(workingDir, "Tritium_input.jar");
        Files.copy(artifact.toPath(), input.toPath());

        File obfnames = new File(workingDir, "obfuscate_names.txt");
        List<String> strings = ObfDictGen.gen(20000);

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
            s = s.replace("(repackage)", "catch_me_if_u_can");
            s = s.replace("(keepattributes)", "*");
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

        {

        }

        radonCfg.setInput(proguardObfuscated);
        radonCfg.setOutput(obfuscated);

        radonCfg.setLibraries(dependencies);

        ExclusionManager ex = new ExclusionManager();
        ex.addExclusion(new Exclusion("ingameime.*"));
        radonCfg.setExclusions(ex);

        radonCfg.setDictionaryType(Dictionaries.ALPHANUMERIC);

        radonCfg.setNoAnnotations(true);

        radonCfg.setTransformers(
                new ArrayList<>(
                        Arrays.asList(
//                                new CodeHider(),
//                                new AggressiveBlockSplitter(),
//                                new IfConfuser(),
                                new DaFlow(),
                                new ClassFolder(),
                                new CRCFucker(),
                                new TimeManipulator(),
                                new LocalVariables(true)
                        )
                )
        );

        Obfuscator obfuscator = new Obfuscator(radonCfg);
        obfuscator.run();
    }

    private List<String> getProguardConfigTemplate() {

        InputStream is = Tests.class.getResourceAsStream("/proguard_cfg_template.cfg");

        byte[] byteArray = IOUtils.toByteArray(is);

        return new ArrayList<>(Arrays.asList(new String(byteArray, StandardCharsets.UTF_8).split("\n")));

    }

    @SneakyThrows
    public static void main(String[] args) {
        new Tests().run();
    }

    @SneakyThrows
    private void runProcessBlocking(File runningDir, String... args) {
        ProcessBuilder pb = new ProcessBuilder(args);

        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);

        pb.directory(runningDir);

        pb.start().waitFor();
    }
}
