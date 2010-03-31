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
package net.kristianandersen.warpdrive.processors.css;

import net.kristianandersen.warpdrive.mojo.WarpDriveMojo;
import net.kristianandersen.warpdrive.utils.FilenameUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: kriand
 * Date: Mar 3, 2010
 * Time: 8:31:58 PM
 * To change this template use File | Settings | File Templates.
 */
class CssUrlRewriter {


    public String rewrite(WarpDriveMojo mojo, File cssFile) throws IOException {
        StringBuilder css = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(cssFile));
        String line = reader.readLine();
        while (line != null) {
            int index = line.indexOf("url(");
            if (index == -1 || line.contains("http")) {
                css.append(line);
            } else {
                int index2 = line.indexOf(')', index);
                String url = line.substring(index + 4, index2);
                css.append(line.substring(0, index + 4));

                if (url.startsWith("'")) {
                   css.append("'");
                    url = url.substring(1);
                }

                if (url.startsWith("\"")) {
                   css.append("\"");
                    url = url.substring(1);
                }

                if(url.startsWith("..")) {
                    url = url.substring(2);
                }

                String versionedUrl = FilenameUtils.insertVersion(url, mojo.getVersion());
                if(mojo.externalHosts != null) {
                    css.append(mojo.externalHosts.get(Math.abs(versionedUrl.hashCode()) % mojo.externalHosts.size()));
                }
                css.append(versionedUrl);
                css.append(line.substring(index2));
            }
            css.append('\n');
            line = reader.readLine();
        }
        return css.toString();
    }
}
