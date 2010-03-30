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
package net.kristianandersen.warpdrive.mojo;

import net.kristianandersen.warpdrive.Runtime;
import net.kristianandersen.warpdrive.filter.FilterConfigurator;
import net.kristianandersen.warpdrive.processors.css.CssProcessor;
import net.kristianandersen.warpdrive.processors.css.YuiCssProcessor;
import net.kristianandersen.warpdrive.processors.images.DefaultImageProcessor;
import net.kristianandersen.warpdrive.processors.images.ImageProcessor;
import net.kristianandersen.warpdrive.processors.js.JsProcessor;
import net.kristianandersen.warpdrive.processors.js.YuiJsProcessor;
import net.kristianandersen.warpdrive.processors.sprites.SmartSpritesProcessor;
import net.kristianandersen.warpdrive.processors.sprites.SpritesProcessor;
import net.kristianandersen.warpdrive.upload.FileUploader;
import net.kristianandersen.warpdrive.versioning.CurrentTimeMillisStrategy;
import net.kristianandersen.warpdrive.versioning.VersioningStrategy;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: kriand
 * Date: Mar 2, 2010
 * Time: 5:46:05 PM
 *
 * @goal process
 * @phase process-sources
 */
public class WarpDriveMojo extends AbstractMojo {

    /**
     * The Maven Project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    public MavenProject project;

    /**
     * Webapp source directory.
     *
     * @parameter default-value="${basedir}/src/main/webapp"
     * @required
     */
    public String webappSourceDir;

    /**
     * Webapp target directory.
     *
     * @parameter default-value="${project.build.directory}/${project.build.finalName}"
     * @required
     */
    public String webappTargetDir;

    /**
     * @parameter default-value=true
     */
    public boolean enabled;

    /**
     * @parameter default-value="${basedir}/src/main/webapp/WEB-INF/web.xml"
     */
    public String webXml;

    /**
     * @parameter default-value="js"
     */
    public String jsDir;

    /**
     * @parameter default-value="images"
     */
    public String imageDir;

    /**
     * @parameter default-value="css"
     */
    public String cssDir;

    /**
     * @parameter
     */
    public List<String> externalHosts;

    /**
     * @parameter
     */
    public Map<String, String> jsBundles;

    /**
     * @parameter
     */
    public Map<String, String> cssBundles;

    /**
     * @parameter default-value=true
     */
    public boolean processJS;

    /**
     * @parameter default-value=true
     */
    public boolean processSprites;

    /**
     * @parameter default-value=true
     */
    public boolean processCSS;

    /**
     * @parameter default-value=true
     */
    public boolean processImages;

    /**
     * @parameter default-value=true
     */
    public boolean configureFilter;

    /**
     * @parameter default-value=8000
     */
    public int yuiJsLineBreak;

    /**
     * @parameter default-value=true
     */
    public boolean yuiJsMunge;

    /**
     * @parameter default-value=false
     */
    public boolean yuiJsVerbose;

    /**
     * @parameter default-value=false
     */
    public boolean yuiJsPreserveAllSemicolons;

    /**
     * @parameter default-value=false
     */
    public boolean yuiJsDisableOptimizations;

    /**
     * @parameter default-value=8000
     */
    public int yuiCssLineBreak;

    /**
     * @parameter
     */
    public List<String> smartSpritesIncludeFiles;

    /**
     * @parameter
     */
    public String smartSpritesCssFileSuffix;

    /**
     * @parameter
     */
    public String smartSpritesPngDepth;

    /**
     * @parameter default-value=false
     */
    public boolean smartSpritesPngIE6;

    /**
     * @parameter
     */
    public String smartSpritesCssFileEncoding;

    /**
     * @parameter default-value=false
     */
    public boolean uploadFiles;

    /**
     * @parameter
     */
    public String s3Bucket;

    /**
     * @parameter
     */
    public File s3Credentials;

    private String version;

    private VersioningStrategy versioningStrategy = new CurrentTimeMillisStrategy();

    public void execute() throws MojoExecutionException {
        JsProcessor jsProcessor = new YuiJsProcessor(this);
        SpritesProcessor spritesProcessor = new SmartSpritesProcessor(this);
        CssProcessor cssProcessor = new YuiCssProcessor(this);
        ImageProcessor imageProcessor = new DefaultImageProcessor(this);
        FilterConfigurator filterConfigurator = new FilterConfigurator(this);
        FileUploader uploader = new FileUploader(this);
        try {
            assertWarModule();
            normalizeDirectories();
            version = versioningStrategy.getVersion();
            writeWarpDriveProperties();
            if (enabled) {
                jsProcessor.processJS();
                spritesProcessor.processSprites();
                cssProcessor.processCss();
                imageProcessor.processImages();
                filterConfigurator.configureWebXml();
                uploader.uploadFiles();
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            throw new MojoExecutionException("Caught IOException", ex);
        }
    }

    private void assertWarModule() throws MojoExecutionException {
        if (!"war".equals(project.getPackaging())) {
            throw new MojoExecutionException("maven-warpdrive-plugin can only be used with war modules");
        }
    }

    private void normalizeDirectories() {
        if (!cssDir.endsWith("/")) cssDir = cssDir + "/";
        if (!jsDir.endsWith("/")) jsDir = jsDir + "/";
        if (!imageDir.endsWith("/")) imageDir = imageDir + "/";
        if (!cssDir.startsWith("/")) cssDir = "/" + cssDir;
        if (!jsDir.startsWith("/")) jsDir = "/" + jsDir;
        if (!imageDir.startsWith("/")) imageDir = "/" + imageDir;
    }

    private void writeWarpDriveProperties() throws IOException {
        File file = new File(project.getBuild().getOutputDirectory(), Runtime.RUNTIME_SETTINGS_FILE);
        file.getParentFile().mkdirs();
        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            writer.write(Runtime.ENABLED_KEY + "=" + enabled + "\n");
            writer.write(Runtime.VERSION_KEY + "=" + version + "\n");
            writer.write(Runtime.IMAGE_DIR_KEY + "=" + imageDir + "\n");
            writer.write(Runtime.JS_DIR_KEY + "=" + jsDir + "\n");
            writer.write(Runtime.CSS_DIR_KEY + "=" + cssDir + "\n");
            if (externalHosts != null && externalHosts.size() > 0) {
                writer.write(Runtime.EXTERNAL_HOSTS_KEY + "=");
                for (int i = 0; i < externalHosts.size(); i++) {
                    writer.write(externalHosts.get(i));
                    if (i < externalHosts.size() - 1) {
                        writer.write(',');
                    }
                }
                writer.write('\n');
            }
            if (cssBundles != null && cssBundles.size() > 0) {
                writeBundle(Runtime.CSS_BUNDLE_PREFIX_KEY, cssBundles, writer);
            }
            if (jsBundles != null && jsBundles.size() > 0) {
                writeBundle(Runtime.JS_BUNDLE_PREFIX_KEY, jsBundles, writer);
            }
        }
        finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private void writeBundle(String prefix, Map<String, String> bundles, FileWriter writer) throws IOException {
        for (String key : bundles.keySet()) {
            writer.write(prefix);
            writer.write(key);
            writer.write('=');
            String[] bundleEntries = bundles.get(key).split(",");
            for (int i = 0; i < bundleEntries.length; i++) {
                writer.write(bundleEntries[i].trim());
                if (i < bundleEntries.length - 1) {
                    writer.write(',');
                }
            }
            writer.write('\n');
        }
    }

    public String getVersion() {
        return version;
    }

}