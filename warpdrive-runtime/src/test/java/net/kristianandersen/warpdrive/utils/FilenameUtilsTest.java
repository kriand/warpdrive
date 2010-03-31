package net.kristianandersen.warpdrive.utils;

import org.junit.Test;
import static junit.framework.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: kriand
 * Date: Mar 30, 2010
 * Time: 8:14:24 PM
 */
public class FilenameUtilsTest {

    @Test
    public void testAppendVersion() {
        String filename = "myscript.js";
        String result = FilenameUtils.insertVersion(filename, "123");
        assertEquals("myscript__v123.js", result);
    }

    @Test
    public void testAppendVersionWithSubdir() {
        String filename = "/dir/subdir/myscript.js";
        String result = FilenameUtils.insertVersion(filename, "123");
        assertEquals("/dir/subdir/myscript__v123.js", result);
    }

    @Test
    public void testAppendVersionAndGzipExtension() {
        String filename = "myscript.js";
        String result = FilenameUtils.insertVersionAndGzipExtension(filename, "123");
        assertEquals("myscript__v123.gz.js", result);
    }

    @Test
    public void testAppendVersionAndGzipExtensionWithSubdir() {
        String filename = "/dir/subdir/myscript.js";
        String result = FilenameUtils.insertVersionAndGzipExtension(filename, "123");
        assertEquals("/dir/subdir/myscript__v123.gz.js", result);
    }

    @Test
    public void testAppendVersionToNoExtension() {
        String filename = "myscript";
        String result = FilenameUtils.insertVersion(filename, "123");
        assertEquals("myscript__v123", result);
    }

    @Test
    public void testAppendVersionAndGzipExtensionToNoExtension() {
        String filename = "myscript";
        String result = FilenameUtils.insertVersionAndGzipExtension(filename, "123");
        assertEquals("myscript__v123.gz", result);
    }
        
}
