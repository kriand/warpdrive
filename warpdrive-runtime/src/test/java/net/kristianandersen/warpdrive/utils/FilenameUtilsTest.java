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
        assertEquals("myscript_v123.js", result);
    }

    @Test
    public void testAppendVersionWithSubdir() {
        String filename = "/dir/subdir/myscript.js";
        String result = FilenameUtils.insertVersion(filename, "123");
        assertEquals("/dir/subdir/myscript_v123.js", result);
    }

    @Test
    public void testAppendGzipExtension() {
        String filename = "myscript.js";
        String result = FilenameUtils.insertGzipExtension(filename);
        assertEquals("myscript.gz.js", result);
    }

    @Test
    public void testAppendGzipExtensionWithDir() {
        String filename = "/dir/subdir/myscript.js";
        String result = FilenameUtils.insertGzipExtension(filename);
        assertEquals("/dir/subdir/myscript.gz.js", result);
    }

    @Test
    public void testAppendGzipExtensionAndVersion() {
        String filename = "myscript.js";
        String result = FilenameUtils.insertVersion(filename, "123");
        result = FilenameUtils.insertGzipExtension(result);
        assertEquals("myscript_v123.gz.js", result);
    }
}
