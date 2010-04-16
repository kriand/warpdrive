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
 * @author kriand <a href="http://mailhide.recaptcha.net/d?k=01r9lbYEAtg9V5s1Ru_jtZ1g==&c=-aIoeZ0yU0yPn2kdog349bCmN-h1pe5Ed0LsyuWMbEc=">Show email</a>
 * Date: Mar 3, 2010
 * Time: 8:54:57 PM 
 */
public class DefaultImageProcessor extends AbstractProcessor {

    /**
     *
     * Constructor.
     *
     * @param mojo The Maven plugin holds configuration
     */
    public DefaultImageProcessor(final WarpDriveMojo mojo) {
        super(mojo, new File(mojo.getWebappSourceDir(), mojo.getImageDir()), "gif", "png", "jpg", "jpeg");
    }

    /**
     *
     * Processes all files in the configured image-directory with extension, gif, png, jpg or jpeg.
     * For images, the only processing performed is inserting version in filename.
     *
     * @throws Exception If a file could not be read or written. 
     * @see net.kristianandersen.warpdrive.processors.AbstractProcessor#process() 
     */
    public final void process() throws Exception {
        getLog().info("Processing image files");
        Collection<File> imageFiles = getFileset();
        for (File f : imageFiles) {
            getLog().debug("Processing file: " + f.getName());
            addVersionToFile(f);
        }
        getLog().info("All image files processed OK");
    }
}
