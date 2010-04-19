package net.kristianandersen.warpdrive.versioning;

import net.kristianandersen.warpdrive.mojo.WarpDriveMojo;

/**
 * Created by IntelliJ IDEA.
 *
 * @author kriand <a href="http://mailhide.recaptcha.net/d?k=01r9lbYEAtg9V5s1Ru_jtZ1g==&c=-aIoeZ0yU0yPn2kdog349bCmN-h1pe5Ed0LsyuWMbEc=">Show email</a>
 * Date: Apr 19, 2010
 * Time: 7:08:19 PM
 */
public abstract class AbstractVersionGenerator {

    /**
     *
     */
    private WarpDriveMojo mojo;

    /**
     *
     * @param inMojo
     */
    public AbstractVersionGenerator(final WarpDriveMojo inMojo) {
        this.mojo = inMojo;
    }

    /**
     * 
     * @return
     */
    String doGetVersion() {
        String versionNumber = getVersion();
        logVersionNumber(versionNumber);
        return versionNumber;
    }

    /**
     *
     * @return
     */
    protected abstract String getVersion();

    /**
     *
     * @return
     */
    protected WarpDriveMojo getMojo() {
        return mojo;
    }

    /**
     *
     */
    protected void logVersionNumber(String versionNumber) {
        getMojo().getLog().info(String.format("** Will use versionnumber %s to version files **", versionNumber));
    }

}
