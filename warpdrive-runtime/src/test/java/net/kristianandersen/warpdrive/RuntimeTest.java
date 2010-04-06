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

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * Created by IntelliJ IDEA.
 * User: kriand
 * Date: Mar 31, 2010
 * Time: 9:47:28 PM
 */
public class RuntimeTest {

    @Test
    public void testBufferScripts() throws Exception {
        final StringBuilder buffer = new StringBuilder();
        final String scriptToBuffer = "<script src=\"script.js\"></script>";
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getAttribute(Runtime.SCRIPT_BUFFER_KEY)).thenReturn(buffer);
        Runtime.bufferScripts(request, scriptToBuffer);
        assertEquals(scriptToBuffer, buffer.toString());
    }

    @Test
    public void testGetBufferedScripts() throws Exception {
        final StringBuilder buffer = new StringBuilder();
        final String scriptToBuffer1 = "<script src=\"script1.js\"></script>";
        final String scriptToBuffer2 = "<script src=\"script2.js\"></script>";
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getAttribute(Runtime.SCRIPT_BUFFER_KEY)).thenReturn(buffer);
        Runtime.bufferScripts(request, scriptToBuffer1);
        Runtime.bufferScripts(request, scriptToBuffer2);
        String result = Runtime.getBufferedScripts(request);
        assertEquals(scriptToBuffer1 + scriptToBuffer2, result);
    }

    @Test
    public void testGetGZippedScriptTagUsingDefaultParams() throws Exception {
        Properties props = new Properties();
        props.setProperty(Runtime.ENABLED_KEY, "true");
        props.setProperty(Runtime.VERSION_KEY, "123");
        props.setProperty(Runtime.JS_DIR_KEY, "/js/");
        Runtime.configure(props);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeaders("Accept-Encoding")).thenReturn(new StringTokenizer("gzip"));
        when(request.getContextPath()).thenReturn("");
        String tag = Runtime.getScriptTag("myscript.js", null, new HashMap<String, String>(), request);
        assertEquals("<script src=\"/js/myscript__v123.gz.js\" type=\"text/javascript\" ></script>", tag);
    }

    @Test
    public void testGetGZippedScriptTag() throws Exception {
        Properties props = new Properties();
        props.setProperty(Runtime.ENABLED_KEY, "true");
        props.setProperty(Runtime.VERSION_KEY, "123");
        props.setProperty(Runtime.JS_DIR_KEY, "/js/");
        Runtime.configure(props);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeaders("Accept-Encoding")).thenReturn(new StringTokenizer("gzip"));
        when(request.getContextPath()).thenReturn("");
        String tag = Runtime.getScriptTag("myscript.js", "text/myscript", new HashMap<String, String>(), request);
        assertEquals("<script src=\"/js/myscript__v123.gz.js\" type=\"text/myscript\" ></script>", tag);
    }

    @Test
    public void testGetScriptTagUsingDefaultParams() throws Exception {
        Properties props = new Properties();
        props.setProperty(Runtime.ENABLED_KEY, "true");
        props.setProperty(Runtime.VERSION_KEY, "123");
        props.setProperty(Runtime.JS_DIR_KEY, "/js/");
        Runtime.configure(props);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeaders("Accept-Encoding")).thenReturn(new StringTokenizer("nozip"));
        when(request.getContextPath()).thenReturn("");
        String tag = Runtime.getScriptTag("myscript.js", null, new HashMap<String, String>(), request);
        assertEquals("<script src=\"/js/myscript__v123.js\" type=\"text/javascript\" ></script>", tag);
    }

    @Test
    public void testGetScriptTag() throws Exception {
        Properties props = new Properties();
        props.setProperty(Runtime.ENABLED_KEY, "true");
        props.setProperty(Runtime.VERSION_KEY, "123");
        props.setProperty(Runtime.JS_DIR_KEY, "/js/");
        Runtime.configure(props);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeaders("Accept-Encoding")).thenReturn(new StringTokenizer("nozip"));
        when(request.getContextPath()).thenReturn("");
        String tag = Runtime.getScriptTag("myscript.js", "text/myscript", new HashMap<String, String>(), request);
        assertEquals("<script src=\"/js/myscript__v123.js\" type=\"text/myscript\" ></script>", tag);
    }

    @Test
    public void testGetImageTag() throws Exception {
        Properties props = new Properties();
        props.setProperty(Runtime.ENABLED_KEY, "true");
        props.setProperty(Runtime.VERSION_KEY, "123");
        props.setProperty(Runtime.IMAGE_DIR_KEY, "/image/");
        Runtime.configure(props);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getContextPath()).thenReturn("");
        String tag = Runtime.getImageTag("myimage.png", new HashMap<String, String>(), request);
        assertEquals("<img src=\"/image/myimage__v123.png\" />", tag);
    }

    @Test
    public void testGetGZippedStylesheetTagUsingDefaultParams() throws Exception {
        Properties props = new Properties();
        props.setProperty(Runtime.ENABLED_KEY, "true");
        props.setProperty(Runtime.VERSION_KEY, "123");
        props.setProperty(Runtime.CSS_DIR_KEY, "/css/");
        Runtime.configure(props);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeaders("Accept-Encoding")).thenReturn(new StringTokenizer("gzip"));
        when(request.getContextPath()).thenReturn("");
        String tag = Runtime.getStylesheetTag("mystyle.css", null, null, new HashMap<String, String>(), request);
        assertEquals("<link href=\"/css/mystyle__v123.gz.css\" rel=\"stylesheet\" type=\"text/css\" />", tag);
    }

    @Test
    public void testGetGZippedStylesheetTag() throws Exception {
        Properties props = new Properties();
        props.setProperty(Runtime.ENABLED_KEY, "true");
        props.setProperty(Runtime.VERSION_KEY, "123");
        props.setProperty(Runtime.CSS_DIR_KEY, "/css/");
        Runtime.configure(props);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeaders("Accept-Encoding")).thenReturn(new StringTokenizer("gzip"));
        when(request.getContextPath()).thenReturn("");
        String tag = Runtime.getStylesheetTag("mystyle.css", "mystylesheet", "text/mycss", new HashMap<String, String>(), request);
        assertEquals("<link href=\"/css/mystyle__v123.gz.css\" rel=\"mystylesheet\" type=\"text/mycss\" />", tag);
    }

    @Test
    public void testGetStylesheetTagUsingDefaultParams() throws Exception {
        Properties props = new Properties();
        props.setProperty(Runtime.ENABLED_KEY, "true");
        props.setProperty(Runtime.VERSION_KEY, "123");
        props.setProperty(Runtime.CSS_DIR_KEY, "/css/");
        Runtime.configure(props);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeaders("Accept-Encoding")).thenReturn(new StringTokenizer("nozip"));
        when(request.getContextPath()).thenReturn("");
        String tag = Runtime.getStylesheetTag("mystyle.css", null, null, new HashMap<String, String>(), request);
        assertEquals("<link href=\"/css/mystyle__v123.css\" rel=\"stylesheet\" type=\"text/css\" />", tag);
    }

    @Test
    public void testGetStylesheetTag() throws Exception {
        Properties props = new Properties();
        props.setProperty(Runtime.ENABLED_KEY, "true");
        props.setProperty(Runtime.VERSION_KEY, "123");
        props.setProperty(Runtime.CSS_DIR_KEY, "/css/");
        Runtime.configure(props);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeaders("Accept-Encoding")).thenReturn(new StringTokenizer("nozip"));
        when(request.getContextPath()).thenReturn("");
        String tag = Runtime.getStylesheetTag("mystyle.css", "mystylesheet", "text/mycss", new HashMap<String, String>(), request);
        assertEquals("<link href=\"/css/mystyle__v123.css\" rel=\"mystylesheet\" type=\"text/mycss\" />", tag);
    }

    @Test
    public void testJsUnbundle() throws Exception {
        Properties props = new Properties();
        props.setProperty(Runtime.ENABLED_KEY, "false");
        props.setProperty(Runtime.VERSION_KEY, "123");
        props.setProperty(Runtime.JS_DIR_KEY, "/js/");
        props.setProperty(Runtime.BUNDLE_PREFIX_KEY + "mybundle.js", "script.js,dir/myscript.js");
        Runtime.configure(props);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getContextPath()).thenReturn("");
        String expected = "<script src=\"/js/script.js\" type=\"text/myscript\" ></script><script src=\"/js/dir/myscript.js\" type=\"text/myscript\" ></script>";
        assertEquals(expected, Runtime.getScriptTag("mybundle.js", "text/myscript", null, request));
    }

    @Test
    public void testJsUnbundleUsingDefaultParams() throws Exception {
        Properties props = new Properties();
        props.setProperty(Runtime.ENABLED_KEY, "false");
        props.setProperty(Runtime.VERSION_KEY, "123");
        props.setProperty(Runtime.JS_DIR_KEY, "/js/");
        props.setProperty(Runtime.BUNDLE_PREFIX_KEY + "mybundle.js", "script.js,dir/myscript.js");
        Runtime.configure(props);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getContextPath()).thenReturn("");
        String expected = "<script src=\"/js/script.js\" type=\"text/javascript\" ></script><script src=\"/js/dir/myscript.js\" type=\"text/javascript\" ></script>";
        assertEquals(expected, Runtime.getScriptTag("mybundle.js", null, null, request));
    }

    @Test
    public void testCssUnbundle() throws Exception {
        Properties props = new Properties();
        props.setProperty(Runtime.ENABLED_KEY, "false");
        props.setProperty(Runtime.VERSION_KEY, "123");
        props.setProperty(Runtime.CSS_DIR_KEY, "/css/");
        props.setProperty(Runtime.BUNDLE_PREFIX_KEY + "mybundle.css", "stylesheet.css,dir/mystylesheet.css");
        Runtime.configure(props);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getContextPath()).thenReturn("");
        String expected = "<link href=\"/css/stylesheet.css\" rel=\"mystylesheet\" type=\"text/mycss\" /><link href=\"/css/dir/mystylesheet.css\" rel=\"mystylesheet\" type=\"text/mycss\" />";
        assertEquals(expected, Runtime.getStylesheetTag("mybundle.css", "mystylesheet", "text/mycss", null, request));
    }

    @Test
    public void testCssUnbundleUsingDefaultParams() throws Exception {
        Properties props = new Properties();
        props.setProperty(Runtime.ENABLED_KEY, "false");
        props.setProperty(Runtime.VERSION_KEY, "123");
        props.setProperty(Runtime.CSS_DIR_KEY, "/css/");
        props.setProperty(Runtime.BUNDLE_PREFIX_KEY + "mybundle.css", "stylesheet.css,dir/mystylesheet.css");
        Runtime.configure(props);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getContextPath()).thenReturn("");
        String expected = "<link href=\"/css/stylesheet.css\" rel=\"stylesheet\" type=\"text/css\" /><link href=\"/css/dir/mystylesheet.css\" rel=\"stylesheet\" type=\"text/css\" />";
        assertEquals(expected, Runtime.getStylesheetTag("mybundle.css", null, null, null, request));
    }

    @Test
    public void testExternalHost() throws Exception {
        Properties props = new Properties();
        props.setProperty(Runtime.ENABLED_KEY, "true");
        props.setProperty(Runtime.VERSION_KEY, "123");
        props.setProperty(Runtime.CSS_DIR_KEY, "/css/");
        props.setProperty(Runtime.EXTERNAL_HOSTS_KEY, "http://www.example.com");
        Runtime.configure(props);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getContextPath()).thenReturn("");
        when(request.getHeaders("Accept-Encoding")).thenReturn(new StringTokenizer("gzip"));
        String expected = "<link href=\"http://www.example.com/css/stylesheet__v123.gz.css\" rel=\"stylesheet\" type=\"text/css\" />";
        assertEquals(expected, Runtime.getStylesheetTag("stylesheet.css", null, null, null, request));
    }
}
