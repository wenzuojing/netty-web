package com.github.wens.netty.web.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wens on 15-5-21.
 */
public class StringUtils {

    public interface ReplacementHandler {
        String doReplace(Matcher matcher);
    }

    public static String replace(String src, Pattern regex, ReplacementHandler replacementHandler) {

        if (regex == null || replacementHandler == null) {
            return src;
        }
        Matcher matcher = regex.matcher(src);
        StringBuilder sb = new StringBuilder(src.length());

        int start_index = 0;
        while (matcher.find()) {
            sb.append(src.substring(start_index, matcher.start()));
            sb.append(replacementHandler.doReplace(matcher));
            start_index = matcher.end();
        }
        if (start_index != src.length()) {
            sb.append(src.substring(start_index, src.length()));
        }
        return sb.toString();
    }
}
