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
public class MavenVersionGenerator extends AbstractVersionGenerator {

    /**
     *
     * Constructor.
     *
     * @param mojo The WarpDrive plugin
     * @see AbstractVersionGenerator#AbstractVersionGenerator(org.kriand.warpdrive.mojo.WarpDriveMojo)
     */
    public MavenVersionGenerator(final WarpDriveMojo mojo) {
        super(mojo);
    }

    /**
     * Returns the current Maven version as the version.
     *
     * @return The current Maven version.
     * @see AbstractVersionGenerator#getVersionNumber()
     */
    public final String getVersionNumber() throws IllegalStateException {
        return getMojo().getProject().getVersion();
    }
}