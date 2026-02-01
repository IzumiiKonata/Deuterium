package tritium.utils.network;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * http请求工具
 * Created by wangfan on 2018-12-14 上午 8:38.
 */
public class HttpUtils {

    /**
     * get请求
     *
     * @param params 请求参数
     */
    public static InputStream get(String url, Map<String, String> params) throws IOException {
        return get(url, params, null);
    }

    public static String getString(String url, Map<String, String> params) throws IOException {
        return readString(get(url, params));
    }

    public static String deleteString(String url, Map<String, String> params, Map<String, String> headers) throws IOException {
        return readString(delete(url, params, headers));
    }

    public static String putString(String url, Map<String, String> params, Map<String, String> headers) throws IOException {
        return readString(put(url, params, headers));
    }

    /**
     * get请求
     *
     * @param params  请求参数
     * @param headers 请求�?
     */
    public static InputStream get(String url, Map<String, String> params, Map<String, String> headers) throws IOException {
        return request(mapToString(url, params, "?"), null, headers, "GET");
    }

    public static String getString(String url, Map<String, String> params, Map<String, String> headers) throws IOException {
        return readString(get(url, params, headers));
    }

    /**
     * 异步get请求
     *
     * @param params       请求参数
     * @param onHttpResult 请求回调
     */
    public static void getAsync(String url, Map<String, String> params, OnHttpResult onHttpResult) {
        getAsync(url, params, null, onHttpResult);
    }

    /**
     * 异步get请求
     *
     * @param params       请求参数
     * @param headers      请求�?
     * @param onHttpResult 请求回调
     */
    public static void getAsync(String url, Map<String, String> params, Map<String, String> headers, OnHttpResult onHttpResult) {
        requestAsync(mapToString(url, params, "?"), null, headers, "GET", onHttpResult);
    }

    /**
     * post请求
     *
     * @param params 请求参数
     */
    public static InputStream post(String url, Map<String, String> params) throws IOException {
        return post(url, params, null);
    }

    public static String postString(String url, Map<String, String> params) throws IOException {
        return readString(post(url, params));
    }

    public static String postString(String url, Map<String, String> params, Map<String, String> headers) throws IOException {
        return readString(post(url, params, headers));
    }

    /**
     * post请求
     *
     * @param params  请求参数
     * @param headers 请求�?
     */
    public static InputStream post(String url, Map<String, String> params, Map<String, String> headers) throws IOException {
        return request(url, mapToString(null, params, null), headers, "POST");
    }

    /**
     * 异步post请求
     *
     * @param params 请求参数
     */
    public static void postAsyn(String url, Map<String, String> params, OnHttpResult onHttpResult) {
        postAsyn(url, params, null, onHttpResult);
    }

    /**
     * 异步post请求
     *
     * @param params  请求参数
     * @param headers 请求�?
     */
    public static void postAsyn(String url, Map<String, String> params, Map<String, String> headers, OnHttpResult onHttpResult) {
        requestAsync(url, mapToString(null, params, null), headers, "POST", onHttpResult);
    }

    /**
     * put请求
     *
     * @param params 请求参数
     */
    public static InputStream put(String url, Map<String, String> params) throws IOException {
        return put(url, params, null);
    }

    /**
     * put请求
     *
     * @param params  请求参数
     * @param headers 请求�?
     */
    public static InputStream put(String url, Map<String, String> params, Map<String, String> headers) throws IOException {
        return request(url, mapToString(null, params, null), headers, "PUT");
    }

    /**
     * 异步put请求
     *
     * @param params 请求参数
     */
    public static void putAsyn(String url, Map<String, String> params, OnHttpResult onHttpResult) {
        putAsyn(url, params, null, onHttpResult);
    }

    /**
     * 异步put请求
     *
     * @param params  请求参数
     * @param headers 请求�?
     */
    public static void putAsyn(String url, Map<String, String> params, Map<String, String> headers, OnHttpResult onHttpResult) {
        requestAsync(url, mapToString(null, params, null), headers, "PUT", onHttpResult);
    }

