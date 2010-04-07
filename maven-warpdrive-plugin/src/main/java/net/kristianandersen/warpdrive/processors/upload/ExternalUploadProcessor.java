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
package net.kristianandersen.warpdrive.processors.upload;

import net.kristianandersen.warpdrive.mojo.WarpDriveMojo;
import net.kristianandersen.warpdrive.processors.AbstractProcessor;

import java.io.File;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: kriand
 * Date: Mar 23, 2010
 * Time: 10:44:15 PM
 */
public class ExternalUploadProcessor extends AbstractProcessor {

    public ExternalUploadProcessor(int priority, WarpDriveMojo mojo) {
        super(priority,
                mojo,
                new File[]{new File(mojo.webappTargetDir + mojo.cssDir),
                        new File(mojo.webappTargetDir + mojo.jsDir),
                        new File(mojo.webappTargetDir + mojo.imageDir)},
                "css", "js", "gif", "png", "jpg", "jpeg");
    }

    public void process() throws Exception {
        Collection<File> files = getFileset();
        if (mojo.s3SettingsFile != null) {
            S3Uploader s3Uploader = new S3Uploader(mojo);
            s3Uploader.uploadFiles(files);
        }
    }
}
