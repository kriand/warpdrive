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
import net.kristianandersen.warpdrive.processors.upload.s3.S3Uploader;
import net.kristianandersen.warpdrive.utils.FileUtils;
import net.kristianandersen.warpdrive.processors.css.CssFilenameFilter;
import net.kristianandersen.warpdrive.processors.js.JsFilenameFilter;
import net.kristianandersen.warpdrive.processors.images.ImageFilenameFilter;

import java.io.File;
import java.util.Set;
import java.util.Collection;
import java.util.HashSet;

/**
 * Created by IntelliJ IDEA.
 * User: kriand
 * Date: Mar 23, 2010
 * Time: 10:44:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExternalUploadProcessor {

    private final WarpDriveMojo mojo;

    public ExternalUploadProcessor(WarpDriveMojo mojo) {
        this.mojo = mojo;
    }

    public void uploadFiles() throws Exception {

        if(!mojo.uploadFiles) {
            return;
        }

        Set<File> files = getFilesToUpload();
        
        if(mojo.s3Bucket != null && mojo.s3Credentials != null) {

            S3Uploader s3Uploader = new S3Uploader(mojo);
            s3Uploader.uploadFiles(files);
        }

    }

    private Set<File> getFilesToUpload() {
        Set<File> files = new HashSet<File>();

        File cssDir = new File(mojo.webappTargetDir + mojo.cssDir);
        Collection<File> cssFiles = FileUtils.listFiles(cssDir, new CssFilenameFilter(), true);
        files.addAll(cssFiles);

        File jsDir = new File(mojo.webappTargetDir + mojo.jsDir);
        Collection<File> jsFiles = FileUtils.listFiles(jsDir, new JsFilenameFilter(), true);
        files.addAll(jsFiles);

        File imageDir = new File(mojo.webappTargetDir + mojo.imageDir);
        Collection<File> imageFiles = FileUtils.listFiles(imageDir, new ImageFilenameFilter(), true);
        files.addAll(imageFiles);

        return files;
    }


}
