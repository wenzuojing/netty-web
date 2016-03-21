package com.github.wens.netty.web;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 简单易容 http client
 * 无第三方依赖,支持链式操作
 * <p/>
 * Created by wens on 15-5-20.
 */
public class HttpClient {

    public static Get get(String url) {
        return new Get(url);
    }

    public static Head head(String url) {
        return new Head(url);
    }

    public static Delete delete(String url) {
        return new Delete(url);
    }

    public static Post post(String url) {
        return new Post(url);
    }

    public static Post post(String url, byte[] playload) {
        return new Post(url).addPlayload(playload);
    }

    public static Put put(String url) {
        return new Put(url);
    }

    public static Put put(String url, byte[] playload) {
        return new Put(url).addPlayload(playload);
    }

    public static Patch patch(String url) {
        return new Patch(url);
    }

    public static Patch patch(String url, byte[] playload) {
        return new Patch(url).addPlayload(playload);
    }


    public interface Handler<T> {
        T handle(int statusCode, byte[] body, HttpURLConnection connection);
    }

    public static class StatusCodeHandler implements Handler<Boolean> {

        private int statusCode;

        public StatusCodeHandler(int statusCode) {
            this.statusCode = statusCode;
        }

        @Override
        public Boolean handle(int statusCode, byte[] body, HttpURLConnection connection) {
            return this.statusCode == statusCode;
        }
    }

    public static StatusCodeHandler STATUS_CODE_200 = new StatusCodeHandler(200);

    public static abstract class BaseHttp<T extends BaseHttp> {

        protected String url;
        protected HttpURLConnection conn;
        protected String charset = "UTF-8";
        protected String contentType;
        protected String cookie;
        protected String method;
        protected Map<String, String> headers;
        protected int connectTimeout = 30000;
        protected int readTimeout = 30000;

        public BaseHttp() {
            headers = new HashMap<String, String>();
        }

        protected void openConn() {

            if (url == null || method == null) {
                throw new NullPointerException("url or method is null.");
            }

            URL connURL = null;
            try {
                connURL = new URL(url);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }

            try {
                conn = (HttpURLConnection) connURL.openConnection();
                conn.setRequestMethod(method);
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.111 Safari/537.36");
                conn.setReadTimeout(readTimeout);
                conn.setConnectTimeout(connectTimeout);
                conn.setUseCaches(false);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setInstanceFollowRedirects(true);
                conn.setRequestProperty("Connection", "Keep-Alive");

                for (String name : headers.keySet()) {
                    this.conn.setRequestProperty(name, headers.get(name));
                }

                if (charset != null) {
                    conn.setRequestProperty("Charset", charset);
                }

                if (contentType != null) {
                    conn.setRequestProperty("Content-Type", contentType);
                }

                if (cookie != null) {
                    conn.setRequestProperty("Cookie", cookie);
                }
                conn.connect();
            } catch (Exception e) {
                throw new RuntimeException("open connection fail:" + url, e);
            }
        }

        public <T> T ok(Handler<T> handler) {
            openConn();
            if (handler != null) {
                try {
                    InputStream input = conn.getInputStream();
                    String contentLength = conn.getHeaderField("Content-Length");
                    ByteArrayOutputStream out = new ByteArrayOutputStream(contentLength != null ? Integer.parseInt(contentLength) : 1024);
                    Utils.copy(input, out);
                    return handler.handle(conn.getResponseCode(), out.toByteArray(), conn);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return null;
        }

        public String getUrl() {
            return url;
        }

        public T setUrl(String url) {
            this.url = url;
            return (T) this;
        }

        public HttpURLConnection getConn() {
            return conn;
        }

        public String getCharset() {
            return charset;
        }

        public T setCharset(String charset) {
            this.charset = charset;
            return (T) this;
        }

        public String getContentType() {
            return contentType;
        }

        public T setContentType(String contentType) {
            this.contentType = contentType;
            return (T) this;
        }

        public String getMethod() {
            return method;
        }

        public T setMethod(String method) {
            this.method = method;
            return (T) this;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public T setHeader(String name, String value) {
            this.headers.put(name, value);
            return (T) this;
        }

        public int getConnectTimeout() {
            return connectTimeout;
        }

        public T setConnectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
            return (T) this;
        }

        public int getReadTimeout() {
            return readTimeout;
        }

        public T setReadTimeOut(int readTimeout) {
            this.readTimeout = readTimeout;
            return (T) this;
        }

        public void close() {
            this.conn.disconnect();
        }
    }

    public static class Utils {

        public static String encodeUrl(String data, String charset) {
            try {
                return URLEncoder.encode(data, charset);
            } catch (UnsupportedEncodingException var3) {
                throw new RuntimeException(var3);
            }
        }

        public static String decodeUrl(String data, String charset) {
            try {
                return URLDecoder.decode(data, charset);
            } catch (UnsupportedEncodingException var3) {
                throw new RuntimeException(var3);
            }
        }

        public static int copy(InputStream input, OutputStream output) throws IOException {
            long count = copyLarge(input, output);
            return count > 2147483647L ? -1 : (int) count;
        }

        public static long copyLarge(InputStream input, OutputStream output) throws IOException {
            byte[] buffer = new byte[4096];
            long count = 0L;

            int n1;
            for (boolean n = false; -1 != (n1 = input.read(buffer)); count += (long) n1) {
                output.write(buffer, 0, n1);
            }

            return count;
        }

        public static byte[] readFile(File file) {
            FileInputStream fileInputStream = null;

            try {
                fileInputStream = new FileInputStream(file);

                //byte[] bytes = new byte[file.length()] ;
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream((int) file.length());

                Utils.copy(fileInputStream, outputStream);

                return outputStream.toByteArray();

            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e) {
                        //
                    }
                }
            }
        }


    }

