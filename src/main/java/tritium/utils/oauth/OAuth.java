package tritium.utils.oauth;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpServer;
import lombok.*;
import tritium.utils.other.multithreading.MultiThreadingUtil;
import tritium.utils.network.HttpUtils;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * the microsoft login utils
 *
 * @author IzumiiKonata
 * @since 2022/6/29 21:22
 */
public class OAuth {


    public void logIn(LoginCallback callback) {
        MultiThreadingUtil.runAsync(() -> {
            try {
                Map<String, String> authParams = new HashMap<>();

                authParams.put("client_id", "52fbe3f4-6688-4f14-bfd9-f23b40fa5edd");
                authParams.put("response_type", "code");
                authParams.put("redirect_uri", "http://127.0.0.1:57134");
                authParams.put("scope", "XboxLive.signin offline_access");

                String url = this.buildURL("https://login.live.com/oauth20_authorize.srf", authParams, "?");

                // browse the url
                Desktop.getDesktop().browse(new URI(url));
                callback.setStatus("oauth.waiting");
                HttpServer httpServer = HttpServer.create(new InetSocketAddress(57134), 0);
                AtomicBoolean ran = new AtomicBoolean(false);
                // Handle requests
                httpServer.createContext("/", handler -> {
                    // make sure the request is only requested once
                    if (ran.get()) {
                        return;
                    }

                    ran.set(true);
                    // got the token, and close the server
                    var html = "<html><body><h1>Obtained ur token successfully.</h1><h3>Now you can return to minecraft.</h3></body></html>\n";
                    handler.sendResponseHeaders(200, 0);
                    var bytes = html.getBytes(StandardCharsets.UTF_8);
                    var os = handler.getResponseBody();
                    os.write(bytes);
                    os.close();

                    httpServer.stop(2);
                    var code = handler.getRequestURI().toString();
                    code = code.substring(code.lastIndexOf('=') + 1);
                    callback.setStatus("oauth.gettingtoken");
                    Map<String, String> tokenParams = new HashMap<>();
                    tokenParams.put("client_id", "52fbe3f4-6688-4f14-bfd9-f23b40fa5edd");
                    tokenParams.put("code", code);
                    tokenParams.put("grant_type", "authorization_code");
                    tokenParams.put("redirect_uri", "http://127.0.0.1:57134");
//            var oauthJson = this.httpRequest("https://login.live.com/oauth20_token.srf", tokenParams, null, "POST", "application/x-www-form-urlencoded");
                    var oauthJson = HttpUtils.postString("https://login.live.com/oauth20_token.srf", tokenParams);

                    this.doLogin(oauthJson, callback);

                });
                httpServer.setExecutor(null);
                httpServer.start();
            } catch (Exception e) {
                callback.onFailed(e);
            }
        });
    }


    @SneakyThrows
    public void refresh(String refreshToken, LoginCallback callback) {
        MultiThreadingUtil.runAsync(() -> {
            try {
                callback.setStatus("oauth.gettingtoken");
                Map<String, String> tokenParams = new HashMap<>();
                tokenParams.put("client_id", "52fbe3f4-6688-4f14-bfd9-f23b40fa5edd");
                tokenParams.put("refresh_token", refreshToken);
                tokenParams.put("grant_type", "refresh_token");
                tokenParams.put("redirect_uri", "http://127.0.0.1:57134");
                var oauthJson = HttpUtils.postString("https://login.live.com/oauth20_token.srf", tokenParams);

                this.doLogin(oauthJson, callback);
            } catch (Exception e) {
                callback.onFailed(e);
            }
        });
    }

