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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
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

    private final WarpDriveMojo mojo;

    protected AbstractProcessor(final WarpDriveMojo inMojo) {
        this(inMojo, (File[]) null);
    }

    protected AbstractProcessor(final WarpDriveMojo inMojo, final File inWorkDir, final String... inFileExtensions) {
        this(inMojo, new File[]{inWorkDir}, inFileExtensions);
    }

    protected AbstractProcessor(final WarpDriveMojo inMojo, final File[] inWorkDirs, final String... inFileExtensions) {
        this.mojo = inMojo;
        this.workDirs = inWorkDirs;
        this.fileExtensions = inFileExtensions;
    }

    public abstract void process() throws Exception;

    protected final Log getLog() {
        return getMojo().getLog();
    }

    protected final Collection<File> getFileset() {
        List<File> result = new ArrayList<File>();
        for (File workDir : workDirs) {
            result.addAll(FileUtils.listFiles(workDir, fileExtensions, true));
        }
        return result;
    }

    protected final void writeFile(final File originalFile, final String data) throws IOException {
        String baseFilename = getBaseFileName(originalFile, getMojo().getWebappSourceDir());
        String filenameWithVersion = FilenameUtils.insertVersion(baseFilename, getMojo().getVersion());
        String filenameWithVersionAndGzipExtension = FilenameUtils.insertVersionAndGzipExtension(baseFilename, getMojo().getVersion());
        File output = new File(getMojo().getWebappTargetDir() + filenameWithVersion);
        File gzippedOutput = new File(getMojo().getWebappTargetDir() + filenameWithVersionAndGzipExtension);
        if(!output.getParentFile().mkdirs()) {
            throw new IOException("Unable to write file: " + output);
        }
        FileWriter writer = new FileWriter(output);
        OutputStreamWriter zipWriter = new OutputStreamWriter((new GZIPOutputStream(new FileOutputStream(gzippedOutput))));
        try {
            writer.write(data);
            zipWriter.write(data);
        } finally {
            if (writer != null) {
                writer.close();
            }
            if (zipWriter != null) {
                zipWriter.close();
            }
        }
    }

    protected final void writeFile(final File originalFile) throws IOException {
        String baseFilename = getBaseFileName(originalFile, getMojo().getWebappSourceDir());
        String filenameWithVersion = FilenameUtils.insertVersion(baseFilename, getMojo().getVersion());
        File output = new File(getMojo().getWebappTargetDir() + filenameWithVersion);
        if(!output.getParentFile().mkdirs()) {
            throw new IOException("Unable to write file: " + output);
        }
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(originalFile);
            fos = new FileOutputStream(output);
            byte[] b = new byte[WarpDriveMojo.WRITE_BUFFER_SIZE];
            int i = 0;
            while ((i = fis.read(b)) != -1) {
                fos.write(b, 0, i);
            }
        } finally {
            if (fis != null) {
                fis.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
    }

    private String getBaseFileName(final File originalFile, final String basedir) {
        String o = originalFile.getAbsolutePath();
        return o.substring(o.indexOf(basedir) + basedir.length());
    }

    protected final WarpDriveMojo getMojo() {
        return mojo;
    }
}
