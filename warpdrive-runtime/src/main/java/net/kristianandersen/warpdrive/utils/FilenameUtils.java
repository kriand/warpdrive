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
 * User: kriand
 * Date: Mar 3, 2010
 * Time: 6:42:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class FilenameUtils {

    public static String insertVersion(String file, String version) {
        StringBuilder result = new StringBuilder();
        int lastDot = file.lastIndexOf('.');
        result.append(file.substring(0, lastDot)).append("_v").append(version).append(file.substring(lastDot));
        return result.toString();
    }

    public static String insertGzipExtension(String file) {
        StringBuilder result = new StringBuilder();
        int lastDot = file.lastIndexOf('.');
        result.append(file.substring(0, lastDot)).append(Runtime.GZIP_EXTENSION).append(file.substring(lastDot));
        return result.toString();
    }
}
