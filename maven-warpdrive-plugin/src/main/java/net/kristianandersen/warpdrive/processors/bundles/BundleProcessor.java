package net.kristianandersen.warpdrive.processors.bundles;

import net.kristianandersen.warpdrive.mojo.WarpDriveMojo;
import net.kristianandersen.warpdrive.utils.FilenameUtils;
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

    public BundleProcessor(WarpDriveMojo mojo) {
        super(mojo);    
    }

    public void createBundles() throws IOException {
        createBundlesInDir(mojo.cssBundles, mojo.cssDir);
        createBundlesInDir(mojo.jsBundles, mojo.jsDir);
    }

    private void createBundlesInDir(Map<String, String> bundle, String bundleDir) throws IOException {
        if (bundle == null || bundle.size() == 0) {
            return;
        }
        for (String bundleName : bundle.keySet()) {
            String filenameWithVersion = FilenameUtils.insertVersion(bundleDir + bundleName, mojo.getVersion());
            String filenameWithVersionAndGzipExtension = FilenameUtils.insertVersionAndGzipExtension(bundleDir + bundleName, mojo.getVersion());
            File outputFile = new File(mojo.webappTargetDir + filenameWithVersion);
            File gzippedOutputFile = new File(mojo.webappTargetDir + filenameWithVersionAndGzipExtension);
            FileOutputStream output = null;
            GZIPOutputStream zippedOutput = null;
            try {
                output = new FileOutputStream(outputFile);
                zippedOutput = new GZIPOutputStream(new FileOutputStream(gzippedOutputFile));
                String files = bundle.get(bundleName);
                for (String file : files.split(",")) {
                    FileInputStream fis = null;
                    try {
                        file = file.trim();
                        String versionedFile = FilenameUtils.insertVersion(bundleDir + file, mojo.getVersion());
                        File f = new File(mojo.webappTargetDir + versionedFile);
                        fis = new FileInputStream(f);
                        byte[] buf = new byte[1048576];
                        int read = 0;
                        while ((read = fis.read(buf)) != -1) {
                            output.write(buf, 0, read);
                            zippedOutput.write(buf, 0, read);
                        }
                    }
                    finally {
                        if (fis != null) {
                            fis.close();
                        }
                    }
                }
            }
            finally {
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
