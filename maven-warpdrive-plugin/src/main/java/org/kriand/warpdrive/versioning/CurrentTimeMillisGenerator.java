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

/**
 *
 * Simple versiongenerator, just returning the currenttime in millis.
 *
 * Created by IntelliJ IDEA.
 * @author kriand <a href="http://mailhide.recaptcha.net/d?k=01r9lbYEAtg9V5s1Ru_jtZ1g==&c=-aIoeZ0yU0yPn2kdog349bCmN-h1pe5Ed0LsyuWMbEc=">Show email</a>
 * Date: Mar 2, 2010
 * Time: 6:07:58 PM
 */
public class CurrentTimeMillisGenerator extends AbstractVersionGenerator {

    /**
     * Constructor.
     *
     * @param mojo The WarpDrive plugin
     * @see org.kriand.warpdrive.versioning.AbstractVersionGenerator#AbstractVersionGenerator(org.kriand.warpdrive.mojo.WarpDriveMojo)
     */
    public CurrentTimeMillisGenerator(final WarpDriveMojo mojo) {
        super(mojo);
    }

    /**
     *
     * Returns the version as current time in mills.
     *
     * @return The version as current time in mills.
     * @see AbstractVersionGenerator#getVersionNumber()
     */
    public final String getVersionNumber() {
        return String.valueOf(System.currentTimeMillis());
    }
}
