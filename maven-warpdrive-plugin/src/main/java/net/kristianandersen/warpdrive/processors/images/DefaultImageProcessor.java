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

import java.io.File;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: kriand
 * Date: Mar 3, 2010
 * Time: 8:54:57 PM 
 */
public class DefaultImageProcessor extends AbstractProcessor {

    public DefaultImageProcessor(final WarpDriveMojo mojo) {
        super(mojo, new File(mojo.getWebappSourceDir(), mojo.getImageDir()), "gif", "png", "jpg", "jpeg");
    }

    public final void process() throws Exception {
        getLog().info("Processing image files");
        Collection<File> imageFiles = getFileset();
        for (File f : imageFiles) {
            getLog().debug("Processing file: " + f.getName());
            writeFile(f);                        
        }
        getLog().info("All image files processed OK");
    }
}
