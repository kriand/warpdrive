package net.kristianandersen.warpdrive.versioning;

import net.kristianandersen.warpdrive.mojo.WarpDriveMojo;

/**
 * Created by IntelliJ IDEA.
 *
 * @author kriand <a href="http://mailhide.recaptcha.net/d?k=01r9lbYEAtg9V5s1Ru_jtZ1g==&c=-aIoeZ0yU0yPn2kdog349bCmN-h1pe5Ed0LsyuWMbEc=">Show email</a>
 *         Date: Apr 19, 2010
 *         Time: 7:43:33 PM
 */
public class VersionProvider {

    /**
     *
     */
    private static final Class<? extends AbstractVersionGenerator> DEFAULT_VERSION_GENERATOR_IMPL = CurrentTimeMillisGenerator.class;

    /**
     *
     */
    private AbstractVersionGenerator versionGenerator;

    /**
     * @param mojo
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
     *
     * @param mojo
     * @return
     */
    private Class<? extends AbstractVersionGenerator> determineVersionGeneratorClass(final WarpDriveMojo mojo) {
        Class<? extends AbstractVersionGenerator> generatorClass = DEFAULT_VERSION_GENERATOR_IMPL;
        if (mojo.getVersionGeneratorClass() != null) {
            try {
                generatorClass = (Class<? extends AbstractVersionGenerator>) Class.forName(mojo.getVersionGeneratorClass());
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Unable to load provided versiongenerator", e);
            }
        }
        else if (isProjectUsingSvn(mojo)) {
            generatorClass = SvnRevNumberGenerator.class;
        }
        return generatorClass;
    }

    /**
     * @return
     */
    public String getVersion() {
        assert versionGenerator != null : "versionGenerator was null";
        return versionGenerator.doGetVersion();
    }

    /**
     * 
     * @param mojo
     * @return
     */
    private boolean isProjectUsingSvn(final WarpDriveMojo mojo) {
        return mojo.getProject().getScm().getConnection().startsWith("scm:svn");
    }

}
