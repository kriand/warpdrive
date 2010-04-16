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

import java.io.File;
import java.io.FileReader;
import java.io.StringWriter;
import java.util.Collection;

/**
 *
 * Compresses Javascripts using <a href="http://developer.yahoo.com/yui/compressor/">YUI Compressor</a>.
 *
 * Created by IntelliJ IDEA.
 * @author kriand <a href="http://mailhide.recaptcha.net/d?k=01r9lbYEAtg9V5s1Ru_jtZ1g==&c=-aIoeZ0yU0yPn2kdog349bCmN-h1pe5Ed0LsyuWMbEc=">Show email</a>
 * Date: Mar 2, 2010
 * Time: 7:46:28 PM
 */
public class YuiJsProcessor extends AbstractProcessor {

    /**
     *
     * Constructs a new Javascript processor, with configurations from the mojo.
     *
     * @param mojo Holds configuration.
     */
    public YuiJsProcessor(final WarpDriveMojo mojo) {
        super(mojo, new File(mojo.getWebappSourceDir(), mojo.getJsDir()), "js");
    }

    /**
     *
     * Runs all .js files in the configured js-directory through the YUI Compressor
     *
     * @throws Exception If an error occurs during compression or while writing files.
     * @see net.kristianandersen.warpdrive.processors.AbstractProcessor#process()
     */
    public final void process() throws Exception {
        getLog().info("Processing JS files");
        Collection<File> jsFiles = getFileset();
        for (File file : jsFiles) {
            getLog().debug("Compressing file: " + file);
            JavaScriptCompressor compressor = new JavaScriptCompressor(new FileReader(file), new JsErrorReporter(getMojo()));
            StringWriter s = new StringWriter();
            compressor.compress(s, getMojo().getYuiJsLineBreak(),
                    getMojo().isYuiJsMunge(),
                    getMojo().isYuiJsVerbose(),
                    getMojo().isYuiJsPreserveAllSemicolons(),
                    getMojo().isYuiJsDisableOptimizations());
            writeVersionedFile(file, s.toString());
        }
        getLog().info("All JS files processed OK");
    }

}
