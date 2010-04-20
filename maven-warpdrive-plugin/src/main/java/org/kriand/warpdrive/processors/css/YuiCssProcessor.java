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
package org.kriand.warpdrive.processors.css;

import com.yahoo.platform.yui.compressor.CssCompressor;
import org.kriand.warpdrive.mojo.WarpDriveMojo;
import org.kriand.warpdrive.processors.AbstractProcessor;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;

/**
 *
 * Compresses CSS files using <a href="http://developer.yahoo.com/yui/compressor/">YUI Compressor</a>.
 * Also handles rewriting of URLs inside CSS to access versioned resources created by WarpDrive.
 *
 * Created by IntelliJ IDEA.
 * @author kriand <a href="http://mailhide.recaptcha.net/d?k=01r9lbYEAtg9V5s1Ru_jtZ1g==&c=-aIoeZ0yU0yPn2kdog349bCmN-h1pe5Ed0LsyuWMbEc=">Show email</a>
 * Date: Mar 2, 2010
 * Time: 9:56:45 PM
 */
public class YuiCssProcessor extends AbstractProcessor {

    /**
     * Handles rewriting of URLs.
     */
    private final CssUrlRewriter rewriter;

    /**
     *
     * Constructor.
     *
     * @param mojo Holds configuration
     */
    public YuiCssProcessor(final WarpDriveMojo mojo) {
        super(mojo, new File(mojo.getWebappSourceDir(), mojo.getCssDir()), "css");
        rewriter = new CssUrlRewriter();
    }

    /**
     *
     * Compresses all cssfiles in the configured cssDirectory.
     *
     * @throws Exception If a file can not be compressed.
     * @see org.kriand.warpdrive.processors.AbstractProcessor#process()
     */
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
            writeVersionedFile(file, s.toString());
        }
        getLog().info("All CSS files processed OK");
    }
}
