package com.github.wens.netty.web.route;

import com.github.wens.netty.web.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wens on 15-9-1.
 */
public class RegRouteParser {

    private static final Pattern KEY_PATTERN = Pattern.compile("\\{(\\w+)(:(.*?))?\\}");

    private static final Pattern REGEX = Pattern.compile("(\\{\\d+(,\\d+)?\\})");

    private static final Pattern REGEX_FLAG = Pattern.compile("#REGEX_FLAG#");

    final Set<String> keys = new HashSet<String>();

    private String route;
    private String routeRegex;

    RegRouteParser(String route) {
        this.route = route;
    }

    public RegRouteParser parse() {

        //替换正则,消除对路由解析,如{3} {3,8}
        final List<String> regexs = new ArrayList<String>(5);

        route = StringUtils.replace(route, REGEX, new StringUtils.ReplacementHandler() {
            @Override
            public String doReplace(Matcher matcher) {
                regexs.add(matcher.group(1));
                return "#REGEX_FLAG#";
            }
        });


        routeRegex = StringUtils.replace(route, KEY_PATTERN, new StringUtils.ReplacementHandler() {
            @Override
            public String doReplace(Matcher matcher) {
                keys.add(matcher.group(1));
                return "(?<" + matcher.group(1) + ">" + (matcher.group(3) == null ? "[^/]+" : matcher.group(3)) + ")";
            }
        });

        routeRegex = StringUtils.replace(routeRegex, REGEX_FLAG, new StringUtils.ReplacementHandler() {
            int index = 0;

            @Override
            public String doReplace(Matcher matcher) {
                return regexs.get(index++);
            }
        });
        return this;
    }

    public Set<String> getKeys() {
        return keys;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getRouteRegex() {
        return routeRegex;
    }

    public void setRouteRegex(String routeRegex) {
        this.routeRegex = routeRegex;
    }
}
