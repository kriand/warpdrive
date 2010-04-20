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
package org.kriand.warpdrive.processors.versioning;

import org.kriand.warpdrive.mojo.WarpDriveMojo;

/**
 * Created by IntelliJ IDEA.
 *
 * @author kriand <a href="http://mailhide.recaptcha.net/d?k=01r9lbYEAtg9V5s1Ru_jtZ1g==&c=-aIoeZ0yU0yPn2kdog349bCmN-h1pe5Ed0LsyuWMbEc=">Show email</a>
 * Date: Apr 19, 2010
 * Time: 7:08:19 PM
 */
public abstract class AbstractVersionGenerator {

    /**
     * Hold a reference to the WarpDrive plugin. Enables
     * versiongenerators to leverage configuration form pom.xml
     */
    private WarpDriveMojo mojo;

    /**
     *
     * This constructor is called by the WarpDrive plugin.
     *
     * @param inMojo A reference to the WarpDrive plugin.
     */
    public AbstractVersionGenerator(final WarpDriveMojo inMojo) {
        this.mojo = inMojo;
    }

    /**
     *
     * Called by {@linkplain org.kriand.warpdrive.processors.versioning.VersionProvider}
     * Gets the versionnumber by calling {@linkplain AbstractVersionGenerator#getVersionNumber()} and makes sure it gets logged.
     *
     * @return The versionnumber WarpDrive will use for this build.
     */
    final String doGetVersion() {
        String versionNumber = getVersionNumber();
        logVersionNumber(versionNumber);
        return versionNumber;
    }

    /**
     * Subclasses implement their versioning strategies here.
     * @return The versionnumber WarpDrive will use for this build.
     */
    protected abstract String getVersionNumber();

    /**
     *
     * Returns the WarpDrive plugin.
     *
     * @return The WarpDrive plugin
     */
    protected final WarpDriveMojo getMojo() {
        return mojo;
    }

    /**
     * Logs the versionnumber to the Maven Log.
     *
     * @param versionNumber The versionnumber to log.
     */
    protected final void logVersionNumber(final String versionNumber) {
        getMojo().getLog().info(String.format("** Will use versionnumber %s to version files **", versionNumber));
    }

}
