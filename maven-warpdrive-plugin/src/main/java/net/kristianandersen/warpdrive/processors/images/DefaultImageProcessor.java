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
package net.kristianandersen.warpdrive.processors.images;

import net.kristianandersen.warpdrive.mojo.WarpDriveMojo;
import net.kristianandersen.warpdrive.processors.AbstractProcessor;
import net.kristianandersen.warpdrive.utils.FileUtils;
import org.carrot2.labs.smartsprites.SmartSpritesParameters;
import org.carrot2.labs.smartsprites.SpriteBuilder;
import org.carrot2.labs.smartsprites.message.MessageLog;
import org.carrot2.labs.smartsprites.message.Message;
import org.carrot2.labs.smartsprites.message.MemoryMessageSink;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: kriand
 * Date: Mar 3, 2010
 * Time: 8:54:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class DefaultImageProcessor extends AbstractProcessor implements ImageProcessor {

    public DefaultImageProcessor(WarpDriveMojo mojo) {
        super(mojo);
    }


    public void processImages() throws IOException {
        
        if (!mojo.processImages || mojo.imageDir == null) {
            return;
        }
        File imageDir = new File(mojo.webappSourceDir, mojo.imageDir);
        if (!imageDir.exists()) {
            return;
        }
        Collection<File> imageFiles = FileUtils.listFiles(imageDir, new ImageFilenameFilter(), true);
        for (File f : imageFiles) {
            writeFile(f);                        
        }
    }

}
