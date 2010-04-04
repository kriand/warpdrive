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
 */
public class Runtime {

    public final static String RUNTIME_CONFIG_FILE = "/net/kristianandersen/warpdrive/config.properties";

    public final static String GZIP_EXTENSION = ".gz";

    public final static String ENABLED_KEY = "enabled";
    public final static String VERSION_KEY = "version";
    public final static String EXTERNAL_HOSTS_KEY = "external.hosts";
    public final static String SCRIPT_BUFFER_KEY = "net.kristianandersen.warpdrive.ScriptBuffer";    
    public final static String IMAGE_DIR_KEY = "image.dir";
    public final static String JS_DIR_KEY = "js.dir";
    public final static String CSS_DIR_KEY = "css.dir";
    public final static String BUNDLE_PREFIX_KEY = "css.bundle.";

    private static Properties settings = new Properties();

    private static boolean enabled = false;
    private static String version = null;
    private static String[] externalHosts = null;
    private static String imagesDir = null;
    private static String jsDir = null;
    private static String cssDir = null;
    public static final Map<String, List<String>> bundles = new HashMap<String, List<String>>();

    static {
        InputStream is = null;
        try {
            Properties props = new Properties();
            is = Thread.currentThread().getContextClassLoader().getResourceAsStream(RUNTIME_CONFIG_FILE);
            props.load(is);
            configure(props);
        }
        catch (Exception ex) {
            System.err.println("Unable to initialize WarpDrive, disabling!");
            ex.printStackTrace();
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

    /**
     *
     *
     * @param request
     * @param scriptsToBuffer
     */
    public static void bufferScripts(HttpServletRequest request, String scriptsToBuffer) {
        StringBuilder scriptBuffer = getScriptBuffer(request);
        scriptBuffer.append(scriptsToBuffer.trim());
    }

    /**
     *
     * @param request
     * @return
     */
    public static String renderBufferedScripts(HttpServletRequest request) {
        return getScriptBuffer(request).toString();
    }

    /**
     *
     * @param src
     * @param type
     * @param params
     * @param request
     * @return
     */
    public static String getScriptTag(String src, String type, Map<String, String> params, HttpServletRequest request) {
        if (!enabled && isBundle(src)) {
            return unbundleScriptBundles(src, type, params, request);
        }
        return writeScriptTag(src, type, params, request);
    }

    /**
     *
     * @param src
     * @param params
     * @param request
     * @return
     */
    public static String getImageTag(String src, Map<String, String> params, HttpServletRequest request) {
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
     *
     * @param href
     * @param rel
     * @param type
     * @param params
     * @param request
     * @return
     */
    public static String getStylesheetTag(String href, String rel, String type, Map<String, String> params, HttpServletRequest request) {
        if (!enabled && isBundle(href)) {
            return unbundleCssBundles(href, rel, type, params, request);
        }
        return writeCssTag(href, rel, type, params, request);
    }


    /**
     *
     * @param config
     */
    static void configure(Properties config) {
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
     *
     * @param request
     * @return
     */
    private static StringBuilder getScriptBuffer(HttpServletRequest request) {
        StringBuilder scriptBuffer = (StringBuilder) request.getAttribute(SCRIPT_BUFFER_KEY);
        if (scriptBuffer == null) {
            scriptBuffer = new StringBuilder();
            request.setAttribute(SCRIPT_BUFFER_KEY, scriptBuffer);
        }
        return scriptBuffer;
    }

    /**
     *
     * @param href
     * @param rel
     * @param type
     * @param params
     * @param request
     * @return
     */
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
        buffer.append("/>");
        return buffer.toString();
    }

    /**
     *
     * @param src
     * @param type
     * @param params
     * @param request
     * @return
     */
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
        buffer.append("></script>");
        return buffer.toString();
    }

    /**
     *
     * @param filename
     * @param buffer
     * @param topLevelDir
     * @param request
     * @param isTextResource
     */
    private static void appendLink(String filename, StringBuilder buffer, String topLevelDir, HttpServletRequest request, boolean isTextResource) {
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
     *
     * @param name
     * @return
     */
    static boolean isBundle(String name) {
        return bundles.containsKey(name);
    }

    /**
     *
     * @param src
     * @param type
     * @param params
     * @param request
     * @return
     */
    private static String unbundleScriptBundles(String src, String type, Map<String, String> params, HttpServletRequest request) {
        StringBuilder builder = new StringBuilder();
        for (String script : bundles.get(src)) {
            builder.append(writeScriptTag(script, type, params, request));
        }
        return builder.toString();
    }

    /**
     *
     * @param href
     * @param rel
     * @param type
     * @param params
     * @param request
     * @return
     */
    private static String unbundleCssBundles(String href, String rel, String type, Map<String, String> params, HttpServletRequest request) {
        StringBuilder builder = new StringBuilder();
        for (String stylesheet : bundles.get(href)) {
            builder.append(writeCssTag(stylesheet, rel, type, params, request));
        }
        return builder.toString();
    }

    /**
     *
     * @param request
     * @return
     */
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

    /**
     *
     * @param config
     */
    private static void setupBundles(Properties config) {
        Enumeration properties = config.propertyNames();
        while (properties.hasMoreElements()) {
            String property = (String) properties.nextElement();
            if (property.startsWith(BUNDLE_PREFIX_KEY)) {
                bundles.put(property.substring(BUNDLE_PREFIX_KEY.length()), Arrays.asList(config.getProperty(property).split(",")));
            }
        }
    }

    /**
     * 
     * @param config
     */
    private static void setupExternalHosts(Properties config) {
        if (config.getProperty(EXTERNAL_HOSTS_KEY) != null) {
            externalHosts = config.getProperty(EXTERNAL_HOSTS_KEY).split(",");
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
