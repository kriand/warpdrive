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
import org.apache.maven.model.Scm;
import org.apache.maven.project.MavenProject;

/**
 * Factory for obtaining versionnumbers.
 * <p/>
 * Created by IntelliJ IDEA.
 *
 * @author kriand <a href="http://mailhide.recaptcha.net/d?k=01r9lbYEAtg9V5s1Ru_jtZ1g==&c=-aIoeZ0yU0yPn2kdog349bCmN-h1pe5Ed0LsyuWMbEc=">Show email</a>
 *         Date: Apr 19, 2010
 *         Time: 7:43:33 PM
 */
public class VersionProvider {

    /**
     * The default version generator implementation to use.
     */
    private static final Class<? extends AbstractVersionGenerator> DEFAULT_VERSION_GENERATOR_IMPL = CurrentTimeMillisGenerator.class;

    /**
     * The version generator to be used.
     */
    private AbstractVersionGenerator versionGenerator;

    /**
     * Constructor.
     *
     * @param mojo The WarpDrive plugin
     */
    public VersionProvider(final WarpDriveMojo mojo) {
        Class<? extends AbstractVersionGenerator> generatorClass = determineVersionGeneratorClass(mojo);
        try {
            versionGenerator = generatorClass.getConstructor(WarpDriveMojo.class).newInstance(mojo);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to instantiate provided versiongenerator", e);
        }
    }

    /**
     * Determines which versiongenerator to use by inspecting the project pom. Fallback to default.
     *
     * @param mojo The WarpDrive plugin
     * @return A suitable implementation of {@linkplain org.kriand.warpdrive.versioning.AbstractVersionGenerator}
     * @throws IllegalArgumentException If the project pom specifies a class that can not be found.
     */
    private Class<? extends AbstractVersionGenerator> determineVersionGeneratorClass(final WarpDriveMojo mojo) throws IllegalArgumentException {
        if (mojo.getVersionGeneratorClass() != null) {
            try {
                return (Class<? extends AbstractVersionGenerator>) Class.forName(mojo.getVersionGeneratorClass());
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Unable to load versiongenerator class specified in project pom", e);
            }
        } else if (isProjectUsingSvn(mojo)) {
            return SvnRevNumberGenerator.class;
        } else {
            return DEFAULT_VERSION_GENERATOR_IMPL;
        }
    }

    /**
     * Calls the underlying versiongenerator to obtain the version.
     *
     * @return The versionumber to be used by WarpDrive.
     */
    public final String getVersion() {
        assert versionGenerator != null : "versionGenerator was null";
        return versionGenerator.doGetVersion();
    }

    /**
     * Checks if a project is using SVN by inspecting the scm element of the pom.
     * Projects using SVN should use {@linkplain org.kriand.warpdrive.versioning.SvnRevNumberGenerator}
     *
     * @param mojo The WarpDrive plugin
     * @return True if the project is configured to use SVN, false otherwise.
     */
    private boolean isProjectUsingSvn(final WarpDriveMojo mojo) {
        MavenProject project = mojo.getProject();
        if (project != null) {
            Scm scm = project.getScm();
            if (scm != null) {
                if (scm.getConnection() != null && scm.getConnection().startsWith("scm:svn")) {
                    return true;
                }
            }
        }
        return false;
    }
}
