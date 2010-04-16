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
package net.kristianandersen.warpdrive.utils;

import net.kristianandersen.warpdrive.Runtime;

/**
 * Created by IntelliJ IDEA.
 * @author kriand <a href="http://mailhide.recaptcha.net/d?k=01r9lbYEAtg9V5s1Ru_jtZ1g==&c=-aIoeZ0yU0yPn2kdog349bCmN-h1pe5Ed0LsyuWMbEc=">Show email</a>
 * Date: Mar 3, 2010
 * Time: 6:42:39 PM
 */
public final class FilenameUtils {

    /**
     * Private contructor to prevent instantiation.
     */
    private FilenameUtils() {

    }

    /**
     * 
     * @param file
     * @param version
     * @return
     */
    public static String insertVersion(final String file, final String version) {
        assert file != null : "File should not be null";
        assert version != null : "Version should not be null";
        final StringBuilder result = new StringBuilder();
        final int ext = findExtension(file);
        result.append(file.substring(0, ext))
              .append(Runtime.VERSION_PREFIX)
              .append(version)
              .append(file.substring(ext));
        return result.toString();
    }

    /**
     *
     * @param file
     * @param version
     * @return
     */
    public static String insertVersionAndGzipExtension(final String file, final String version) {
        assert file != null : "File should not be null";
        assert version != null : "Version should not be null";
        final StringBuilder result = new StringBuilder();
        final int ext = findExtension(file);
        result.append(file.substring(0, ext))
              .append(Runtime.VERSION_PREFIX)
              .append(version)
              .append(Runtime.GZIP_EXTENSION)
              .append(file.substring(ext));
        return result.toString();
    }

    /**
     *
     * @param file
     * @return
     */
    private static int findExtension(String file) {
        int lastDot = file.lastIndexOf('.');
        if (lastDot == -1) {
            lastDot = file.length();
        }
        return lastDot;
    }
}