    /**
     * delete请求
     *
     * @param params 请求参数
     */
    public static InputStream delete(String url, Map<String, String> params) throws IOException {
        return delete(url, params, null);
    }

    /**
     * delete请求
     *
     * @param params  请求参数
     * @param headers 请求�?
     */
    public static InputStream delete(String url, Map<String, String> params, Map<String, String> headers) throws IOException {
        return request(mapToString(url, params, "?"), null, headers, "DELETE");
    }

    /**
     * 异步delete请求
     *
     * @param params 请求参数
     */
    public static void deleteAsync(String url, Map<String, String> params, OnHttpResult onHttpResult) {
        deleteAsync(url, params, null, onHttpResult);
    }

    /**
     * 异步delete请求
     *
     * @param params  请求参数
     * @param headers 请求�?
     */
    public static void deleteAsync(String url, Map<String, String> params, Map<String, String> headers, OnHttpResult onHttpResult) {
        requestAsync(mapToString(url, params, "?"), null, headers, "DELETE", onHttpResult);
    }

    /**
     * 表单请求
     *
     * @param params  请求参数
     * @param headers 请求�?
     * @param method  请求方式
     */
    public static InputStream request(String url, String params, Map<String, String> headers, String method) throws IOException {
        return request(url, params, headers, method, "application/x-www-form-urlencoded");
    }

    /**
     * http请求
     *
     * @param params    请求参数
     * @param headers   请求�?
     * @param method    请求方式
     * @param mediaType 参数类型,application/json,application/x-www-form-urlencoded
     */
    public static InputStream request(String url, String params, Map<String, String> headers, String method, String mediaType) throws IOException {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }
        method = method.toUpperCase();
        OutputStreamWriter writer = null;
        URL httpUrl = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
        if (method.equals("POST") || method.equals("PUT")) {
            conn.setDoOutput(true);
            conn.setUseCaches(false);
        }
        conn.setReadTimeout(8000);
        conn.setConnectTimeout(5000);
        conn.setRequestMethod(method);
//        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
//        conn.setRequestProperty("Accept-Charset", "utf-8");
//        conn.setRequestProperty("Content-Type", mediaType);
        // 添加请求�?
        if (headers != null) {
            for (String key : headers.keySet()) {
                conn.setRequestProperty(key, headers.get(key));
            }
        }
        // 添加参数
        if (params != null) {
            conn.setRequestProperty("Content-Length", String.valueOf(params.length()));
            writer = new OutputStreamWriter(conn.getOutputStream());
            writer.write(params);
            writer.flush();
        }

        BufferedInputStream bin;
        // 判断连接状�??
        if (conn.getResponseCode() >= 300) {
            throw new RuntimeException("HTTP Request is not success, Response code is " + conn.getResponseCode());
//            bin = new BufferedInputStream(conn.getErrorStream());
        }
        // 获取返回数据

//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        int size;
//        byte[] buf = new byte[1024];
//        while ((size = bin.read(buf)) != -1) {
//            baos.write(buf, 0, size);
//        }
//
//        bin.close();

//        result = new ByteArrayInputStream(baos.toByteArray());

        int contentLength = conn.getContentLength();
        InputStream inputStream = conn.getInputStream();