    @SneakyThrows
    private void doLogin(String oauthJson, LoginCallback callback) {
        try {
            Gson gson = (new GsonBuilder().disableHtmlEscaping()).create();

            var loginAccessToken = gson.fromJson(oauthJson, JsonObject.class).get("access_token").getAsString();
            var refreshToken = gson.fromJson(oauthJson, JsonObject.class).get("refresh_token").getAsString();
            callback.setStatus("oauth.signingintoxbl");
            Map<String, Object> XBLAuthParams = new HashMap<>();
            Map<String, String> XBLAuthProperties = new HashMap<>();

            XBLAuthProperties.put("AuthMethod", "RPS");
            XBLAuthProperties.put("SiteName", "user.auth.xboxlive.com");
            XBLAuthProperties.put("RpsTicket", "d=" + loginAccessToken);

            XBLAuthParams.put("Properties", XBLAuthProperties);
            XBLAuthParams.put("RelyingParty", "http://auth.xboxlive.com");
            XBLAuthParams.put("TokenType", "JWT");


            var xblJson = this.httpRequest("https://user.auth.xboxlive.com/user/authenticate", XBLAuthParams, null, "POST", "application/json");

            var xbl = gson.fromJson(xblJson, JsonObject.class);
            var xblToken = xbl.get("Token").getAsString();
            var xblUhs = xbl.get("DisplayClaims").getAsJsonObject().get("xui").getAsJsonArray().get(0).getAsJsonObject().get("uhs").getAsString();

            callback.setStatus("oauth.signingintoxsts");
            Map<String, Object> XSTSAuthParams = new HashMap<>();
            Map<String, Object> XSTSAuthProperties = new HashMap<>();

            XSTSAuthProperties.put("SandboxId", "RETAIL");
            List<String> userTokens = Collections.singletonList(xblToken);
            XSTSAuthProperties.put("UserTokens", userTokens);

            XSTSAuthParams.put("Properties", XSTSAuthProperties);
            XSTSAuthParams.put("RelyingParty", "rp://api.minecraftservices.com/");
            XSTSAuthParams.put("TokenType", "JWT");

            var xstsJson = this.httpRequest("https://xsts.auth.xboxlive.com/xsts/authorize", XSTSAuthParams, null, "POST", "application/json");

            var xsts = gson.fromJson(xstsJson, JsonObject.class);
            if (xsts.get("XErr") != null) {
                XSTSError error = new XSTSError(xsts.get("XErr").getAsLong());
                error.printStackTrace();
                callback.setStatus(error.getErrMSG());
                callback.onFailed(new RuntimeException(error.getErrMSG()));
            }

            var xstsToken = xsts.get("Token").getAsString();
            var xstsUhs = xsts.get("DisplayClaims").getAsJsonObject().get("xui").getAsJsonArray().get(0).getAsJsonObject().get("uhs").getAsString();

            callback.setStatus("oauth.gettingmctoken");
            Map<String, Object> mojangLoginParams = new HashMap<>();
            mojangLoginParams.put("identityToken", "XBL3.0 x=" + xstsUhs + ";" + xstsToken);

            var mojangLoginJson = this.httpRequest("https://api.minecraftservices.com/authentication/login_with_xbox", mojangLoginParams, null, "POST", "application/json");

            var mojangResponse = gson.fromJson(mojangLoginJson, JsonObject.class);
            var minecraftToken = mojangResponse.get("access_token").getAsString();
//        var expiresIn = mojangResponse.get("expires_in").getAsLong();
            var accessToken = minecraftToken;
            callback.setStatus("oauth.gettinguserprofile");
            Map<String, String> checkProductHeaders = new HashMap<>();

            checkProductHeaders.put("Authorization", "Bearer " + minecraftToken);
            var profileJson = HttpUtils.getString("https://api.minecraftservices.com/minecraft/profile", null, checkProductHeaders);
            var profile = gson.fromJson(profileJson, JsonObject.class);

            if (profile.get("error") != null) {
//            this.errorOccurred = true;
                callback.setStatus("This account does not own Minecraft!");
                callback.onFailed(new RuntimeException("This account does not own Minecraft!"));
                return;
            }

            var userUUID = profile.get("id").getAsString();
            var userName = profile.get("name").getAsString();

            callback.onSucceed(userUUID, userName, accessToken, refreshToken);
        } catch (Exception e) {
            callback.onFailed(e);
        }
    }


