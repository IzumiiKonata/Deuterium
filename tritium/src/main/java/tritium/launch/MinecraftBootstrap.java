package tritium.launch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.properties.PropertyMap;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfiguration;
import net.minecraft.util.Session;
import org.lwjgl.system.Configuration;
import tritium.Tritium;

import java.io.File;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.util.List;

/**
 * @author IzumiiKonata
 * Date: 2026/1/24 11:35
 */
public class MinecraftBootstrap {

    public static void main(String[] args) {

        Configuration.MEMORY_ALLOCATOR.set("jemalloc");
        Configuration.DISABLE_CHECKS.set(true);
        Configuration.DISABLE_FUNCTION_CHECKS.set(true);
        Configuration.DISABLE_HASH_CHECKS.set(true);
        Configuration.DEBUG.set(false);
        Configuration.DEBUG_FUNCTIONS.set(false);

        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("demo");
        parser.accepts("fullscreen");
        parser.accepts("checkGlErrors");

        // server host to be automatically joined
        OptionSpec<String> serverHostSpec = parser
                .accepts("server").withRequiredArg();

        // server port
        OptionSpec<Integer> serverPortSpec = parser
                .accepts("port")
                .withRequiredArg()
                .ofType(Integer.class)
                .defaultsTo(25565);

        // .minecraft directory location
        OptionSpec<File> gameDirSpec = parser
                .accepts("gameDir")
                .withRequiredArg()
                .ofType(File.class)
                .defaultsTo(new File("."));

        // assets directory location
        OptionSpec<File> assetsDirSpec = parser
                .accepts("assetsDir")
                .withRequiredArg()
                .ofType(File.class);

        // resource pack directory location
        OptionSpec<File> resPackDirSpec = parser
                .accepts("resourcePackDir")
                .withRequiredArg()
                .ofType(File.class);

        // proxy host
        OptionSpec<String> proxyHostSpec = parser
                .accepts("proxyHost")
                .withRequiredArg();
        
        // proxy port
        OptionSpec<Integer> proxyPortSpec = parser
                .accepts("proxyPort")
                .withRequiredArg()
                .defaultsTo("8080", new String[0])
                .ofType(Integer.class);
        
        // proxy user
        OptionSpec<String> proxyUserSpec = parser
                .accepts("proxyUser")
                .withRequiredArg();
        
        // proxy password
        OptionSpec<String> proxyPassSpec = parser
                .accepts("proxyPass")
                .withRequiredArg();
        
        // username
        OptionSpec<String> usernameSpec = parser
                .accepts("username")
                .withRequiredArg()
                .defaultsTo(Tritium.NAME + System.currentTimeMillis() % 1000L);
        
        // uuid
        OptionSpec<String> uuidSpec = parser
                .accepts("uuid")
                .withRequiredArg();
        
        // access token
        OptionSpec<String> accessTokenSpec = parser
                .accepts("accessToken")
                .withRequiredArg()
                .required();
        
        // version
        OptionSpec<String> versionSpec = parser
                .accepts("version")
                .withRequiredArg()
                .required();
        
        // window width
        OptionSpec<Integer> widthSpec = parser
                .accepts("width")
                .withRequiredArg()
                .ofType(Integer.class)
                .defaultsTo(1600);
        
        // window height
        OptionSpec<Integer> heightSpec = parser
                .accepts("height")
                .withRequiredArg()
                .ofType(Integer.class)
                .defaultsTo(900);
        
        // user properties
        OptionSpec<String> userPropertiesSpec = parser
                .accepts("userProperties")
                .withRequiredArg()
                .defaultsTo("{}");
        
        // profile properties
        OptionSpec<String> profilePropertiesSpec = parser
                .accepts("profileProperties")
                .withRequiredArg()
                .defaultsTo("{}");
        
        // asset index
        OptionSpec<String> assetIndexSpec = parser
                .accepts("assetIndex")
                .withRequiredArg();
        
        // user type
        OptionSpec<String> userTypeSpec = parser
                .accepts("userType")
                .withRequiredArg()
                .defaultsTo("legacy");
        
        // non options
        OptionSpec<String> nonOptionsSpec = parser
                .nonOptions();
        OptionSet optionset = parser.parse(args);

        List<String> list = optionset.valuesOf(nonOptionsSpec);

        if (!list.isEmpty()) {
            System.out.println("Completely ignored arguments: " + list);
        }

        String proxyHost = optionset.valueOf(proxyHostSpec);
        Proxy proxy = Proxy.NO_PROXY;

        if (proxyHost != null) {
            try {
                proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxyHost, optionset.valueOf(proxyPortSpec)));
            } catch (Exception ignored) {
            }
        }

        final String proxyUsername = optionset.valueOf(proxyUserSpec);
        final String proxyPassword = optionset.valueOf(proxyPassSpec);

        if (!proxy.equals(Proxy.NO_PROXY) && isNullOrEmpty(proxyUsername) && isNullOrEmpty(proxyPassword)) {
            Authenticator.setDefault(new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(proxyUsername, proxyPassword.toCharArray());
                }
            });
        }

        int displayWidth = optionset.valueOf(widthSpec);
        int displayHeight = optionset.valueOf(heightSpec);

        if (displayWidth <= 1600 || displayHeight <= 900) {
            displayWidth = 1600;
            displayHeight = 900;
        }

        boolean fullScreen = optionset.has("fullscreen");
        boolean checkGlErrors = optionset.has("checkGlErrors");
        boolean demoMode = optionset.has("demo");

        String gameVersion = optionset.valueOf(versionSpec);
        Gson gson = (new GsonBuilder()).registerTypeAdapter(PropertyMap.class, new PropertyMap.Serializer()).create();

        PropertyMap userPropertyMap = gson.fromJson(optionset.valueOf(userPropertiesSpec), PropertyMap.class);
        PropertyMap profilePropsMap = gson.fromJson(optionset.valueOf(profilePropertiesSpec), PropertyMap.class);

        File gameDir = optionset.valueOf(gameDirSpec);
        File assetsDir = optionset.has(assetsDirSpec) ? optionset.valueOf(assetsDirSpec) : new File(gameDir, "assets/");
        File resPackDir = optionset.has(resPackDirSpec) ? optionset.valueOf(resPackDirSpec) : new File(gameDir, "resourcepacks/");

        String userName = usernameSpec.value(optionset);
        String uuid = optionset.has(uuidSpec) ? uuidSpec.value(optionset) : userName;
        String assetIndex = optionset.has(assetIndexSpec) ? assetIndexSpec.value(optionset) : null;
        String accessToken = accessTokenSpec.value(optionset);
        String userType = userTypeSpec.value(optionset);

        Session session = new Session(userName, uuid, accessToken, userType);

        // auto join
        String serverHost = optionset.valueOf(serverHostSpec);
        Integer serverPort = optionset.valueOf(serverPortSpec);

        GameConfiguration gameconfiguration = new GameConfiguration(
                new GameConfiguration.UserInformation(
                        session,
                        userPropertyMap,
                        profilePropsMap,
                        proxy
                ),
                new GameConfiguration.DisplayInformation(
                        displayWidth,
                        displayHeight,
                        fullScreen,
                        checkGlErrors
                ),
                new GameConfiguration.FolderInformation(
                        gameDir,
                        resPackDir,
                        assetsDir,
                        assetIndex),
                new GameConfiguration.GameInformation(
                        demoMode,
                        gameVersion
                ),
                new GameConfiguration.ServerInformation(
                        serverHost, serverPort
                )
        );

        Runtime.getRuntime().addShutdownHook(new Thread("Client Shutdown Thread") {
            public void run() {
                Minecraft.stopIntegratedServer();
            }
        });

        Thread.currentThread().setName("Client thread");
        new Minecraft(gameconfiguration).run();
    }

    private static boolean isNullOrEmpty(String str) {
        return str != null && !str.isEmpty();
    }

}
