package org.wzj.web;

import io.netty.handler.codec.http.HttpHeaders;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

import static io.netty.handler.codec.http.HttpResponseStatus.*;

/**
 * 部分代码参考 netty example
 * 适合处理小文件
 * Created by wens on 15-5-15.
 */
public class StaticSource {

    private static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");

    private static final Pattern ALLOWED_FILE_NAME = Pattern.compile("[A-Za-z0-9][-_A-Za-z0-9\\.]*");
    public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
    public static final String HTTP_DATE_GMT_TIMEZONE = "GMT";
    public static final int HTTP_CACHE_SECONDS = 60;
    public volatile static String staticDir;


    public static void servingStaticFile(WebContext context) throws ParseException {

        Request request = context.getRequest();
        Response response = context.getResponse();

        final String uri = request.getUri();
        if (staticDir == null) {
            response.sendError(FORBIDDEN.code(), FORBIDDEN.reasonPhrase());
            return;
        }

        File file = new File(staticDir + sanitizeUri(uri));
        if (file.isHidden() || !file.exists()) {
            response.sendError(NOT_FOUND.code(), NOT_FOUND.reasonPhrase());
            return;
        }

        if (file.isDirectory()) {
            if (uri.endsWith("/")) {
                sendListing(response, file);
            } else {
                sendRedirect(response, uri + '/');
            }
            return;
        }

        if (!file.isFile()) {
            response.sendError(FORBIDDEN.code(), FORBIDDEN.reasonPhrase());
            return;
        }

        // Cache Validation
        String ifModifiedSince = request.getHeader(HttpHeaders.Names.IF_MODIFIED_SINCE);
        if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
            SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
            Date ifModifiedSinceDate = dateFormatter.parse(ifModifiedSince);

            // Only compare up to the second because the datetime format we send to the client
            // does not have milliseconds
            long ifModifiedSinceDateSeconds = ifModifiedSinceDate.getTime() / 1000;
            long fileLastModifiedSeconds = file.lastModified() / 1000;
            if (ifModifiedSinceDateSeconds == fileLastModifiedSeconds) {
                sendNotModified(response);
                return;
            }
        }

        long fileLength = file.length();

        setContentLengthHeader(response, fileLength);
        setContentTypeHeader(response, file);
        setDateAndCacheHeaders(response, file);


        response.writeFile(file);


    }

    private static void sendListing(Response response, File dir) {
        response.setContentType("text/html; charset=UTF-8");

        String dirPath = dir.getPath();
        StringBuilder buf = new StringBuilder()
                .append("<!DOCTYPE html>\r\n")
                .append("<html><head><title>")
                .append("Listing of: ")
                .append(dirPath)
                .append("</title></head><body>\r\n")

                .append("<h3>Listing of: ")
                .append(dirPath)
                .append("</h3>\r\n")

                .append("<ul>")
                .append("<li><a href=\"../\">..</a></li>\r\n");

        for (File f : dir.listFiles()) {
            if (f.isHidden() || !f.canRead()) {
                continue;
            }

            String name = f.getName();
            if (!ALLOWED_FILE_NAME.matcher(name).matches()) {
                continue;
            }

            buf.append("<li><a href=\"")
                    .append(name)
                    .append("\">")
                    .append(name)
                    .append("</a></li>\r\n");
        }

        buf.append("</ul></body></html>\r\n");
        response.writeBody(buf.toString());
    }

    private static void sendRedirect(Response response, String newUri) {
        response.redirect(newUri);
    }


    private static void sendNotModified(Response response) {
        response.setStatus(NOT_MODIFIED.code(), NOT_MODIFIED.reasonPhrase());
        setDateHeader(response);
    }


    private static void setDateHeader(Response response) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));
        Calendar time = new GregorianCalendar();
        response.setHeader(HttpHeaders.Names.DATE, dateFormatter.format(time.getTime()));
    }


    private static void setDateAndCacheHeaders(Response response, File fileToCache) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

        // Date header
        Calendar time = new GregorianCalendar();
        response.setHeader(HttpHeaders.Names.DATE, dateFormatter.format(time.getTime()));

        // Add cache headers
        time.add(Calendar.SECOND, HTTP_CACHE_SECONDS);
        response.setHeader(HttpHeaders.Names.EXPIRES, dateFormatter.format(time.getTime()));
        response.setHeader(HttpHeaders.Names.CACHE_CONTROL, "private, max-age=" + HTTP_CACHE_SECONDS);
        response.setHeader(HttpHeaders.Names.LAST_MODIFIED, dateFormatter.format(new Date(fileToCache.lastModified())));
    }


    private static void setContentTypeHeader(Response response, File file) {
        MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
        response.setContentType(mimeTypesMap.getContentType(file.getPath()));
    }

    private static void setContentLengthHeader(Response response, long fileLength) {
        response.setContentLength(fileLength);

    }

    private static String sanitizeUri(String uri) {
        // Decode the path.
        try {
            uri = URLDecoder.decode(uri, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }

        if (uri.isEmpty() || uri.charAt(0) != '/') {
            return null;
        }

        // Convert file separators.
        uri = uri.replace('/', File.separatorChar);

        // Simplistic dumb security check.
        // You will have to do something serious in the production environment.
        if (uri.contains(File.separator + '.') ||
                uri.contains('.' + File.separator) ||
                uri.charAt(0) == '.' || uri.charAt(uri.length() - 1) == '.' ||
                INSECURE_URI.matcher(uri).matches()) {
            return null;
        }

        return uri;
    }

}
