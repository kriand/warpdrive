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
package org.kriand.warpdrive.utils;

import org.junit.Test;
import static junit.framework.Assert.*;

/**
 *
 * Unittests for {@linkplain org.kriand.warpdrive.utils.FilenameUtils}
 *
 * Created by IntelliJ IDEA.
 * @author kriand <a href="http://mailhide.recaptcha.net/d?k=01r9lbYEAtg9V5s1Ru_jtZ1g==&c=-aIoeZ0yU0yPn2kdog349bCmN-h1pe5Ed0LsyuWMbEc=">Show email</a>
 * Date: Mar 30, 2010
 * Time: 8:14:24 PM
 */
public class FilenameUtilsTest {

    @Test
    public void testAppendVersion() {
        String filename = "myscript.js";
        String result = FilenameUtils.insertVersion(filename, "123");
        assertEquals("myscript_v123.js", result);
    }

    @Test
    public void testAppendVersionWithSubdir() {
        String filename = "/dir/subdir/myscript.js";
        String result = FilenameUtils.insertVersion(filename, "123");
        assertEquals("/dir/subdir/myscript_v123.js", result);
    }

    @Test
    public void testAppendVersionAndGzipExtension() {
        String filename = "myscript.js";
        String result = FilenameUtils.insertVersionAndGzipExtension(filename, "123");
        assertEquals("myscript_v123.gz.js", result);
    }

    @Test
    public void testAppendVersionAndGzipExtensionWithSubdir() {
        String filename = "/dir/subdir/myscript.js";
        String result = FilenameUtils.insertVersionAndGzipExtension(filename, "123");
        assertEquals("/dir/subdir/myscript_v123.gz.js", result);
    }

    @Test
    public void testAppendVersionToNoExtension() {
        String filename = "myscript";
        String result = FilenameUtils.insertVersion(filename, "123");
        assertEquals("myscript_v123", result);
    }

    @Test
    public void testAppendVersionAndGzipExtensionToNoExtension() {
        String filename = "myscript";
        String result = FilenameUtils.insertVersionAndGzipExtension(filename, "123");
        assertEquals("myscript_v123.gz", result);
    }
        
}
