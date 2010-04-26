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
package org.kriand.warpdrive.processors.bundles;

import org.kriand.warpdrive.mojo.WarpDriveMojo;
import org.kriand.warpdrive.processors.AbstractProcessor;
import org.kriand.warpdrive.utils.FilenameUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

/**
 * Bundles configured scripts or stylesheets together, reducing the number
 * of files and consequently the number of requests from each page.
 *
 * 
 * @author kriand <a href="http://mailhide.recaptcha.net/d?k=01r9lbYEAtg9V5s1Ru_jtZ1g==&c=-aIoeZ0yU0yPn2kdog349bCmN-h1pe5Ed0LsyuWMbEc=">Show email</a>
 * Date: Apr 1, 2010
 * Time: 1:20:38 AM
 */
public class BundleProcessor extends AbstractProcessor {

    /**
     * Creates a new instance, configuration is provided through
     * {@linkplain org.kriand.warpdrive.mojo.WarpDriveMojo}.
     *
     * @param mojo The WarpDrive Maven plugin provides all configuration.
     */
    public BundleProcessor(final WarpDriveMojo mojo) {
        super(mojo);
    }

    /**
     * Performs the bundling, creating one new file for each bundle.
     *
     * @throws Exception If a bundle can not be created due to fawlty configuration, io errors, etc.
     * @see org.kriand.warpdrive.processors.AbstractProcessor#process()
     */
    public final void process() throws Exception {

        getLog().info("Processing css bundles in: " + getMojo().getCssBundles());
        createBundlesInDir(getMojo().getCssBundles(), getMojo().getCssDir());

        getLog().info("Processing js bundles in: " + getMojo().getJsBundles());
        createBundlesInDir(getMojo().getJsBundles(), getMojo().getJsDir());

        getLog().info("All bundles created OK");
    }

    /**
     * Creates a set of configured bundles.
     *
     * @param bundles The bundles to create. The keys will be the bundle filenames.
     *                The values are comma-separated lists containing the files in each bundle.
     * @param bundleDir The directory where the bundle will be created.
     * @throws IOException If the bundles can not be created.
     */
    private void createBundlesInDir(final Map<String, String> bundles, final String bundleDir) throws IOException {
        assert bundleDir == null : "Bundledir was null";
        if (bundles == null || bundles.size() == 0) {
            getLog().info(String.format("No bundles configured in directory: %s", bundleDir));
            return;
        }
        for (Map.Entry<String, String> bundleEntry : bundles.entrySet()) {

            final String filenameWithVersion = FilenameUtils.insertVersion(bundleDir + bundleEntry.getKey(), getMojo().getVersion());
            final String filenameWithVersionAndGzipExtension = FilenameUtils.insertVersionAndGzipExtension(bundleDir + bundleEntry.getKey(), getMojo().getVersion());

            final File outputFile = new File(getMojo().getWebappTargetDir() + filenameWithVersion);
            final File gzippedOutputFile = new File(getMojo().getWebappTargetDir() + filenameWithVersionAndGzipExtension);

            FileOutputStream output = null;
            GZIPOutputStream zippedOutput = null;

            try {
                output = new FileOutputStream(outputFile);
                zippedOutput = new GZIPOutputStream(new FileOutputStream(gzippedOutputFile));
                final String files = bundleEntry.getValue();
                for (String file : files.split(org.kriand.warpdrive.Runtime.MULTIVAL_SEPARATOR)) {
                    FileInputStream fis = null;
                    try {
                        file = file.trim();
                        final String versionedFile = FilenameUtils.insertVersion(bundleDir + file, getMojo().getVersion());
                        final File f = new File(getMojo().getWebappTargetDir() + versionedFile);
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
