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
package net.kristianandersen.warpdrive.processors;

import net.kristianandersen.warpdrive.mojo.WarpDriveMojo;
import net.kristianandersen.warpdrive.utils.FilenameUtils;

import java.io.*;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: kriand
 * Date: Mar 2, 2010
 * Time: 8:44:20 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractProcessor {


    protected WarpDriveMojo mojo;

    public AbstractProcessor(WarpDriveMojo mojo) {
        this.mojo = mojo;

    }

    protected void writeFile(File originalFile, String data) throws IOException {
        String baseFilename = getBaseFileName(originalFile, mojo.webappSourceDir);
        String filenameWithVersion = FilenameUtils.insertVersion(baseFilename, mojo.getVersion());
        File output = new File(mojo.webappTargetDir + filenameWithVersion);
        File gzippedOutput = new File(mojo.webappTargetDir + FilenameUtils.insertGzipExtension(filenameWithVersion));
        output.getParentFile().mkdirs();
        FileWriter writer = new FileWriter(output);
        OutputStreamWriter zipWriter = new OutputStreamWriter((new GZIPOutputStream(new FileOutputStream(gzippedOutput))));
        try {
            writer.write(data);
            zipWriter.write(data);            
        }
        finally {
            if (writer != null) {
                writer.close();
            }
            if (zipWriter != null) {
                zipWriter.close();
            }
        }
    }

    protected void writeFile(File originalFile) throws IOException {
        String baseFilename = getBaseFileName(originalFile, mojo.webappSourceDir);
        String filenameWithVersion = FilenameUtils.insertVersion(baseFilename, mojo.getVersion());
        File output = new File(mojo.webappTargetDir + filenameWithVersion);
        output.getParentFile().mkdirs();
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(originalFile);
            fos = new FileOutputStream(output);
            byte[] b = new byte[4096 * 8];
            int i = 0;
            while ((i = fis.read(b)) != -1) {
                fos.write(b, 0, i);
            }
        }
        finally {
            if (fis != null) {
                fis.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
    }

    protected void writeBundles(String bundleDir, Map<String, String> bundleConfig) throws IOException {
        if (bundleConfig == null || bundleConfig.size() == 0) {
            return;
        }
        for (String bundleName : bundleConfig.keySet()) {
            String filenameWithVersion = FilenameUtils.insertVersion(bundleDir + bundleName, mojo.getVersion());
            File outputFile = new File(mojo.webappTargetDir + filenameWithVersion);
            File gzippedOutputFile = new File(mojo.webappTargetDir + FilenameUtils.insertGzipExtension(filenameWithVersion));
            FileOutputStream output = null;
            GZIPOutputStream zippedOutput = null;
            try {
                output = new FileOutputStream(outputFile);
                zippedOutput = new GZIPOutputStream(new FileOutputStream(gzippedOutputFile));
                String files = bundleConfig.get(bundleName);
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

    private String getBaseFileName(File originalFile, String basedir) {
        String o = originalFile.getAbsolutePath();
        return o.substring(o.indexOf(basedir) + basedir.length());
    }


}
