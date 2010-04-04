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

import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: kriand
 * Date: Mar 31, 2010
 * Time: 9:47:28 PM
 */
public class RuntimeTest {

    @Test
    public void testIsScriptBundle() throws Exception {
        Properties props = new Properties();
        props.setProperty("enabled", "false");
        props.setProperty(Runtime.BUNDLE_PREFIX_KEY + "bundle.js", "script1.js, script2.js");
        Runtime.configure(props);
        assertTrue(Runtime.isBundle("bundle.js"));
        assertFalse(Runtime.isBundle("not.a.bundle.js"));

    }

    @Test
    public void testIsCssBundle() throws Exception {
        Properties props = new Properties();
        props.setProperty("enabled", "false");
        props.setProperty(Runtime.BUNDLE_PREFIX_KEY + "bundle.css", "stylesheet1.css, stylesheet2.css");
        Runtime.configure(props);
        assertTrue(Runtime.isBundle("bundle.css"));
        assertFalse(Runtime.isBundle("not.a.bundle.css"));

    }

    @Test
    public void testBufferScripts() throws Exception {
        final StringBuilder buffer = new StringBuilder();
        final String bufferedScript = "<script src=\"script.js\"></script>";
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getAttribute(Runtime.SCRIPT_BUFFER_KEY)).thenReturn(buffer);
        Runtime.bufferScripts(request, bufferedScript);
        assertEquals(bufferedScript.trim(), buffer.toString().trim());
    }


}
