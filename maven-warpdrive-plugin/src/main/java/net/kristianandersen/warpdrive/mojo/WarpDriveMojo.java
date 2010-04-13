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
     *
     */
    public static final int WRITE_BUFFER_SIZE = 32768;

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
    private String webappSourceDir;

    /**
     * Webapp target directory.
     *
     * @parameter default-value="${project.build.directory}/${project.build.finalName}"
     * @required
     */
    private String webappTargetDir;

    /**
     * @parameter default-value=true
     */
    private boolean enabled;

    /**
     * @parameter default-value="${basedir}/src/main/webapp/WEB-INF/web.xml"
     */
    private String webXml;

    /**
     * @parameter default-value="js"
     */
    private String jsDir;

    /**
     * @parameter default-value="images"
     */
    private String imageDir;

    /**
     * @parameter default-value="css"
     */
    private String cssDir;

    /**
     * @parameter
     */
    private List<String> externalHosts;

    /**
     * @parameter
     */
    private Map<String, String> jsBundles;

    /**
     * @parameter
     */
    private Map<String, String> cssBundles;

    /**
     * @parameter default-value=true
     */
    private boolean processJS;

    /**
     * @parameter default-value=true
     */
    private boolean processSprites;

    /**
     * @parameter default-value=true
     */
    private boolean processCSS;

    /**
     * @parameter default-value=true
     */
    private boolean processImages;

    /**
     * @parameter default-value=true
     */
    private boolean processWebXml;

    /**
     * @parameter default-value=8000
     */
    private int yuiJsLineBreak;

    /**
     * @parameter default-value=true
     */
    private boolean yuiJsMunge;

    /**
     * @parameter default-value=false
     */
    private boolean yuiJsVerbose;

    /**
     * @parameter default-value=false
     */
    private boolean yuiJsPreserveAllSemicolons;

    /**
     * @parameter default-value=false
     */
    private boolean yuiJsDisableOptimizations;

    /**
     * @parameter default-value=8000
     */
    private int yuiCssLineBreak;

    /**
     * @parameter default-value=false
     */
    private boolean uploadFiles;

    /**
     * @parameter
     */
    private File s3SettingsFile;

    private String version;

    private final VersioningStrategy versioningStrategy = new CurrentTimeMillisStrategy();

    public void execute() throws MojoExecutionException {
        try {
            if (!isEnabled()) {
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
        if (isProcessJS()) {
            processors.add(new YuiJsProcessor(this));
        }
        if (isProcessCSS()) {
            processors.add(new YuiCssProcessor(this));
        }
        if (bundlesAreConfigured()) {
            processors.add(new BundleProcessor(this));
        }
        if (isProcessImages()) {
            processors.add(new DefaultImageProcessor(this));
        }
        if (isProcessWebXml()) {
            processors.add(new WebXmlProcessor(this));
        }
        if (isUploadFiles()) {
            processors.add(new ExternalUploadProcessor(this));
        }
        return processors;
    }

    private boolean bundlesAreConfigured() {
        return (getCssBundles() != null && getCssBundles().size() > 0) || (getJsBundles() != null && getJsBundles().size() > 0);
    }

    private void assertWarModule() throws MojoExecutionException {
        if (!"war".equals(project.getPackaging())) {
            throw new MojoExecutionException("maven-warpdrive-plugin can only be used with war modules");
        }
    }

    private void normalizeDirectories() {
        if (!getCssDir().endsWith("/")) setCssDir(getCssDir() + "/");
        if (!getJsDir().endsWith("/")) setJsDir(jsDir + "/");
        if (!getImageDir().endsWith("/")) setImageDir(getImageDir() + "/");
        if (!getCssDir().startsWith("/")) setCssDir("/" + getCssDir());
        if (!getJsDir().startsWith("/")) setJsDir("/" + getJsDir());
        if (!getImageDir().startsWith("/")) setImageDir("/" + imageDir);
    }

    private void writeWarpDriveConfigFile() throws IOException {
        File file = new File(project.getBuild().getOutputDirectory(), Runtime.RUNTIME_CONFIG_FILE);
        file.getParentFile().mkdirs();
        FileWriter writer = null;
        getLog().info("Writing WarpDrive configfile to: " + file.getName());
        try {
            writer = new FileWriter(file);
            writeBooleanValue(Runtime.ENABLED_KEY, isEnabled(), writer);
            writeStringValue(Runtime.VERSION_KEY, version, writer);
            writeStringValue(Runtime.IMAGE_DIR_KEY, getImageDir(), writer);
            writeStringValue(Runtime.JS_DIR_KEY, getJsDir(), writer);
            writeStringValue(Runtime.CSS_DIR_KEY, getCssDir(), writer);
            writeExternalHostsConfig(writer);
            writeBundleConfig(getCssBundles(), writer);
            writeBundleConfig(getJsBundles(), writer);

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
        if (getExternalHosts() == null || getExternalHosts().isEmpty()) {
            return;
        }
        writer.write(Runtime.EXTERNAL_HOSTS_KEY + "=");
        for (int i = 0; i < getExternalHosts().size(); i++) {
            writer.write(getExternalHosts().get(i));
            if (i < getExternalHosts().size() - 1) {
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

    public String getWebappSourceDir() {
        return webappSourceDir;
    }

    public void setWebappSourceDir(String webappSourceDir) {
        this.webappSourceDir = webappSourceDir;
    }

    public String getWebappTargetDir() {
        return webappTargetDir;
    }

    public void setWebappTargetDir(String webappTargetDir) {
        this.webappTargetDir = webappTargetDir;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getWebXml() {
        return webXml;
    }

    public void setWebXml(String webXml) {
        this.webXml = webXml;
    }

    public String getJsDir() {
        return jsDir;
    }

    public void setJsDir(String jsDir) {
        this.jsDir = jsDir;
    }

    public String getImageDir() {
        return imageDir;
    }

    public void setImageDir(String imageDir) {
        this.imageDir = imageDir;
    }

    public String getCssDir() {
        return cssDir;
    }

    public void setCssDir(String cssDir) {
        this.cssDir = cssDir;
    }

    public List<String> getExternalHosts() {
        return externalHosts;
    }

    public void setExternalHosts(List<String> externalHosts) {
        this.externalHosts = externalHosts;
    }

    public Map<String, String> getJsBundles() {
        return jsBundles;
    }

    public void setJsBundles(Map<String, String> jsBundles) {
        this.jsBundles = jsBundles;
    }

    public Map<String, String> getCssBundles() {
        return cssBundles;
    }

    public void setCssBundles(Map<String, String> cssBundles) {
        this.cssBundles = cssBundles;
    }

    public boolean isProcessJS() {
        return processJS;
    }

    public void setProcessJS(boolean processJS) {
        this.processJS = processJS;
    }

    public boolean isProcessSprites() {
        return processSprites;
    }

    public void setProcessSprites(boolean processSprites) {
        this.processSprites = processSprites;
    }

    public boolean isProcessCSS() {
        return processCSS;
    }

    public void setProcessCSS(boolean processCSS) {
        this.processCSS = processCSS;
    }

    public boolean isProcessImages() {
        return processImages;
    }

    public void setProcessImages(boolean processImages) {
        this.processImages = processImages;
    }

    public boolean isProcessWebXml() {
        return processWebXml;
    }

    public void setProcessWebXml(boolean processWebXml) {
        this.processWebXml = processWebXml;
    }

    public int getYuiJsLineBreak() {
        return yuiJsLineBreak;
    }

    public void setYuiJsLineBreak(int yuiJsLineBreak) {
        this.yuiJsLineBreak = yuiJsLineBreak;
    }

    public boolean isYuiJsMunge() {
        return yuiJsMunge;
    }

    public void setYuiJsMunge(boolean yuiJsMunge) {
        this.yuiJsMunge = yuiJsMunge;
    }

    public boolean isYuiJsVerbose() {
        return yuiJsVerbose;
    }

    public void setYuiJsVerbose(boolean yuiJsVerbose) {
        this.yuiJsVerbose = yuiJsVerbose;
    }

    public boolean isYuiJsPreserveAllSemicolons() {
        return yuiJsPreserveAllSemicolons;
    }

    public void setYuiJsPreserveAllSemicolons(boolean yuiJsPreserveAllSemicolons) {
        this.yuiJsPreserveAllSemicolons = yuiJsPreserveAllSemicolons;
    }

    public boolean isYuiJsDisableOptimizations() {
        return yuiJsDisableOptimizations;
    }

    public void setYuiJsDisableOptimizations(boolean yuiJsDisableOptimizations) {
        this.yuiJsDisableOptimizations = yuiJsDisableOptimizations;
    }

    public int getYuiCssLineBreak() {
        return yuiCssLineBreak;
    }

    public void setYuiCssLineBreak(int yuiCssLineBreak) {
        this.yuiCssLineBreak = yuiCssLineBreak;
    }

    public boolean isUploadFiles() {
        return uploadFiles;
    }

    public void setUploadFiles(boolean uploadFiles) {
        this.uploadFiles = uploadFiles;
    }

    public File getS3SettingsFile() {
        return s3SettingsFile;
    }

    public void setS3SettingsFile(File s3SettingsFile) {
        this.s3SettingsFile = s3SettingsFile;
    }
}
