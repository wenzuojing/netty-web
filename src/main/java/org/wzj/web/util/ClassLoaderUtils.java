package org.wzj.web.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by wens on 15-5-14.
 */
public class ClassLoaderUtils {


    public static URL getResource(String resourceName, Class callingClass) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(resourceName);
        if (url == null) {
            url = ClassLoaderUtils.class.getClassLoader().getResource(resourceName);
        }

        if (url == null) {
            ClassLoader cl = callingClass.getClassLoader();
            if (cl != null) {
                url = cl.getResource(resourceName);
            }
        }

        return url != null || resourceName == null || resourceName.length() != 0 && resourceName.charAt(0) == 47 ? url : getResource('/' + resourceName, callingClass);
    }


    public static Class loadClass(String className, Class callingClass) throws ClassNotFoundException {
        try {
            return Thread.currentThread().getContextClassLoader().loadClass(className);
        } catch (ClassNotFoundException var7) {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException var6) {
                try {
                    return ClassLoaderUtils.class.getClassLoader().loadClass(className);
                } catch (ClassNotFoundException var5) {
                    return callingClass.getClassLoader().loadClass(className);
                }
            }
        }
    }

    public static Class loadClass(String className) throws ClassNotFoundException {
        return loadClass(className, ClassLoaderUtils.class);
    }

    public static ClassLoader getStandardClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    public static ClassLoader getFallbackClassLoader() {
        return ClassLoaderUtils.class.getClassLoader();
    }

    public static Object createNewInstance(String className) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Class clazz;
        try {
            clazz = Class.forName(className, true, getStandardClassLoader());
        } catch (ClassNotFoundException var7) {
            try {
                clazz = Class.forName(className, true, getFallbackClassLoader());
            } catch (ClassNotFoundException var6) {
                throw new ClassNotFoundException("Unable to load class " + className + ". Initial cause was " + var7.getMessage(), var7);
            }
        }

        try {
            Object newInstance = clazz.newInstance();
            return newInstance;
        } catch (IllegalAccessException var4) {
            throw var4;
        } catch (InstantiationException var5) {
            throw var5;
        }
    }

    public static Set<Class<?>> getClasses(String packageName) throws IOException, ClassNotFoundException {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        return getClasses(loader, packageName);
    }

    public static Set<Class<?>> getClasses(ClassLoader loader, String packageName) throws IOException, ClassNotFoundException {
        HashSet classes = new HashSet();
        String path = packageName.replace('.', '/');
        Enumeration resources = loader.getResources(path);
        if (resources != null) {
            while (resources.hasMoreElements()) {
                URL url = (URL) resources.nextElement();
                String protocol = url.getProtocol();
                if ("file".equals(protocol)) {
                    String jar = decode(url.getFile(), "utf-8");
                    classes.addAll(getClassesFromDirectory(new File(jar), packageName));
                } else if ("jar".equalsIgnoreCase(protocol)) {
                    JarFile jar1 = ((JarURLConnection) url.openConnection()).getJarFile();
                    classes.addAll(getClassesFromJARFile(jar1, path));
                }
            }
        }

        return classes;
    }

    private static String stripFilenameExtension(String filename) {
        return filename.indexOf(46) != -1 ? filename.substring(0, filename.lastIndexOf(46)) : filename;
    }

    public static Set<Class<?>> getClassesFromDirectory(File directory, String packageName) throws ClassNotFoundException {
        HashSet classes = new HashSet();
        if (!directory.exists()) {
            return classes;
        } else {
            File[] files = directory.listFiles();
            File[] arr$ = files;
            int len$ = files.length;

            for (int i$ = 0; i$ < len$; ++i$) {
                File file = arr$[i$];
                String fileName;
                if (file.isDirectory()) {
                    fileName = packageName + "." + file.getName();
                    classes.addAll(getClassesFromDirectory(file, fileName));
                } else {
                    fileName = file.getName();
                    if (fileName.endsWith(".class")) {
                        String name = packageName + '.' + stripFilenameExtension(fileName);
                        Class clazz = loadClass(name);
                        classes.add(clazz);
                    }
                }
            }

            return classes;
        }
    }


    public static Set<Class<?>> getClassesFromJARFile(JarFile jarFile, String packageName) throws IOException, FileNotFoundException, ClassNotFoundException {
        HashSet classes = new HashSet();
        Enumeration entries = jarFile.entries();

        while (entries.hasMoreElements()) {
            JarEntry jarEntry = (JarEntry) entries.nextElement();
            if (jarEntry != null) {
                String className = jarEntry.getName();
                if (className.endsWith(".class")) {
                    className = stripFilenameExtension(className);
                    if (className.startsWith(packageName)) {
                        classes.add(loadClass(className.replace('/', '.')));
                    }
                }
            }
        }

        return classes;
    }

    private static String decode(String data, String charset) {
        try {
            return URLDecoder.decode(data, charset);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }


}
