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
package org.kriand.warpdrive;

import org.kriand.warpdrive.utils.FilenameUtils;

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
 * This class implements the runtime behaviour of WarpDrive. All logic for the taglibs is contained
 * in here. It relies on a static initializer looking for a configuration file in the classpath at
 * {@linkplain Runtime#RUNTIME_CONFIG_FILE}
 * <p/>
 *  IDEA.
 *
 * @author kriand <a href="http://mailhide.recaptcha.net/d?k=01r9lbYEAtg9V5s1Ru_jtZ1g==&c=-aIoeZ0yU0yPn2kdog349bCmN-h1pe5Ed0LsyuWMbEc=">Show email</a>
 *         Date: Mar 2, 2010
 *         Time: 10:54:25 PM
 */
public final class Runtime {

    /**
     * Where to look for the configuration file in classpath.
     */
    public static final String RUNTIME_CONFIG_FILE = "/org/kriand/warpdrive/config.properties";

    /**
     * Syntax for the gzip extension.
     */
    public static final String GZIP_EXTENSION = ".gz";

    /**
     * Syntax for a prefix inserted right before the version.
     */
    public static final String VERSION_PREFIX = "_v";

    /**
     * Configuration key for developmentmode.
     */
    public static final String DEV_MODE_KEY = "developmentMode";

    /**
     * Configuration key, the version of the current build.
     */
    public static final String VERSION_KEY = "version";

    /**
     * Configuration key for external hosts.
     */
    public static final String EXTERNAL_HOSTS_KEY = "external.hosts";

    /**
     * Configuration key, the image directory, relative to src/main/webapp.
     */
    public static final String IMAGE_DIR_KEY = "image.dir";

    /**
     * Configuration key, the javascript directory, relative to src/main/webapp.
     */
    public static final String JS_DIR_KEY = "js.dir";

    /**
     * Configuration key, the css directory, relative to src/main/webapp.
     */
    public static final String CSS_DIR_KEY = "css.dir";

    /**
     * Configuration key, prefix used to specify bundles.
     */
    public static final String BUNDLE_PREFIX_KEY = "bundle.";

    /**
     * The character sequence used to separate values in multivalue config elements.
     */
    public static final String MULTIVAL_SEPARATOR = ",";

    /**
     * The key to use when storing the javascript buffer on the request.
     */
    public static final String SCRIPT_BUFFER_KEY = "org.kriand.warpdrive.ScriptBuffer";

    /**
     * Holds the configured bundles, populated from config.
     */
    private static final Map<String, List<String>> BUNDLES = new HashMap<String, List<String>>();

    /**
     * Indicates if development mode is enabled, populated from config.
     */
    private static boolean developmentMode = false;

    /**
     * Holds the current version, populated from config.
     */
    private static String version = null;

    /**
     * Holds the currently configured external hosts, if any. Populated from config.
     */
    private static String[] externalHosts = null;

    /**
     * Holds the current image dir, populated from config.
     */
    private static String imagesDir = null;

    /**
     * Holds the current javascript dir, populated from config.
     */
    private static String jsDir = null;

    /**
     * Holds the current css dir, populated from config.
     */
    private static String cssDir = null;

    /**
     * Initializes the Runtime by reading the configfile from classpath.
     * TODO: Get rid of this. 
     */
    static {
        InputStream is = null;
        try {
            Properties props = new Properties();
            is = Class.class.getResourceAsStream(RUNTIME_CONFIG_FILE);
            if (is != null) {
                props.load(is);
                configure(props);
            } else {
                is = Thread.currentThread().getContextClassLoader().getResourceAsStream(RUNTIME_CONFIG_FILE);
                if (is != null) {
                    props.load(is);
                    configure(props);
                } else {
                    throw new IllegalStateException("Could not find configuration file");
                }
            }
        } catch (IOException ioex) {
            throw new IllegalStateException("Caught IOException while reading config", ioex);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    System.err.println("[WarpDrive] Caught IOException on close(): " + e.getMessage());
                }
            }
        }
    }

    /**
     * Private constructor to prevent this class from being instantiated.
     */
    private Runtime() {

    }

    /**
     * This method is exposed as a jstl function and called from taglib. Buffers the provided contents using a StringBuilder
     * stored on the request.
     *
     * @param request         The HttpServletRequest
     * @param scriptsToBuffer The scriptdata to buffer.
     */
    public static void bufferScripts(final HttpServletRequest request, final String scriptsToBuffer) {
        StringBuilder scriptBuffer = getScriptBuffer(request);
        scriptBuffer.append(scriptsToBuffer.trim());
    }

    /**
     * This method is exposed as a jstl function and called from taglib.
     * Returns all javascripttags buffered during the current request. This method
     * also clears the buffer as it should only be called once per request.
     *
     * @param request The HttpServletRequest
     * @return All buffered scripttags.
     */
    public static String getBufferedScripts(final HttpServletRequest request) {
        final StringBuilder scriptBuffer = getScriptBuffer(request);
        final String result = scriptBuffer.toString();
        clearScriptBuffer(request);
        return result;
    }

    /**
     * This method is exposed as a jstl function and called from taglib.
     * Returns a javascript tag, ready to be included in page,
     * with versioned url. (Assuming developmentMode is not active, in that case
     * urls are returned without versionnumber)
     * If the src param points to a bundle and developmentMode is active, each
     * script in the bundle is returned in its own script tag.
     *
     * @param src     The src attribute in a script tag. From tag parameter.
     * @param type    Script type, default is text/javascript. From tag parameter.
     * @param params  additional attributes specified in tag. From tag parameters.
     * @param request The HttpServlet request.
     * @return Script tag ready to be included in page.
     */
    public static String getScriptTag(final String src, final String type, final Map<String, String> params, final HttpServletRequest request) {
        if (developmentMode && isBundle(src)) {
            return unbundleScriptBundle(src, type, params, request);
        }
        return writeScriptTag(src, type, params, request);
    }

    /**
     * This method is exposed as a jstl function and called from taglib.
     * Returns an image tag, with a versioned imageurl. If developmentMode is enabled, the
     * url is not versioned.
     *
     * @param src The src attribute pointing to the image. From tag parameter
     * @param params Additional parameters, like border, with, etc. From tag parameter.
     * @param request The current servlet request
     * @return An img tag ready to be included in page.
     */
    public static String getImageTag(final String src, final Map<String, String> params, final HttpServletRequest request) {
        StringBuilder buffer = new StringBuilder();
        buffer.append("<img src=\"");
        appendVersionedLink(src, buffer, imagesDir, request, false);
        buffer.append("\" ");
        addAdditionalParameters(params, buffer);
        buffer.append("/>");
        return buffer.toString();
    }

    /**
     * This method is exposed as a jstl function and called from taglib.
     * Returns a link tag with href pointing to versioned URL.
     * If developmentMode is enabled, the url is not versioned and if
     * href points to a bundle, the files in the bundle are printed
     * as individual link tags.
     *
     *
     * @param href The url to the resource. From tag parameter.
     * @param rel The rel attribute. Optional, default is <b>stylesheet</b>. From tag parameter.
     * @param type The type attribute. Optional, default is <b>text/css</b>. From tag parameter.
     * @param params Additional parameters from tag.
     * @param request The current request.
     * @return A link tag with versioned href, ready to use in page.
     */
    public static String getStylesheetTag(final String href, final String rel, final String type, final Map<String, String> params, final HttpServletRequest request) {
        if (developmentMode && isBundle(href)) {
            return unbundleCssBundle(href, rel, type, params, request);
        }
        return writeCssTag(href, rel, type, params, request);
    }


    /**
     * Configures the WarpDrive Runtime.
     *
     * @param config The configuration values to use.
     */
    static void configure(final Properties config) {
        developmentMode = Boolean.valueOf(config.getProperty(DEV_MODE_KEY));
        version = config.getProperty(VERSION_KEY);
        imagesDir = config.getProperty(IMAGE_DIR_KEY);
        jsDir = config.getProperty(JS_DIR_KEY);
        cssDir = config.getProperty(CSS_DIR_KEY);
        configureExternalHosts(config);
        if (developmentMode) {
            configureBundles(config);
        }
    }

    /**
     *
     * Gets the buffer used to store buffered scripts during a request.
     * The buffer is created first time this method is called.
     *
     * @param request The current request.
     * @return The scriptbuffer for current request.
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
     * Removes the scriptbuffer from the request.
     *
     * @param request The current request.
     */
    private static void clearScriptBuffer(final HttpServletRequest request) {
        request.removeAttribute(SCRIPT_BUFFER_KEY);
    }

    /**
     *
     * Creates link tag.
     *
     * @param href The url to the unversioned css file.
     * @param rel The rel attribute. Optional, default is stylesheet.
     * @param type The type attribute. Optional, default is text/css.
     * @param params Additional parameters.
     * @param request The current request.
     * @return Complete link tag for use in page.
     */
    private static String writeCssTag(final String href, final String rel, final String type, final Map<String, String> params, final HttpServletRequest request) {
        StringBuilder buffer = new StringBuilder();
        buffer.append("<link href=\"");
        appendVersionedLink(href, buffer, cssDir, request, true);
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
        addAdditionalParameters(params, buffer);
        buffer.append("/>");
        return buffer.toString();
    }

    /**
     *
     * Creates script tag.
     *
     * @param src The script source.
     * @param type The type attribute. Optional, default is text/javascript.
     * @param params Additional parameters.
     * @param request The current request.
     * @return A complete script tag for use in page.
     */
    private static String writeScriptTag(final String src, final String type, final Map<String, String> params, final HttpServletRequest request) {

        final StringBuilder buffer = new StringBuilder();

        buffer.append("<script src=\"");
        appendVersionedLink(src, buffer, jsDir, request, true);

        buffer.append("\" type=\"");
        if (type == null || "".equals(type)) {
            buffer.append("text/javascript");
        } else {
            buffer.append(type);
        }
        buffer.append("\" ");
        addAdditionalParameters(params, buffer);
        buffer.append("></script>");
        return buffer.toString();
    }

    /**
     *
     * Adds additional parameters to a tag.
     *
     * @param params Parameters to add
     * @param buffer Buffer currently in use to create a tag.
     */
    private static void addAdditionalParameters(final Map<String, String> params, final StringBuilder buffer) {
        if (params == null) {
            return;
        }
        for (Map.Entry<String, String> entry : params.entrySet()) {
            buffer.append(entry.getKey()).append("=\"").append(entry.getValue()).append("\" ");
        }
    }

    /**
     *
     * Creates a url with version.
     *
     * @param filename Original, unversioned file.
     * @param buffer The buffer currently in use to create a tag.
     * @param topLevelDir The url is prepended with this directory. /js, /image, /css, etc.
     * @param request The current request.
     * @param isTextResource Flag indicating if the requested resource is a text resource. (script or stylesheet).
     */
    private static void appendVersionedLink(final String filename, final StringBuilder buffer, final String topLevelDir, final HttpServletRequest request, final boolean isTextResource) {
        if (developmentMode) {
            buffer.append(request.getContextPath()).append(topLevelDir).append(filename);
            return;
        }
        if (externalHosts != null) {
            int hashCode = filename.hashCode();
            if (hashCode == Integer.MIN_VALUE) {
                hashCode++;
            }
            buffer.append(externalHosts[Math.abs(hashCode) % externalHosts.length]);
        }
        buffer.append(request.getContextPath()).append(topLevelDir);
        String versionedSrc;
        if (isTextResource && isGzipAccepted(request)) {
            versionedSrc = FilenameUtils.insertVersionAndGzipExtension(filename, version);
        } else {
            versionedSrc = FilenameUtils.insertVersion(filename, version);
        }
        buffer.append(versionedSrc);

    }

    /**
     *
     * Checks if a requested resource is a bundles.
     * Used to unbundle bundles in development mode.
     *
     * @param filename The requested file.
     * @return True if the file is a bundle created by the WarpDrive plugin.
     */
    private static boolean isBundle(final String filename) {
        return BUNDLES.containsKey(filename);
    }

    /**
     *
     * Unbundles a script bundles, writing each of the bundled
     * scripts in its own script tag. Used in development mode.
     *
     * @param src The file, presumably a bundle.
     * @param type The type attribute of the script tags. Optional, default is javascript.
     * @param params Additional parameters to pass to the script tags.
     * @param request The current request.
     * @return Script tags, one for each tag in the bundle.
     */
    private static String unbundleScriptBundle(final String src, final String type, final Map<String, String> params, final HttpServletRequest request) {
        StringBuilder builder = new StringBuilder();
        for (String script : BUNDLES.get(src)) {
            builder.append(writeScriptTag(script, type, params, request));
        }
        return builder.toString();
    }

    /**
     *
     * Unbundles a scc bundles, writing each of the bundled
     * stylesheets in its own link tag. Used in development mode.
     *
     * @param href The requested stylesheet, presumably a bundle.
     * @param rel The rel attribute to pass to link tags. Optional, default is stylesheet.
     * @param type The type attribute to pass to link tags. Optional, default is text/css.
     * @param params Additonal parameters to pass to the link tags.
     * @param request The current request.
     * @return link tags, one for each stylesheet in bundle.
     */
    private static String unbundleCssBundle(final String href, final String rel, final String type, final Map<String, String> params, final HttpServletRequest request) {
        StringBuilder builder = new StringBuilder();
        for (String stylesheet : BUNDLES.get(href)) {
            builder.append(writeCssTag(stylesheet, rel, type, params, request));
        }
        return builder.toString();
    }

    /**
     *
     * Checks if the client accepts gzipped content.
     *
     * @param request The current request.
     * @return True if the Accept-Encoding header contains the value <i>gzip</i>.
     */
    private static boolean isGzipAccepted(final HttpServletRequest request) {
        Enumeration acceptEncoding = request.getHeaders("Accept-Encoding");
        boolean gzipAccepted = false;
        while (acceptEncoding.hasMoreElements()) {
            String val = (String) acceptEncoding.nextElement();
            if ("gzip".equals(val)) {
                gzipAccepted = true;
            }
        }
        return gzipAccepted;
    }

    /**
     *
     * Configure bundles from config parameters.
     *
     * @param config Config parameters to use. 
     */
    private static void configureBundles(final Properties config) {
        Enumeration properties = config.propertyNames();
        while (properties.hasMoreElements()) {
            String property = (String) properties.nextElement();
            if (property.startsWith(BUNDLE_PREFIX_KEY)) {
                BUNDLES.put(property.substring(BUNDLE_PREFIX_KEY.length()), Arrays.asList(config.getProperty(property).split(MULTIVAL_SEPARATOR)));
            }
        }
    }

    /**
     *
     * Configure external hosts from config parameteres.
     *
     * @param config Config parameters to use. 
     */
    private static void configureExternalHosts(final Properties config) {
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