    @SneakyThrows
    private String httpRequest(String url, Map<String, Object> data, Map<String, String> headers, String method, String mediaType) {

        try {
            String result;

            if (url == null || url.trim().isEmpty()) {
                return null;
            }

            @Cleanup
            OutputStreamWriter writer = null;
            @Cleanup
            BufferedReader in = null;

            URL httpUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
            if (method.equalsIgnoreCase("POST") || method.equalsIgnoreCase("PUT")) {
                conn.setDoOutput(true);
                conn.setUseCaches(false);
            }
            conn.setReadTimeout(8000);
            conn.setConnectTimeout(5000);
            conn.setRequestMethod(method);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            conn.setRequestProperty("Accept-Charset", "utf-8");
            conn.setRequestProperty("Content-Type", mediaType);
            // 添加请求�?
            if (headers != null) {
                for (String key : headers.keySet()) {
                    conn.setRequestProperty(key, headers.get(key));
                }
            }
            Gson gson = (new GsonBuilder().disableHtmlEscaping()).create();
            String params = gson.toJson(data);

            // 添加参数
            if (params != null) {
                conn.setRequestProperty("Content-Length", String.valueOf(params.length()));
                writer = new OutputStreamWriter(conn.getOutputStream());
                writer.write(params);
                writer.flush();
            }
            // 判断连接状�??
            if (conn.getResponseCode() >= 300) {
                new Exception("HTTP Request is not success, Response code is " + conn.getResponseCode()).printStackTrace();
            }
            // 获取返回数据
            StringBuilder sb = new StringBuilder();
            InputStream input = conn.getInputStream();
            in = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }
            result = sb.toString();
            // 断开连接
            conn.disconnect();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return httpRequest(url, data, headers, method, mediaType);
        }

    }

    @SneakyThrows
    private String buildURL(@NonNull String url, @NonNull Map<String, String> params, @NonNull String first) {
        StringBuilder sb;
        sb = new StringBuilder(url);
        sb.append(first);
        boolean isFirst = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!isFirst) {
                sb.append("&");
            } else {
                isFirst = false;
            }
            sb.append(entry.getKey());
            sb.append("=");
            sb.append(URLEncoder.encode(entry.getValue(), "UTF-8"));

        }
        return sb.toString();
    }

    @Getter
    private static class XSTSError extends Throwable {
        private final long errCode;
        private final String errMSG;

        public XSTSError(long errCode) {
            this.errCode = errCode;
            this.errMSG = this.getErrMSG(this.errCode);
        }

        private String getErrMSG(long code) {
            if (code == 2148916233L) {
                return "The account doesn't have an Xbox account. Once they sign up for one (or login through minecraft.net to create one) then they can proceed with the login. This shouldn't happen with accounts that have purchased Minecraft with a Microsoft account, as they would've already gone through that Xbox signup process.";
            }

            if (code == 2148916235L) {
                return "The account is from a country where Xbox Live is not available/banned";
            }

            if (code == 2148916236L) {
                return "The account needs adult verification on Xbox page. (South Korea)";
            }

            if (code == 2148916237L) {
                return "The account needs adult verification on Xbox page. (South Korea)";
            }

            if (code == 2148916238L) {
                return "The account is a child (under 18) and cannot proceed unless the account is added to a Family by an adult. This only seems to occur when using a custom Microsoft Azure application. When using the Minecraft launchers client id, this doesn't trigger.";
            }

            return "Unknown Error (" + code + ")";
        }
    }

    @Getter
    private static class AccountError {
        private final String errorMSG;

        public AccountError(long errCode) {
            this.errorMSG = this.getErrorMSG(errCode);
        }

        private String getErrorMSG(long errorCode) {
            if (errorCode == -1) {
                return "The account does not own minecraft!";
            }

            return "Unknown Error (" + errorCode + ")";
        }
    }

    public interface LoginCallback {

        void onSucceed(String uuid, String userName, String token, String refreshToken);

        void onFailed(Exception e);

        void setStatus(String status);

    }

//    public interface RefreshCallback

}
