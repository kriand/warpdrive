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
import net.kristianandersen.warpdrive.utils.FileUtils;
import net.kristianandersen.warpdrive.mojo.WarpDriveMojo;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

import java.io.*;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: kriand
 * Date: Mar 2, 2010
 * Time: 7:46:28 PM
 */
public class YuiJsProcessor extends AbstractProcessor implements JsProcessor {


    public YuiJsProcessor(WarpDriveMojo mojo) {
        super(mojo);
    }


    public void processJS() throws IOException{

        if (!mojo.processJS || mojo.jsDir == null) {
            return;
        }

        File jsDir = new File(mojo.webappSourceDir, mojo.jsDir);

        if(!jsDir.exists()) {
            return;
        }

        Collection<File> jsFiles = FileUtils.listFiles(jsDir, new JsFilenameFilter(), true);

        for (File file : jsFiles) {
            JavaScriptCompressor compressor = new JavaScriptCompressor(new FileReader(file), new JsErrorReporter(mojo));
            StringWriter s = new StringWriter();
            compressor.compress(s, mojo.yuiJsLineBreak,
                                   mojo.yuiJsMunge,
                                   mojo.yuiJsVerbose,
                                   mojo.yuiJsPreserveAllSemicolons,
                                   mojo.yuiJsDisableOptimizations);
            writeFile(file, s.toString());
        }        
    }

}
