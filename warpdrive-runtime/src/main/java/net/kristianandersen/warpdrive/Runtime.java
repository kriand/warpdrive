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
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;


/**
 * Created by IntelliJ IDEA.
 * User: kriand
 * Date: Mar 2, 2010
 * Time: 10:54:25 PM
 */
public final class Runtime {

    public static final String RUNTIME_CONFIG_FILE = "/net/kristianandersen/warpdrive/config.properties";

    public static final String GZIP_EXTENSION = ".gz";
    public static final String VERSION_PREFIX = "__v";

    public static final String ENABLED_KEY = "enabled";
    public static final String VERSION_KEY = "version";
    public static final String EXTERNAL_HOSTS_KEY = "external.hosts";
    public static final String SCRIPT_BUFFER_KEY = "net.kristianandersen.warpdrive.ScriptBuffer";
    public static final String IMAGE_DIR_KEY = "image.dir";
    public static final String JS_DIR_KEY = "js.dir";
    public static final String CSS_DIR_KEY = "css.dir";
    public static final String BUNDLE_PREFIX_KEY = "bundle.";
    public static final String MULTIVAL_SEPARATOR = ",";

    private static final Map<String, List<String>> bundles = new HashMap<String, List<String>>();

    private static boolean enabled = false;
    private static String version = null;
    private static String[] externalHosts = null;
    private static String imagesDir = null;
    private static String jsDir = null;
    private static String cssDir = null;

