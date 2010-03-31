/*
   Copyright 2010 Kristian Andersen

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package net.kristianandersen.warpdrive;

import net.kristianandersen.warpdrive.utils.FilenameUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: kriand
 * Date: Mar 2, 2010
 * Time: 10:54:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class Runtime {

    public final static String RUNTIME_SETTINGS_FILE = "/net/kristianandersen/warpdrive/settings.properties";

    public final static String GZIP_EXTENSION = ".gz";

    public final static String ENABLED_KEY = "enabled";
    public final static String VERSION_KEY = "version";
    public final static String EXTERNAL_HOSTS_KEY = "external.hosts";
    public final static String SCRIPT_BUFFER_KEY = "net.kristianandersen.warpdrive.ScriptBuffer";
    public final static String IMAGE_DIR_KEY = "image.dir";
    public final static String JS_DIR_KEY = "js.dir";
    public final static String CSS_DIR_KEY = "css.dir";
    public final static String CSS_BUNDLE_PREFIX_KEY = "css.bundle.";
    public final static String JS_BUNDLE_PREFIX_KEY = "js.bundle.";

    private static Properties settings = new Properties();

    private static boolean enabled = false;
    private static String version = null;
    private static String[] externalHosts = null;
    private static String imagesDir = null;
    private static String jsDir = null;
    private static String cssDir = null;
    public static Map<String, List<String>> cssBundles = new HashMap<String, List<String>>();
    public static Map<String, List<String>> jsBundles = new HashMap<String, List<String>>();

    static {
        InputStream is = null;
        try {
            Properties props = new Properties();
            is = Thread.currentThread().getContextClassLoader().getResourceAsStream(RUNTIME_SETTINGS_FILE);
            props.load(is);
            setConfig(props);
        }
        catch (Exception ex) {
            //TODO Log
            enabled = false;
        }
        finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public static StringBuilder getScriptBuffer(HttpServletRequest request) {
        StringBuilder scriptBuffer = (StringBuilder) request.getAttribute(SCRIPT_BUFFER_KEY);
        if (scriptBuffer == null) {
            scriptBuffer = new StringBuilder();
            request.setAttribute(SCRIPT_BUFFER_KEY, scriptBuffer);
        }
        return scriptBuffer;
    }

    public static String getScriptTag(String src, String type, Map<String, String> params, HttpServletRequest request) {
        if (!enabled && isScriptBundle(src)) {
            return unbundleScriptBundles(src, type, params, request);
        }
        return writeScriptTag(src, type, params, request);
    }

    public static String getImageTag(String src, Map<String, String> params, HttpServletRequest request) {
        StringBuilder buffer = new StringBuilder();
        buffer.append("<img src=\"");
        appendLink(src, buffer, imagesDir, request, false);
        buffer.append("\" ");
        for (String key : params.keySet()) {
            buffer.append(key).append("=\"").append(params.get(key)).append("\" ");
        }
        buffer.append("/>\n");
        return buffer.toString();
    }

    public static String getStylesheetTag(String href, String rel, String type, Map<String, String> params, HttpServletRequest request) {
        if (!enabled && isCssBundle(href)) {
            return unbundleCssBundles(href, rel, type, params, request);
        }
        return writeCssTag(href, rel, type, params, request);
    }

    static void setConfig(Properties settings) {        
        Runtime.settings = settings;
        enabled = Boolean.valueOf(settings.getProperty(ENABLED_KEY));
        version = settings.getProperty(VERSION_KEY);
        imagesDir = settings.getProperty(IMAGE_DIR_KEY);
        jsDir = settings.getProperty(JS_DIR_KEY);
        cssDir = settings.getProperty(CSS_DIR_KEY);
        setExternalHosts();
        if (!enabled) {
            figureOutBundles();
        }
    }


    private static String writeCssTag(String href, String rel, String type, Map<String, String> params, HttpServletRequest request) {
        StringBuilder buffer = new StringBuilder();
        buffer.append("<link href=\"");
        appendLink(href, buffer, cssDir, request, true);
        buffer.append("\" rel=\"");
        if ("".equals(rel)) {
            buffer.append("stylesheet");
        } else {
            buffer.append(rel);
        }
        buffer.append("\" type=\"");
        if ("".equals(type)) {
            buffer.append("text/css");
        } else {
            buffer.append(type);
        }
        buffer.append("\" ");
        for (String key : params.keySet()) {
            buffer.append(key).append("=\"").append(params.get(key)).append("\" ");
        }
        buffer.append("/>\n");
        return buffer.toString();
    }

    private static String writeScriptTag(String src, String type, Map<String, String> params, HttpServletRequest request) {
        StringBuilder buffer = new StringBuilder();
        buffer.append("<script src=\"");
        appendLink(src, buffer, jsDir, request, true);

        buffer.append("\" type=\"");
        if ("".equals(type)) {
            buffer.append("text/javascript");
        } else {
            buffer.append(type);
        }
        buffer.append("\" ");
        for (String key : params.keySet()) {
            buffer.append(key).append("=\"").append(params.get(key)).append("\" ");
        }
        buffer.append("></script>\n");
        return buffer.toString();
    }

    private static void appendLink(String src, StringBuilder buffer, String topLevelDir, HttpServletRequest request, boolean isTextResource) {
        if (!enabled) {
            buffer.append(request.getContextPath()).append(topLevelDir).append(src);
            return;
        }
        if (externalHosts != null) {
            buffer.append(externalHosts[Math.abs(src.hashCode()) % externalHosts.length]);
        }
        buffer.append(request.getContextPath()).append(topLevelDir);
        String versionedSrc = FilenameUtils.insertVersion(src, version);
        if (isTextResource && isGzipAccepted(request)) {
            versionedSrc = FilenameUtils.insertGzipExtension(versionedSrc);
        }
        buffer.append(versionedSrc);

    }

    static boolean isCssBundle(String href) {
        return cssBundles.get(href) != null;
    }

    static boolean isScriptBundle(String src) {
        return jsBundles.get(src) != null;
    }

    private static String unbundleScriptBundles(String src, String type, Map<String, String> params, HttpServletRequest request) {
        StringBuilder builder = new StringBuilder();
        for (String script : jsBundles.get(src)) {
            builder.append(writeScriptTag(script, type, params, request));
        }
        return builder.toString();
    }

    private static String unbundleCssBundles(String href, String rel, String type, Map<String, String> params, HttpServletRequest request) {
        StringBuilder builder = new StringBuilder();
        for (String stylesheet : cssBundles.get(href)) {
            builder.append(writeCssTag(stylesheet, rel, type, params, request));
        }
        return builder.toString();
    }

    private static boolean isGzipAccepted(HttpServletRequest request) {
        Enumeration acceptEncoding = request.getHeaders("Accept-Encoding");
        boolean gzipAccepted = false;
        boolean qZero = false;
        while (acceptEncoding.hasMoreElements()) {
            String val = (String) acceptEncoding.nextElement();
            if (val.contains("gzip") || val.contains("*")) {
                gzipAccepted = true;
            }
            if (val.contains("q=0")) {
                qZero = true;
            }
        }
        return gzipAccepted && !qZero;
    }

    private static void figureOutBundles() {
        Enumeration properties = settings.propertyNames();
        while (properties.hasMoreElements()) {
            String property = (String) properties.nextElement();
            if (property.startsWith(CSS_BUNDLE_PREFIX_KEY)) {
                cssBundles.put(property.substring(CSS_BUNDLE_PREFIX_KEY.length()), Arrays.asList(settings.getProperty(property).split(",")));
            }
            if (property.startsWith(JS_BUNDLE_PREFIX_KEY)) {
                jsBundles.put(property.substring(JS_BUNDLE_PREFIX_KEY.length()), Arrays.asList(settings.getProperty(property).split(",")));
            }
        }
    }

    private static void setExternalHosts() {
        if (settings.getProperty(EXTERNAL_HOSTS_KEY) != null) {
            externalHosts = settings.getProperty(EXTERNAL_HOSTS_KEY).split(",");
            for (int i = 0; i < externalHosts.length; i++) {
                externalHosts[i] = externalHosts[i].trim();
                if (externalHosts[i].endsWith("/")) {
                    externalHosts[i] = externalHosts[i].substring(0, externalHosts[i].length() - 1);
                }
            }
            if (externalHosts.length < 1) {
                externalHosts = null;
            }
        }
    }


}
