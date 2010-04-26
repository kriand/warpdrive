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
package org.kriand.warpdrive.versioning;

import org.kriand.warpdrive.mojo.WarpDriveMojo;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Version generator suited for projects using SVN. Uses the SVN revisionnumber in the project basedir.
 * <p/>
 *  IDEA.
 *
 * @author kriand <a href="http://mailhide.recaptcha.net/d?k=01r9lbYEAtg9V5s1Ru_jtZ1g==&c=-aIoeZ0yU0yPn2kdog349bCmN-h1pe5Ed0LsyuWMbEc=">Show email</a>
 * Date: Mar 2, 2010
 * Time: 6:07:58 PM
 */
public class SvnRevNumberGenerator extends AbstractVersionGenerator {

    /**
     * Relative path to svn entries file.
     */
    private static final String SVN_ENTRIES_FILE = ".svn/entries";

    /**
     *
     * Constructor.
     *
     * @param mojo The WarpDrive plugin
     * @see org.kriand.warpdrive.versioning.AbstractVersionGenerator#AbstractVersionGenerator(org.kriand.warpdrive.mojo.WarpDriveMojo)
     */
    public SvnRevNumberGenerator(final WarpDriveMojo mojo) {
        super(mojo);
    }

    /**
     * Returns the current svn revisionnumber from the basedir as the version.
     *
     * @return The current svn revisionnumber from the basedir.
     * @see org.kriand.warpdrive.versioning.AbstractVersionGenerator#getVersionNumber()
     * @throws IllegalStateException If the svn-entries file could not be found.
     */
    public final String getVersionNumber() throws IllegalStateException {
        final File svnEntries = new File(getMojo().getProject().getBasedir(), SVN_ENTRIES_FILE);
        if (!svnEntries.exists()) {
            throw new IllegalStateException(String.format("Unable to locate svn-entries file: %s", svnEntries.getAbsolutePath()));
        }
        return String.valueOf(getSvnRevNumber(svnEntries));
    }

    /**
     *
     * Parses the .svn/entries file and gets the revisionnumber. The revisionnumber
     * is expected to be found on the 4. line of the file.
     *
     * @param svnEntries The svn-entries file.
     * @return The revisionnumber from the provided file.
     * @throws IllegalStateException If the svn-entries file could not be parsed.
     */
    private int getSvnRevNumber(final File svnEntries) throws IllegalStateException {
        try {
            String svnEntires = FileUtils.readFileToString(svnEntries);
            String[] lines = svnEntires.split("\n");
            if (lines.length < 4) {
                throw new IllegalStateException(String.format("svn-entries file: %s did not conform to expected format.", svnEntries.getAbsolutePath()));
            }
            int revNumber = Integer.parseInt(lines[3]);
            return revNumber;

        } catch (IOException e) {
            throw new IllegalStateException(String.format("Unable to read svn-entries file: %s", svnEntries.getAbsolutePath()), e);
        }
    }
}
