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
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.logging.Log;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.GZIPOutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: kriand
 * Date: Mar 2, 2010
 * Time: 8:44:20 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractProcessor {

    private String[] fileExtensions;

    private File[] workDirs;

    protected final WarpDriveMojo mojo;

    protected AbstractProcessor(WarpDriveMojo mojo) {
        this(mojo, (File[])null );
    }

    protected AbstractProcessor(WarpDriveMojo mojo, File workDir, String... fileExtensions) {
        this(mojo, new File[]{workDir}, fileExtensions );
    }

    protected AbstractProcessor(WarpDriveMojo mojo, File[] workDirs, String... fileExtensions) {
        this.mojo = mojo;
        this.workDirs = workDirs;
        this.fileExtensions = fileExtensions;
    }

    public abstract void process() throws Exception;

    protected Log log() {
        return mojo.getLog();
    }

    protected Collection<File> getFileset() {
        List<File> result = new ArrayList<File>();
        for (File workDir : workDirs) {
            result.addAll(FileUtils.listFiles(workDir, fileExtensions, true));
        }
        return result;
    }

    protected void writeFile(File originalFile, String data) throws IOException {
        String baseFilename = getBaseFileName(originalFile, mojo.webappSourceDir);
        String filenameWithVersion = FilenameUtils.insertVersion(baseFilename, mojo.getVersion());
        String filenameWithVersionAndGzipExtension = FilenameUtils.insertVersionAndGzipExtension(baseFilename, mojo.getVersion());
        File output = new File(mojo.webappTargetDir + filenameWithVersion);
        File gzippedOutput = new File(mojo.webappTargetDir + filenameWithVersionAndGzipExtension);
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

    private String getBaseFileName(File originalFile, String basedir) {
        String o = originalFile.getAbsolutePath();
        return o.substring(o.indexOf(basedir) + basedir.length());
    }
}
