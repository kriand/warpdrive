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
package org.kriand.warpdrive.processors.upload;

import org.kriand.warpdrive.mojo.WarpDriveMojo;
import org.kriand.warpdrive.processors.AbstractProcessor;

import java.io.File;
import java.util.Collection;

/**
 *
 * Processor for handling uploads of resources to external locations.
 * Currently, only <a href="http://aws.amazon.com/s3/">Amazon S3</a> is supported.
 *
 *
 * @author kriand <a href="http://mailhide.recaptcha.net/d?k=01r9lbYEAtg9V5s1Ru_jtZ1g==&c=-aIoeZ0yU0yPn2kdog349bCmN-h1pe5Ed0LsyuWMbEc=">Show email</a>
 * Date: Mar 23, 2010
 * Time: 10:44:15 PM
 */
public class ExternalUploadProcessor extends AbstractProcessor {

    /**
     *
     * Constructs a new upload processor, with configurations from the mojo.
     *
     * @param mojo The WarpDrive plugin, containing configuration.
     */
    public ExternalUploadProcessor(final WarpDriveMojo mojo) {
        super(mojo,
                new File[]{new File(mojo.getWebappTargetDir() + mojo.getCssDir()),
                        new File(mojo.getWebappTargetDir() + mojo.getJsDir()),
                        new File(mojo.getWebappTargetDir() + mojo.getImageDir())},
                "css", "js", "gif", "png", "jpg", "jpeg");
    }

    /**
     *
     * Starts the fileupload-process.
     *
     * @throws Exception If something goes wrong during upload
     */
    public final void process() throws Exception {
        Collection<File> files = getFileset();
        if (getMojo().getS3SettingsFile() != null) {
            getLog().info(String.format("Uploading %s files to S3", files.size()));
            S3Uploader s3Uploader = new S3Uploader(getMojo(), getLog());
            s3Uploader.uploadFiles(files);
            getLog().info("All files uploaded OK");
        }
    }
}
