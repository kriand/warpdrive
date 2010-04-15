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
package net.kristianandersen.warpdrive.processors.bundles;

import net.kristianandersen.warpdrive.mojo.WarpDriveMojo;
import net.kristianandersen.warpdrive.utils.FilenameUtils;
import net.kristianandersen.warpdrive.Runtime;
import net.kristianandersen.warpdrive.processors.AbstractProcessor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: kriand
 * Date: Apr 1, 2010
 * Time: 1:20:38 AM
 */
public class BundleProcessor extends AbstractProcessor {

    /**
     *
     * @param mojo
     */
    public BundleProcessor(final WarpDriveMojo mojo) {
        super(mojo);    
    }

    /**
     *
     * @throws Exception
     */
    public final void process() throws Exception {
        getLog().info("Processing css bundles in: " + getMojo().getCssBundles());
        createBundlesInDir(getMojo().getCssBundles(), getMojo().getCssDir());
        getLog().info("Processing js bundles in: " + getMojo().getJsBundles());
        createBundlesInDir(getMojo().getJsBundles(), getMojo().getJsDir());
        getLog().info("All bundles created OK");
    }

    /**
     * 
     * @param bundle
     * @param bundleDir
     * @throws IOException
     */
    private void createBundlesInDir(final Map<String, String> bundle, final String bundleDir) throws IOException {
        if (bundle == null || bundle.size() == 0) {
            return;
        }
        for (Map.Entry<String, String> bundleEntry : bundle.entrySet()) {

            String filenameWithVersion = FilenameUtils.insertVersion(bundleDir + bundleEntry.getKey(), getMojo().getVersion());
            String filenameWithVersionAndGzipExtension = FilenameUtils.insertVersionAndGzipExtension(bundleDir + bundleEntry.getKey(), getMojo().getVersion());

            File outputFile = new File(getMojo().getWebappTargetDir() + filenameWithVersion);
            File gzippedOutputFile = new File(getMojo().getWebappTargetDir() + filenameWithVersionAndGzipExtension);

            FileOutputStream output = null;
            GZIPOutputStream zippedOutput = null;

            try {
                output = new FileOutputStream(outputFile);
                zippedOutput = new GZIPOutputStream(new FileOutputStream(gzippedOutputFile));
                String files = bundleEntry.getValue();
                for (String file : files.split(Runtime.MULTIVAL_SEPARATOR)) {
                    FileInputStream fis = null;
                    try {
                        file = file.trim();
                        String versionedFile = FilenameUtils.insertVersion(bundleDir + file, getMojo().getVersion());
                        File f = new File(getMojo().getWebappTargetDir() + versionedFile);
                        fis = new FileInputStream(f);
                        byte[] buf = new byte[WarpDriveMojo.WRITE_BUFFER_SIZE];
                        int read = 0;
                        while ((read = fis.read(buf)) != -1) {
                            output.write(buf, 0, read);
                            zippedOutput.write(buf, 0, read);
                        }
                    } finally {
                        if (fis != null) {
                            fis.close();
                        }
                    }
                }
            } finally {
                if (output != null) {
                    output.close();
                }
                if (zippedOutput != null) {
                    zippedOutput.close();
                }
            }
        }
    }
}
