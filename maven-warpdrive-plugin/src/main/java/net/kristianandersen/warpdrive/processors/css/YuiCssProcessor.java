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

import com.yahoo.platform.yui.compressor.CssCompressor;
import net.kristianandersen.warpdrive.mojo.WarpDriveMojo;
import net.kristianandersen.warpdrive.processors.AbstractProcessor;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;


/**
 * Created by IntelliJ IDEA.
 * User: kriand
 * Date: Mar 2, 2010
 * Time: 9:56:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class YuiCssProcessor extends AbstractProcessor {


    private final CssUrlRewriter rewriter;

    public YuiCssProcessor(final WarpDriveMojo mojo) {
        super(mojo, new File(mojo.getWebappSourceDir(), mojo.getCssDir()), "css");
        rewriter = new CssUrlRewriter();
    }

    public final void process() throws Exception {
        getLog().info("Processing CSS files");
        Collection<File> cssFiles = getFileset();
        for (File file : cssFiles) {
            getLog().debug("Rewriting URLs in file: " + file.getName());
            String rewritten = rewriter.rewrite(getMojo(), file);
            CssCompressor compressor = new CssCompressor(new StringReader(rewritten));
            StringWriter s = new StringWriter();
            getLog().debug("Compressing file: " + file.getName());
            compressor.compress(s, getMojo().getYuiCssLineBreak());
            writeFile(file, s.toString());
        }
        getLog().info("All CSS files processed OK");
    }
}