        OutputStreamWriter finalWriter = writer;
        return new InputStream() {

            @Override
            public int read() throws IOException {
                return inputStream.read();
            }

            @Override
            public int available() {
                return contentLength;
            }

            @Override
            public void close() throws IOException {
                inputStream.close();
                if (finalWriter != null) {
                    try {
                        finalWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    @SneakyThrows
    public static InputStream download(String urlPath) {
        // 统一资源
        URL url = new URL(urlPath);

        // 连接类的父类，抽象类
        URLConnection urlConnection = url.openConnection();
        // http的连接类
        HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
        // 设定请求的方法，默认是GET
        httpURLConnection.setRequestMethod("GET");
        // 设置字符编码
        httpURLConnection.setRequestProperty("Charset", "UTF-8");
        // 打开到此 URL 引用的资源的通信链接（如果尚未建立这样的连接）。
        httpURLConnection.connect();

        // 文件大小
        int fileLength = httpURLConnection.getContentLength();

        // 文件名
        String fileName = httpURLConnection.getURL().getFile();

        InputStream inputStream = httpURLConnection.getInputStream();

        return new InputStream() {

            @Override
            public int read() throws IOException {
                return inputStream.read();
            }

            @Override
            public int available() {
                return fileLength;
            }

            @Override
            public void close() throws IOException {
                inputStream.close();
            }
        };
    }

    /**
     * 异步表单请求
     *
     * @param params       请求参数
     * @param headers      请求�?
     * @param method       请求方式
     * @param onHttpResult 请求回调
     */
    public static void requestAsync(String url, String params, Map<String, String> headers, String method, OnHttpResult onHttpResult) {
        requestAsync(url, params, headers, method, "application/x-www-form-urlencoded", onHttpResult);
    }

    /**
     * 异步http请求
     *
     * @param params       请求参数
     * @param headers      请求�?
     * @param method       请求方式
     * @param mediaType    参数类型,application/json,application/x-www-form-urlencoded
     * @param onHttpResult 请求回调
     */
    public static void requestAsync(String url, String params, Map<String, String> headers, String method, String mediaType, OnHttpResult onHttpResult) {
        new Thread(() -> {
            try {
                InputStream result = request(url, params, headers, method, mediaType);
                onHttpResult.onSuccess(result);
            } catch (Exception e) {
                onHttpResult.onError(e.getMessage());
            }
        }).start();
    }

    /**
     * map转成string
     */
    @SneakyThrows
    public static String mapToString(String url, Map<String, String> params, String first) {
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
                sb.append(URLEncoder.encode(key, StandardCharsets.UTF_8));
                sb.append("=");
                sb.append(URLEncoder.encode(params.get(key), StandardCharsets.UTF_8));
            }
        }
        return sb.toString();
    }

    @SneakyThrows
    public static InputStream downloadFile(String urlPath) {
        // 统一资源
        URL url = new URL(urlPath);
        // 连接类的父类，抽象类
        URLConnection urlConnection = url.openConnection();
        // http的连接类
        HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
        // 设定请求的方法，默认是GET
        httpURLConnection.setRequestMethod("GET");
        // 设置字符编码
        httpURLConnection.setRequestProperty("Charset", "UTF-8");
        // 打开到此 URL 引用的资源的通信链接（如果尚未建立这样的连接）。
        httpURLConnection.connect();

        // 文件大小
        int fileLength = httpURLConnection.getContentLength();

        // 文件名
        String filePathUrl = httpURLConnection.getURL().getFile();
        //String fileFullName = filePathUrl.substring(filePathUrl.lastIndexOf(File.separatorChar) + 1);

//            System.out.println("file length---->" + fileLength);

        URLConnection con = url.openConnection();

        BufferedInputStream bin = new BufferedInputStream(httpURLConnection.getInputStream());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int size;
        byte[] buf = new byte[1024];
        while ((size = bin.read(buf)) != -1) {
            baos.write(buf, 0, size);
        }

        bin.close();

        return new ByteArrayInputStream(baos.toByteArray());
    }

    @SneakyThrows
    public static String readString(InputStream stream) {
        StringBuilder sb = new StringBuilder();
        BufferedReader in = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        String line;
        while ((line = in.readLine()) != null) {
            sb.append(line).append(System.lineSeparator());
        }
        return sb.toString();
    }
    @Getter
    @Setter
    private static int retryTimes = 10;

    public static InputStream downloadStream(String path) {
        return downloadStream(path, 0);
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
            httpURLConnection.setReadTimeout(10 * 1000);
            // 打开到此 URL 引用的资源的通信链接
            httpURLConnection.connect();

            bin = httpURLConnection.getInputStream();
        } catch (Exception err) {
            if (retry >= retryTimes) {
                throw new RuntimeException("Max retry time reached for url " + path);
            }
            return downloadStream(path, ++retry);
        }
        return bin;
    }

    /**
     * 异步请求回调
     */
    public interface OnHttpResult {
        void onSuccess(InputStream result);

        void onError(String message);
    }
}