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
package net.kristianandersen.warpdrive.processors.sprites;

import net.kristianandersen.warpdrive.mojo.WarpDriveMojo;
import net.kristianandersen.warpdrive.processors.AbstractProcessor;
import net.kristianandersen.warpdrive.processors.css.CssFilenameFilter;
import net.kristianandersen.warpdrive.utils.FileUtils;
import org.carrot2.labs.smartsprites.SmartSpritesParameters;
import org.carrot2.labs.smartsprites.SpriteBuilder;
import org.carrot2.labs.smartsprites.message.Message;
import org.carrot2.labs.smartsprites.message.MessageLog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: kriand
 * Date: Mar 7, 2010
 * Time: 7:12:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class SmartSpritesProcessor extends AbstractProcessor implements SpritesProcessor {


    public SmartSpritesProcessor(WarpDriveMojo mojo) {
        super(mojo);
    }

    public void processSprites() throws IOException {
        if (!mojo.processSprites || mojo.cssDir == null) {
            return;
        }
        File cssDir = new File(mojo.webappSourceDir, mojo.cssDir);

        if (!cssDir.exists()) {
            return;
        }
        setupOutputDirs();
        processSprites(cssDir);
    }


    private void setupOutputDirs() {
        File outputDir = new File(mojo.webappTargetDir + mojo.cssDir);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
    }

    private void processSprites(File cssDir) throws IOException {

        List<String> cssFilenames = getFilesToProcess(cssDir);
        SmartSpritesParameters params = setupParams(cssFilenames);
        if (params != null) {
            MavenLogMessageSink sink = new MavenLogMessageSink(mojo.getLog());
            MessageLog messageLog = new MessageLog(sink);
            SpriteBuilder builder = new SpriteBuilder(params, messageLog);
            builder.buildSprites();
        }
    }

    private List<String> getFilesToProcess(File cssDir) {
        List<String> cssFilenames = new ArrayList<String>();
        if (mojo.smartSpritesIncludeFiles != null) {
            for (String file : mojo.smartSpritesIncludeFiles) {
                cssFilenames.add(mojo.webappSourceDir + mojo.cssDir + file);
            }
        } else {
            Collection<File> cssFiles = FileUtils.listFiles(cssDir, new CssFilenameFilter(), true);
            for (File cssFile : cssFiles) {
                cssFilenames.add(cssFile.getAbsolutePath());
            }
        }
        return cssFilenames;
    }

    private SmartSpritesParameters setupParams(List<String> cssFiles) {

        String cssFileSuffix = SmartSpritesParameters.DEFAULT_CSS_FILE_SUFFIX;
        SmartSpritesParameters.PngDepth pngDepth = SmartSpritesParameters.DEFAULT_SPRITE_PNG_DEPTH;
        String cssFileEncoding = SmartSpritesParameters.DEFAULT_CSS_FILE_ENCODING;

        if (mojo.smartSpritesCssFileSuffix != null) {
            cssFileSuffix = mojo.smartSpritesCssFileSuffix;
        }
        if (mojo.smartSpritesPngDepth != null) {
            try {
                pngDepth = SmartSpritesParameters.PngDepth.valueOf(mojo.smartSpritesPngDepth);
            }
            catch (IllegalArgumentException iaex) {
                mojo.getLog().error(String.format("Invalid value for configuration parameter smartSpritesPngDepth, must be on of %s", Arrays.deepToString(SmartSpritesParameters.PngDepth.values())));
                throw iaex;
            }
        }
        if (mojo.smartSpritesCssFileEncoding != null) {
            cssFileEncoding = mojo.smartSpritesCssFileEncoding;
        }
        return new SmartSpritesParameters(mojo.webappSourceDir + mojo.cssDir,
                cssFiles,
                mojo.webappSourceDir + mojo.cssDir,
                mojo.webappTargetDir + mojo.cssDir,
                Message.MessageLevel.IE6NOTICE,
                cssFileSuffix,
                pngDepth,
                mojo.smartSpritesPngIE6,
                cssFileEncoding);
    }

}
