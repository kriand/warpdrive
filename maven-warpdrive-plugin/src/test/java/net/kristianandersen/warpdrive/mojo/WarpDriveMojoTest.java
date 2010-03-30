package net.kristianandersen.warpdrive.mojo;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: kriand
 * Date: Mar 2, 2010
 * Time: 6:22:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class WarpDriveMojoTest extends AbstractMojoTestCase {

    /**
     * {@inheritDoc}
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * {@inheritDoc}
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * @throws Exception if any
     */
    public void testJsDir() throws Exception {
        File pom = getTestFile("src/test/resources/unit/project-to-test/pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());
        WarpDriveMojo warpDriveMojo = (WarpDriveMojo) lookupMojo("process", pom);
        assertNotNull(warpDriveMojo);
        warpDriveMojo.execute();
    }


}
