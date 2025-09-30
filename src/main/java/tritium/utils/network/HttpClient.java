package tritium.utils.network;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author IzumiiKonata
 * @since 6/16/2023 9:36 AM
 */
public class HttpClient {

    @Getter
    @Setter
    private static int retryTimes = 10;
    @Getter
    private final String address;
    @Getter
    @Setter
    private String cookie = "";

    public HttpClient(String address) {
        this.address = address;
    }

    public static File download(String path, File targetFile) {
        return HttpClient.download(path, targetFile, 0);
    }

    private static File download(String path, File targetFile, int retry) {
        try {
            if (!targetFile.getParentFile().exists()) {
                targetFile.getParentFile().mkdirs();
            } else if (targetFile.exists()) {
                return targetFile;
            }

            // 统一资源
            URL url = new URL(path);
            URLConnection urlConnection = url.openConnection();
            HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
            // 设定请求的方法
            httpURLConnection.setInstanceFollowRedirects(true);
            httpURLConnection.setRequestMethod("GET");
            // 设置字符编码
            httpURLConnection.setRequestProperty("Charset", "UTF-8");
            httpURLConnection.setReadTimeout(5 * 1000);
            // 打开到此 URL 引用的资源的通信链接
            httpURLConnection.connect();

            BufferedInputStream bin = new BufferedInputStream(httpURLConnection.getInputStream());
            OutputStream out = Files.newOutputStream(targetFile.toPath());

            int size = 0;
            byte[] buf = new byte[1024];
            while ((size = bin.read(buf)) != -1) {
                out.write(buf, 0, size);
            }

            bin.close();
            out.close();
        } catch (Exception err) {
            if (retry >= retryTimes) {
                throw new RuntimeException(err.getMessage());
            }
            return HttpClient.download(path, targetFile, ++retry);
        }
        return targetFile;
    }

    public static InputStream downloadStream(String path) {
        return HttpClient.downloadStream(path, 0);
    }

    public static InputStream downloadStream(String path, int retry) {
        InputStream bin = null;
        try {
            // 统一资源
            URL url = new URL(path);
            URLConnection urlConnection = url.openConnection();
            HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
            // 设定请求的方法
            httpURLConnection.setInstanceFollowRedirects(true);
            httpURLConnection.setRequestMethod("GET");
            // 设置字符编码
            httpURLConnection.setRequestProperty("Charset", "UTF-8");
            httpURLConnection.setReadTimeout(5 * 1000);
            // 打开到此 URL 引用的资源的通信链接
            httpURLConnection.connect();

            bin = httpURLConnection.getInputStream();
        } catch (Exception err) {
            if (retry >= retryTimes) {
                throw new RuntimeException(err.getMessage());
            }
            return HttpClient.downloadStream(path, ++retry);
        }
        return bin;
    }

    public String getUrl(String path) {
        return address + path;
    }

    public HttpResult GET(String api, Map<String, Object> data) {
        return this.request(this.getUrl(api), data, (HttpURLConnection connection) -> {
            try {
                connection.setRequestMethod("GET");
            } catch (ProtocolException exception) {
                exception.printStackTrace();
            }
            return connection;
        }, 0);
    }

    public HttpResult POST(String api, Map<String, Object> data) {
        return this.request(this.getUrl(api), data, (HttpURLConnection connection) -> {
            try {
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setDoInput(true);
            } catch (ProtocolException exception) {
                exception.printStackTrace();
            }
            return connection;
        }, 0);
    }

/*    public JsonObject POST_API(String api, Map<String, Object> data) {
        HttpResult result = this.POST(api, data);
        JsonObject json = result.toJson();

        int code = json.get("code").getAsInt();
        if (code == 301) {
            throw new RuntimeException("NO COOKIE");
        }

        if (code != 200) {
            if (json.has("msg")) {
                throw new RuntimeException("code = " + json.get("code").getAsInt() + ", " + json.get("msg").getAsString());
            }

            throw new RuntimeException("code = " + json.get("code").getAsInt() + ", " + json.get("msg").getAsString());
        }

        return json;
    }*/

/*    public String POST_LOGIN(String path, Map<String, Object> data) {
        HttpResult result = this.POST(path, data);
        JsonObject json = result.toJson();

        int code = json.get("code").getAsInt();

        if (code != 200) {
            throw new RuntimeException("login: code = " + code + "," + json);
        }

        return result.getSetCookie();
    }*/

