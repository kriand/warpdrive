package net.kristianandersen.warpdrive;

import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: kriand
 * Date: Mar 31, 2010
 * Time: 9:47:28 PM
 */
public class RuntimeTest {

    @Test
    public void testIsScriptBundle() {
        Properties props = new Properties();
        props.setProperty("enabled", "false");
        props.setProperty(Runtime.BUNDLE_PREFIX_KEY + "bundle.js", "script1.js, script2.js");
        Runtime.configure(props);
        assertTrue(Runtime.isBundle("bundle.js"));
        assertFalse(Runtime.isBundle("not.a.bundle.js"));

    }

    @Test
    public void testIsCssBundle() {
        Properties props = new Properties();
        props.setProperty("enabled", "false");
        props.setProperty(Runtime.BUNDLE_PREFIX_KEY + "bundle.css", "stylesheet1.css, stylesheet2.css");
        Runtime.configure(props);
        assertTrue(Runtime.isBundle("bundle.css"));
        assertFalse(Runtime.isBundle("not.a.bundle.css"));

    }


}
