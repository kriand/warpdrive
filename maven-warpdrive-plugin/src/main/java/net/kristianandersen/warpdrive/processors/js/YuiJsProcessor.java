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
package net.kristianandersen.warpdrive.processors.js;

import com.yahoo.platform.yui.compressor.JavaScriptCompressor;
import net.kristianandersen.warpdrive.processors.AbstractProcessor;
import net.kristianandersen.warpdrive.mojo.WarpDriveMojo;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: kriand
 * Date: Mar 2, 2010
 * Time: 7:46:28 PM
 */
public class YuiJsProcessor extends AbstractProcessor {

    public YuiJsProcessor(WarpDriveMojo mojo) {
        super(mojo, new File(mojo.webappSourceDir, mojo.jsDir), "js" );
    }

    public void process() throws Exception{
        log().info("Processing JS files");
        Collection<File> jsFiles = getFileset();
        for (File file : jsFiles) {
            log().debug("Compressing file: " + file);
            JavaScriptCompressor compressor = new JavaScriptCompressor(new FileReader(file), new JsErrorReporter(mojo));
            StringWriter s = new StringWriter();
            compressor.compress(s, mojo.yuiJsLineBreak,
                                   mojo.yuiJsMunge,
                                   mojo.yuiJsVerbose,
                                   mojo.yuiJsPreserveAllSemicolons,
                                   mojo.yuiJsDisableOptimizations);
            writeFile(file, s.toString());
        }
        log().info("All JS files processed OK");
    }

}
