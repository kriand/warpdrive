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

import org.kriand.warpdrive.Runtime;

/**
 *
 * Utilityclass for manipulating filenames, inserting version etc.
 * Used both runtime and buildtime.
 *
 *  IDEA.
 * @author kriand <a href="http://mailhide.recaptcha.net/d?k=01r9lbYEAtg9V5s1Ru_jtZ1g==&c=-aIoeZ0yU0yPn2kdog349bCmN-h1pe5Ed0LsyuWMbEc=">Show email</a>
 * Date: Mar 3, 2010
 * Time: 6:42:39 PM
 */
public final class FilenameUtils {

    /**
     * Private contructor, class should never be instantiated.
     */
    private FilenameUtils() {

    }

    /**
     *
     * Inserts version in the provided filename, using correct WarpDrive syntax.
     *
     * @param filename An unversioned filename.
     * @param version The version to insert.
     * @return A versioned filename, according to WarpDrive syntax.
     */
    public static String insertVersion(final String filename, final String version) {
        assert filename != null : "Filename should not be null";
        assert version != null : "Version should not be null";
        final StringBuilder result = new StringBuilder();
        final int ext = findExtension(filename);
        result.append(filename.substring(0, ext))
              .append(org.kriand.warpdrive.Runtime.VERSION_PREFIX)
              .append(version)
              .append(filename.substring(ext));
        return result.toString();
    }

    /**
     * Inserts version and gzip-extension in the
     * provided filename, using correct WarpDrive syntax.
     *
     * @param filename An unversioned filename.
     * @param version The version to insert.
     * @return A versioned filename with gzip extension, according to WarpDrive syntax.
     */
    public static String insertVersionAndGzipExtension(final String filename, final String version) {
        assert filename != null : "Filename should not be null";
        assert version != null : "Version should not be null";
        final StringBuilder result = new StringBuilder();
        final int ext = findExtension(filename);
        result.append(filename.substring(0, ext))
              .append(Runtime.VERSION_PREFIX)
              .append(version)
              .append(Runtime.GZIP_EXTENSION)
              .append(filename.substring(ext));
        return result.toString();
    }

    /**
     *
     * Locates the char position of the extension in the filename.
     * If the filename has no extension, the index of the last character is returned.
     *
     * @param filename A simple filename
     * @return The char position of the extension in the filename.
     */
    private static int findExtension(final String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1) {
            lastDot = filename.length();
        }
        return lastDot;
    }
}