    static {
        InputStream is = null;
        try {
            Properties props = new Properties();
            is = Thread.currentThread().getContextClassLoader().getResourceAsStream(RUNTIME_CONFIG_FILE);
            if (is != null) {
                props.load(is);
                configure(props);
            } else {
                System.err.println("[WarpDrive] No config found, WarpDrive must be manually configured");
                enabled = false;
            }
        }
        catch (Exception ex) {
            throw new IllegalStateException("Caught exception while reading config, disabling WarpDrive", ex);
        }
        finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    System.err.println("[WarpDrive] Ignoring IOException on close(): " + e.getMessage());
                }
            }
        }
    }

    private Runtime() {

    }

    /**
     * @param request
     * @param scriptsToBuffer
     */
    public static void bufferScripts(final HttpServletRequest request, final String scriptsToBuffer) {
        StringBuilder scriptBuffer = getScriptBuffer(request);
        scriptBuffer.append(scriptsToBuffer.trim());
    }

    /**
     * @param request
     * @return
     */
    public static String getBufferedScripts(final HttpServletRequest request) {
        return getScriptBuffer(request).toString();
    }

    /**
     * @param src
     * @param type
     * @param params
     * @param request
     * @return
     */
    public static String getScriptTag(final String src, final String type, final Map<String, String> params, final HttpServletRequest request) {
        if (!enabled && isBundle(src)) {
            return unbundleScriptBundles(src, type, params, request);
        }
        return writeScriptTag(src, type, params, request);
    }

    /**
     * @param src
     * @param params
     * @param request
     * @return
     */
    public static String getImageTag(final String src, final Map<String, String> params, final HttpServletRequest request) {
        StringBuilder buffer = new StringBuilder();
        buffer.append("<img src=\"");
        appendLink(src, buffer, imagesDir, request, false);
        buffer.append("\" ");
        for (String key : params.keySet()) {
            buffer.append(key).append("=\"").append(params.get(key)).append("\" ");
        }
        buffer.append("/>");
        return buffer.toString();
    }

    /**
     * @param href
     * @param rel
     * @param type
     * @param params
     * @param request
     * @return
     */
    public static String getStylesheetTag(final String href, final String rel, final String type, final Map<String, String> params, final HttpServletRequest request) {
        if (!enabled && isBundle(href)) {
            return unbundleCssBundles(href, rel, type, params, request);
        }
        return writeCssTag(href, rel, type, params, request);
    }


    /**
     * @param config
     */
    static void configure(final Properties config) {
        enabled = Boolean.valueOf(config.getProperty(ENABLED_KEY));
        version = config.getProperty(VERSION_KEY);
        imagesDir = config.getProperty(IMAGE_DIR_KEY);
        jsDir = config.getProperty(JS_DIR_KEY);
        cssDir = config.getProperty(CSS_DIR_KEY);
        setupExternalHosts(config);
        if (!enabled) {
            setupBundles(config);
        }
    }

    /**
     * @param request
     * @return
     */
    private static StringBuilder getScriptBuffer(final HttpServletRequest request) {
        StringBuilder scriptBuffer = (StringBuilder) request.getAttribute(SCRIPT_BUFFER_KEY);
        if (scriptBuffer == null) {
            scriptBuffer = new StringBuilder();
            request.setAttribute(SCRIPT_BUFFER_KEY, scriptBuffer);
        }
        return scriptBuffer;
    }

    /**
     * @param href
     * @param rel
     * @param type
     * @param params
     * @param request
     * @return
     */
    private static String writeCssTag(final String href, final String rel, final String type, final Map<String, String> params, final HttpServletRequest request) {
        StringBuilder buffer = new StringBuilder();
        buffer.append("<link href=\"");
        appendLink(href, buffer, cssDir, request, true);
        buffer.append("\" rel=\"");
        if (rel == null || "".equals(rel)) {
            buffer.append("stylesheet");
        } else {
            buffer.append(rel);
        }
        buffer.append("\" type=\"");
        if (type == null || "".equals(type)) {
            buffer.append("text/css");
        } else {
            buffer.append(type);
        }
        buffer.append("\" ");
        if(params != null) {
            for (String key : params.keySet()) {
                buffer.append(key).append("=\"").append(params.get(key)).append("\" ");
            }
        }
        buffer.append("/>");
        return buffer.toString();
    }

    /**
     * @param src
     * @param type
     * @param params
     * @param request
     * @return
     */
    private static String writeScriptTag(final String src, final String type, final Map<String, String> params, final HttpServletRequest request) {

        final StringBuilder buffer = new StringBuilder();

        buffer.append("<script src=\"");
        appendLink(src, buffer, jsDir, request, true);

        buffer.append("\" type=\"");
        if (type == null || "".equals(type)) {
            buffer.append("text/javascript");
        } else {
            buffer.append(type);
        }
        buffer.append("\" ");
        if (params != null) {
            for (String key : params.keySet()) {
                buffer.append(key).append("=\"").append(params.get(key)).append("\" ");
            }
        }
        buffer.append("></script>");
        return buffer.toString();
    }

    /**
     * @param filename
     * @param buffer
     * @param topLevelDir
     * @param request
     * @param isTextResource
     */
    private static void appendLink(final String filename, final StringBuilder buffer, final String topLevelDir, final HttpServletRequest request, final boolean isTextResource) {
        if (!enabled) {
            buffer.append(request.getContextPath()).append(topLevelDir).append(filename);
            return;
        }
        if (externalHosts != null) {
            buffer.append(externalHosts[Math.abs(filename.hashCode()) % externalHosts.length]);
        }
        buffer.append(request.getContextPath()).append(topLevelDir);
        String versionedSrc = null;
        if (isTextResource && isGzipAccepted(request)) {
            versionedSrc = FilenameUtils.insertVersionAndGzipExtension(filename, version);
        } else {
            versionedSrc = FilenameUtils.insertVersion(filename, version);
        }
        buffer.append(versionedSrc);

    }

    /**
     * @param name
     * @return
     */
    private static boolean isBundle(final String name) {
        return bundles.containsKey(name);
    }

    /**
     * @param src
     * @param type
     * @param params
     * @param request
     * @return
     */
    private static String unbundleScriptBundles(final String src, final String type, final Map<String, String> params, final HttpServletRequest request) {
        StringBuilder builder = new StringBuilder();
        for (String script : bundles.get(src)) {
            builder.append(writeScriptTag(script, type, params, request));
        }
        return builder.toString();
    }

    /**
     * @param href
     * @param rel
     * @param type
     * @param params
     * @param request
     * @return
     */
    private static String unbundleCssBundles(final String href, final String rel, final String type, final Map<String, String> params, final HttpServletRequest request) {
        StringBuilder builder = new StringBuilder();
        for (String stylesheet : bundles.get(href)) {
            builder.append(writeCssTag(stylesheet, rel, type, params, request));
        }
        return builder.toString();
    }

    /**
     * @param request
     * @return
     */
    private static boolean isGzipAccepted(final HttpServletRequest request) {
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

    /**
     * @param config
     */
    private static void setupBundles(final Properties config) {
        Enumeration properties = config.propertyNames();
        while (properties.hasMoreElements()) {
            String property = (String) properties.nextElement();
            if (property.startsWith(BUNDLE_PREFIX_KEY)) {
                bundles.put(property.substring(BUNDLE_PREFIX_KEY.length()), Arrays.asList(config.getProperty(property).split(MULTIVAL_SEPARATOR)));
            }
        }
    }

    /**
     * @param config
     */
    private static void setupExternalHosts(final Properties config) {
        if (config.getProperty(EXTERNAL_HOSTS_KEY) != null) {
            externalHosts = config.getProperty(EXTERNAL_HOSTS_KEY).split(MULTIVAL_SEPARATOR);
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