    private HttpResult request(String httpUrl, Map<String, Object> params, Connection connection, int retry) {
        HttpURLConnection httpConnection = null;
        InputStream inputStream = null;
        try {
            String url = mapToString(httpUrl, params, "?");
            //创建连接
            httpConnection = this.setRequestHeader(connection.set((HttpURLConnection) new URL(url).openConnection()));
//            System.out.println("COOKIE: " + cookie);
//            httpConnection.setRequestProperty("cookie", cookie);
/*            if(params != null){
                httpConnection.getOutputStream().write(this.extractParams(params));
            }*/
            //设置连接超时时间
            httpConnection.setReadTimeout(5 * 1000);
            httpConnection.connect();
            //获取响应数据
            int code = httpConnection.getResponseCode();
            if (code == 200) {
                inputStream = httpConnection.getInputStream();
                return new HttpResult(code, true, this.readFully(inputStream), httpConnection.getHeaderFields().get("Set-Cookie"));
            } else {
                inputStream = httpConnection.getErrorStream();
                return new HttpResult(code, false, this.readFully(inputStream), httpConnection.getHeaderFields().get("Set-Cookie"));
            }
        } catch (Exception err) {
            err.printStackTrace();
            if (retry >= retryTimes) {
                throw new RuntimeException("Max retry times reached");
            }
            return this.request(httpUrl, params, connection, ++retry);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }
            //关闭远程连接
            assert httpConnection != null;
            httpConnection.disconnect();
        }
    }

    @SneakyThrows
    private byte[] readFully(InputStream is) {
        return IOUtils.toByteArray(is);
    }

    @SneakyThrows
    private byte[] extractParams(Map<String, Object> params) {
        StringBuilder dataStr = new StringBuilder();
        for (Map.Entry<String, Object> value : params.entrySet()) {
            dataStr.append(value.getKey()).append("=").append(URLEncoder.encode(String.valueOf(value.getValue()), "UTF-8")).append("&");
        }
        return dataStr.toString().getBytes();
    }

    private HttpURLConnection setRequestHeader(HttpURLConnection httpConnection) {
        httpConnection.setRequestProperty("cookie", this.getCookie());
        return httpConnection;
    }

    /**
     * map转成string
     */
    public String mapToString(String url, Map<String, Object> params, String first) {
        StringBuilder sb;
        if (url != null) {
            sb = new StringBuilder(url);
        } else {
            sb = new StringBuilder();
        }
        if (params != null) {
            boolean isFirst = true;
            for (String key : params.keySet()) {
                if (isFirst) {
                    if (first != null) {
                        sb.append(first);
                    }
                    isFirst = false;
                } else {
                    sb.append("&");
                }
                sb.append(key);
                sb.append("=");
                sb.append(params.get(key));
            }
        }
        return sb.toString();
    }

    public boolean hasCookie() {
        return !cookie.isEmpty();
    }

    private interface Connection {
        HttpURLConnection set(HttpURLConnection connection);
    }

    public class HttpResult {
        public final int code;
        public final boolean status;
        private final byte[] data;
        private final List<String> cookies;

        public HttpResult(int code, boolean status, byte[] data, List<String> cookies) {
            this.code = code;
            this.status = status;
            this.cookies = cookies;

            if (data == null) {
                byte[] edata = {};
                this.data = edata;
                return;
            }
            this.data = data;
        }

        public String toString() {
            return new String(this.data, StandardCharsets.UTF_8);
        }

        public JsonObject toJson() {
            Gson gson = new Gson();
            JsonReader reader = new JsonReader(new StringReader(this.toString()));
            reader.setLenient(true);

            return gson.fromJson(reader, JsonObject.class);
        }

        public String getSetCookie() {
            Map<String, String> cookiesMap = new HashMap<>();
            for (String cookie : this.cookies) {
                String[] cookiekeys = cookie.split("; ")[0].split("=");
                if (cookiekeys.length == 1) {
                    cookiesMap.put(cookiekeys[0], "");
                    continue;
                }
                cookiesMap.put(cookiekeys[0], cookiekeys[1]);
            }

            StringBuilder cookieData = new StringBuilder();
            for (Map.Entry<String, String> cookiekeys : cookiesMap.entrySet()) {
                cookieData.append(cookiekeys.getKey()).append("=").append(cookiekeys.getValue()).append("; ");
            }
            return cookieData.toString();
        }
    }

}