    public static class Get extends BaseHttp<Get> {
        public Get(String url) {
            setUrl(url).setMethod("GET");
        }
    }

    public static class Head extends BaseHttp<Head> {
        public Head(String url) {
            setUrl(url).setMethod("HEAD");
        }
    }

    public static class Delete extends BaseHttp<Delete> {
        public Delete(String url) {
            setUrl(url).setMethod("DELETE");
        }
    }

    public static class PlayloadHttp<T extends PlayloadHttp> extends BaseHttp<T> {

        private static final String BOUNDARY = "ZnGpDtePMx0KrHh_G0X99Yef9r8JZsRJSXC";

        private static final String ENTITY_BOUNDARY = "--" + BOUNDARY + "\r\n";
        private static final String ENTITY_BOUNDARY_END = "--" + BOUNDARY + "--";

        private ByteArrayOutputStream playload;
        private boolean isFirstAddFormItem = true;

        public PlayloadHttp(String url) {
            setUrl(url);
        }

        public T addPlayload(byte[] data) {
            if (playload == null) {
                playload = new ByteArrayOutputStream(1024);
            }
            try {
                playload.write(data);
            } catch (IOException e) {
                //
            }
            return (T) this;
        }

        public T addFormItem(String name, String value) {
            setContentType("application/x-www-form-urlencoded");
            String p = (isFirstAddFormItem ? "" : "&") + name + "=" + Utils.encodeUrl(value, this.getCharset());
            try {
                addPlayload(p.getBytes(this.getCharset()));
            } catch (UnsupportedEncodingException e) {
                //
            }
            isFirstAddFormItem = false;
            return (T) this;
        }


        public T addMultipartItem(String name, String value) {
            StringBuffer buffer = new StringBuffer(100);
            buffer.append(ENTITY_BOUNDARY);
            buffer.append("Content-Disposition: form-data; name=\"" + Utils.encodeUrl(name, getCharset()) + "\"\"\r\n\r\n");
            buffer.append(value).append("\r\n");
            try {
                addMultipart(buffer.toString().getBytes(getCharset()));
            } catch (UnsupportedEncodingException e) {
                //
            }
            return (T) this;
        }

        public T addMultipartItem(String name, File file) {
            StringBuffer buffer = new StringBuffer(100);
            buffer.append(ENTITY_BOUNDARY);
            buffer.append("Content-Disposition: form-data; name=\"" + Utils.encodeUrl(name, getCharset()) + "\"; filename=\"" + Utils.encodeUrl(file.getName(), getCharset()) + "\"\r\n");
            buffer.append("Content-Type: application/octet-stream\r\n\r\n");
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream(buffer.length() + (int) file.length() + 2);
                outputStream.write(buffer.toString().getBytes(getCharset()));
                outputStream.write(Utils.readFile(file));
                outputStream.write('\r');
                outputStream.write('\n');
                addMultipart(outputStream.toByteArray());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return (T) this;
        }

        private void addMultipart(byte[] entity) {
            setContentType("multipart/form-data; boundary=" + BOUNDARY);
            addPlayload(entity);
        }

        @Override
        protected void openConn() {
            if (contentType != null && contentType.startsWith("multipart/form-data")) {
                addPlayload(ENTITY_BOUNDARY_END.getBytes());
            }

            if (playload == null) {
                setHeader("Content-Length", String.valueOf(0));
                super.openConn();
                return;
            }

            setHeader("Content-Length", String.valueOf(playload.size()));
            super.openConn();

            try {
                OutputStream out = conn.getOutputStream();
                out.write(playload.toByteArray());
                out.flush();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class Post extends PlayloadHttp<Post> {

        public Post(String url) {
            super(url);
            setMethod("POST");
        }
    }

    public static class Put extends PlayloadHttp<Put> {

        public Put(String url) {
            super(url);
            setMethod("PUT");
        }
    }

    public static class Patch extends PlayloadHttp<Patch> {

        public Patch(String url) {
            super(url);
            setMethod("PATCH");
        }
    }
}
