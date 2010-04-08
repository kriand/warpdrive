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
import net.kristianandersen.warpdrive.processors.AbstractProcessor;
import net.kristianandersen.warpdrive.processors.bundles.BundleProcessor;
import net.kristianandersen.warpdrive.processors.css.YuiCssProcessor;
import net.kristianandersen.warpdrive.processors.images.DefaultImageProcessor;
import net.kristianandersen.warpdrive.processors.js.YuiJsProcessor;
import net.kristianandersen.warpdrive.processors.upload.ExternalUploadProcessor;
import net.kristianandersen.warpdrive.processors.webxml.WebXmlProcessor;
import net.kristianandersen.warpdrive.versioning.CurrentTimeMillisStrategy;
import net.kristianandersen.warpdrive.versioning.VersioningStrategy;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: kriand
 * Date: Mar 2, 2010
 * Time: 5:46:05 PM
 *
 * @goal warpspeed
 * @phase prepare-package
 */
public class WarpDriveMojo extends AbstractMojo {

    /**
     * The Maven Project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

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
    public boolean processWebXml;

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
     * @parameter default-value=false
     */
    public boolean uploadFiles;

    /**
     * @parameter
     */
    public File s3SettingsFile;

    private String version;

    private final VersioningStrategy versioningStrategy = new CurrentTimeMillisStrategy();

    public void execute() throws MojoExecutionException {
        try {
            if (!enabled) {
                return;
            }
            printEyeCatcher();
            assertWarModule();
            normalizeDirectories();
            version = versioningStrategy.getVersion();
            writeWarpDriveConfigFile();
            List<AbstractProcessor> processors = setupProcessors();
            for (AbstractProcessor processor : processors) {
                processor.process();
            }
        }
        catch (Exception ex) {
            throw new MojoExecutionException("Caught Exception", ex);
        }
    }

    public String getVersion() {
        return version;
    }

    private List<AbstractProcessor> setupProcessors() {
        List<AbstractProcessor> processors = new ArrayList<AbstractProcessor>();
        if (processJS) {
            processors.add(new YuiJsProcessor(this));
        }
        if (processCSS) {
            processors.add(new YuiCssProcessor(this));
        }
        if (bundlesAreConfigured()) {
            processors.add(new BundleProcessor(this));
        }
        if (processImages) {
            processors.add(new DefaultImageProcessor(this));
        }
        if (processWebXml) {
            processors.add(new WebXmlProcessor(this));
        }
        if (uploadFiles) {
            processors.add(new ExternalUploadProcessor(this));
        }
        return processors;
    }

    private boolean bundlesAreConfigured() {
        return (cssBundles != null && cssBundles.size() > 0) || (jsBundles != null && jsBundles.size() > 0);
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

    private void writeWarpDriveConfigFile() throws IOException {
        File file = new File(project.getBuild().getOutputDirectory(), Runtime.RUNTIME_CONFIG_FILE);
        file.getParentFile().mkdirs();
        FileWriter writer = null;
        getLog().info("Writing WarpDrive configfile to: " + file.getName());
        try {
            writer = new FileWriter(file);
            writeBooleanValue(Runtime.ENABLED_KEY, enabled, writer);
            writeStringValue(Runtime.VERSION_KEY, version, writer);
            writeStringValue(Runtime.IMAGE_DIR_KEY, imageDir, writer);
            writeStringValue(Runtime.JS_DIR_KEY, jsDir, writer);
            writeStringValue(Runtime.CSS_DIR_KEY, cssDir, writer);
            writeExternalHostsConfig(writer);
            writeBundleConfig(cssBundles, writer);
            writeBundleConfig(jsBundles, writer);

        }
        finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private void writeStringValue(String key, String value, Writer writer) throws IOException {
        writer.write(key);
        writer.write('=');
        writer.write(value);
        writer.write('\n');
    }

    private void writeBooleanValue(String key, boolean value, Writer writer) throws IOException {
        writer.write(key);
        writer.write('=');
        writer.write(String.valueOf(value));
        writer.write('\n');
    }

    private void writeExternalHostsConfig(Writer writer) throws IOException {
        if (externalHosts == null || externalHosts.isEmpty()) {
            return;
        }
        writer.write(Runtime.EXTERNAL_HOSTS_KEY + "=");
        for (int i = 0; i < externalHosts.size(); i++) {
            writer.write(externalHosts.get(i));
            if (i < externalHosts.size() - 1) {
                writer.write(Runtime.MULTIVAL_SEPARATOR);
            }
        }
        writer.write('\n');
    }

    private void writeBundleConfig(Map<String, String> bundle, Writer writer) throws IOException {
        if (bundle == null || bundle.isEmpty()) {
            return;
        }
        for (String key : bundle.keySet()) {
            writer.write(Runtime.BUNDLE_PREFIX_KEY);
            writer.write(key);
            writer.write('=');
            String[] bundleEntries = bundle.get(key).split(",");
            for (int i = 0; i < bundleEntries.length; i++) {
                writer.write(bundleEntries[i].trim());
                if (i < bundleEntries.length - 1) {
                    writer.write(Runtime.MULTIVAL_SEPARATOR);
                }
            }
            writer.write('\n');
        }
    }

    private void printEyeCatcher() {
        getLog().info("  +    .          .      +       .        *  .    .  . .. .........");
        getLog().info("             *        .                 .     .    . .  . .........");
        getLog().info(".     +          .         .       .          .   . . . . .........");
        getLog().info("      .        +     .       +     .      .  .  .  . .. ...........");
        getLog().info(".         +            .          +        .  +   .  . .. .........");
        getLog().info("  .    'warpdrive is      _____       .        .   . .  ... .......");
        getLog().info("     active, captain!'-__/.....\\__        +   .   . . .............");
        getLog().info("   .                     \\_____/            .    .  .. ............");
        getLog().info("      +    .     +                    +    .   .    . .. . . ......");
        getLog().info("  +    .          .      +       .            .    .. . ... .......");
        getLog().info("             *        .                 .    +   . .. . ...........");
        getLog().info("             .        .       *         .        . .  .. . ........");
        getLog().info(".     +          .         .       .          +    . . .. . .......");
    }

}
