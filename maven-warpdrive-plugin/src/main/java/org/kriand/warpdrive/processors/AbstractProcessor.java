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
package org.kriand.warpdrive.processors;

import org.kriand.warpdrive.mojo.WarpDriveMojo;
import org.kriand.warpdrive.utils.FilenameUtils;
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
 *
 * Baseclass with common functionality for all WarpDrive processors
 *
 * Created by IntelliJ IDEA.
 * @author kriand <a href="http://mailhide.recaptcha.net/d?k=01r9lbYEAtg9V5s1Ru_jtZ1g==&c=-aIoeZ0yU0yPn2kdog349bCmN-h1pe5Ed0LsyuWMbEc=">Show email</a>
 * Date: Mar 2, 2010
 * Time: 8:44:20 PM
 */
public abstract class AbstractProcessor {

    /**
     * A list of fileextensions that this processors will use
     * when searching for files to process. Only files with an extension
     * contained in this array will be processed. 
     */
    private String[] fileExtensions;

    /**
     * Holds an array of directories which the processor will use when looking
     * for files to process.
     */
    private File[] workDirs;

    /**
     * Reference to the Maven plugin. For easy access to configuration-values.
     */
    private final WarpDriveMojo mojo;

    /**
     *
     * Default constructor, for processors that do not operate
     * versioned resources
     *
     * @param inMojo Maven plugin, for config.
     */
    protected AbstractProcessor(final WarpDriveMojo inMojo) {
        this(inMojo, (File[]) null);
    }

    /**
     *
     * Construtor for processors operating on a single workdir.
     *
     * @param inMojo Maven plugin, for config.
     * @param inWorkDir A workdir where the processor will look for files to process.
     * @param inFileExtensions A list of fileextensions that this processors will process.
     */
    protected AbstractProcessor(final WarpDriveMojo inMojo, final File inWorkDir, final String... inFileExtensions) {
        this(inMojo, new File[]{inWorkDir}, inFileExtensions);
    }

    /**
     *
     * Construtor for processors operating on multiple workdirs.
     *
     * @param inMojo Maven plugin, for config.
     * @param inWorkDirs A list of workdirs where the processor will look for files to process.
     * @param inFileExtensions A list of fileextensions that this processors will process.
     */
    protected AbstractProcessor(final WarpDriveMojo inMojo, final File[] inWorkDirs, final String... inFileExtensions) {
        this.mojo = inMojo;
        this.workDirs = inWorkDirs;
        this.fileExtensions = inFileExtensions;
    }

    /**
     *
     * The main entrypoint for processors, this method is called from the WarpDrive plugin.
     *
     * @throws Exception If Processing can not continue.
     */
    public abstract void process() throws Exception;

    /**
     *
     * Enables processors to easily log to the Maven log.
     *
     * @return  A reference to the Maven Log.
     */
    protected final Log getLog() {
        return getMojo().getLog();
    }

    /**
     *
     * Gets the set of files that the processor should process, based on
     * the provided workdir(s) and fileextensions
     *
      * @return A collection of files that the processor should process.
     */
    protected final Collection<File> getFileset() {
        final List<File> result = new ArrayList<File>();
        for (File workDir : workDirs) {
            result.addAll(FileUtils.listFiles(workDir, fileExtensions, true));
        }
        return result;
    }

    /**
     *
     * Writes the provided data to a versioned file.
     *
     * @param originalFile The original file, used to derive the filename of the new file.
     * @param data The contents of the file.
     * @throws IOException If the file could not be written.
     */
    protected final void writeVersionedFile(final File originalFile, final String data) throws IOException {
        final String baseFilename = getFilenameRelativeToBasedir(originalFile, getMojo().getWebappSourceDir());
        final String filenameWithVersion = FilenameUtils.insertVersion(baseFilename, getMojo().getVersion());
        final String filenameWithVersionAndGzipExtension = FilenameUtils.insertVersionAndGzipExtension(baseFilename, getMojo().getVersion());
        final File output = new File(getMojo().getWebappTargetDir() + filenameWithVersion);
        final File gzippedOutput = new File(getMojo().getWebappTargetDir() + filenameWithVersionAndGzipExtension);
        final boolean created = output.getParentFile().mkdirs();
        if(created) {
            getLog().info(String.format("Created directoty %s", output.getParentFile()));
        }
        FileWriter writer = null;
        OutputStreamWriter zipWriter = null;
        try {
            writer= new FileWriter(output);
            zipWriter = new OutputStreamWriter((new GZIPOutputStream(new FileOutputStream(gzippedOutput))));
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

    /**
     *
     * Writes the contents of a file to a new file with the same name, except with added version.
     *
     * @param originalFile The original file, with the contents to be written to the new file.
     * @throws IOException If the original file could not be read or new file could not be written.
     */
    protected final void addVersionToFile(final File originalFile) throws IOException {
        final String baseFilename = getFilenameRelativeToBasedir(originalFile, getMojo().getWebappSourceDir());
        final String filenameWithVersion = FilenameUtils.insertVersion(baseFilename, getMojo().getVersion());
        final File output = new File(getMojo().getWebappTargetDir() + filenameWithVersion);
        final boolean created = output.getParentFile().mkdirs();
        if(created) {
            getLog().info(String.format("Created directoty %s", output.getParentFile()));
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

    /**
     *
     * Gives subclasses access to the Maven plugin
     *
     * @return The Maven plugin
     */
    protected final WarpDriveMojo getMojo() {
        return mojo;
    }

    /**
     *
     * Calculates a filename relative to a basedir.
     *
     * @param file Zhe file
     * @param basedir The basedir.
     * @return Path to the file, relative to the basedir.
     */
    private String getFilenameRelativeToBasedir(final File file, final String basedir) {
        String o = file.getAbsolutePath();
        return o.substring(o.indexOf(basedir) + basedir.length());
    }
}
