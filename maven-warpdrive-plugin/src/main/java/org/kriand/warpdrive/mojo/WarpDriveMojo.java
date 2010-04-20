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
package org.kriand.warpdrive.mojo;

import org.kriand.warpdrive.Runtime;
import org.kriand.warpdrive.processors.AbstractProcessor;
import org.kriand.warpdrive.processors.bundles.BundleProcessor;
import org.kriand.warpdrive.processors.css.YuiCssProcessor;
import org.kriand.warpdrive.processors.images.DefaultImageProcessor;
import org.kriand.warpdrive.processors.js.YuiJsProcessor;
import org.kriand.warpdrive.processors.upload.ExternalUploadProcessor;
import org.kriand.warpdrive.processors.webxml.WebXmlProcessor;
import org.kriand.warpdrive.versioning.VersionProvider;
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
 * The WarpDrive Maven plugin performs all the work required at buildtime.
 * 
 * Created by IntelliJ IDEA.
 * @author kriand <a href="http://mailhide.recaptcha.net/d?k=01r9lbYEAtg9V5s1Ru_jtZ1g==&c=-aIoeZ0yU0yPn2kdog349bCmN-h1pe5Ed0LsyuWMbEc=">Show email</a>
 * Date: Mar 2, 2010
 * Time: 5:46:05 PM
 * @goal warpspeed
 * @phase prepare-package
 * @requiresProject
 * 
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
     * @parameter default-value=false
     */
    private boolean developmentMode;

    /**
     * @parameter default-value="${basedir}/src/main/webapp/WEB-INF/web.xml"
     */
    private String webXmlSource;

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
    private boolean generateWebXml;

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

    /**
     * @parameter
     */
    private String versionGeneratorClass;

    private String version;

    public final void execute() throws MojoExecutionException {
        try {
            assertWarModule();
            normalizeDirectories();
            version = new VersionProvider(this).getVersion();
            writeWarpDriveConfigFile();
            if (isDevelopmentMode()) {
                return;
            }
            printEyeCatcher();
            List<AbstractProcessor> processors = setupProcessors();
            for (AbstractProcessor processor : processors) {
                processor.process();
            }
        } catch (Exception ex) {
            throw new MojoExecutionException("Caught Exception", ex);
        }
    }

    public final String getVersion() {
        return version;
    }

    public final MavenProject getProject() {
        return project;
    }

    public final void setProject(final MavenProject inProject) {
        this.project = inProject;
    }

    public final String getWebappSourceDir() {
        return webappSourceDir;
    }

    public final void setWebappSourceDir(final String inWebappSourceDir) {
        this.webappSourceDir = inWebappSourceDir;
    }

    public final String getWebappTargetDir() {
        return webappTargetDir;
    }

    public final void setWebappTargetDir(final String inWebappTargetDir) {
        this.webappTargetDir = inWebappTargetDir;
    }

    public final boolean isDevelopmentMode() {
        return developmentMode;
    }

    public final void setDevelopmentMode(final boolean inDevelopmentMode) {
        this.developmentMode = inDevelopmentMode;
    }

    public final String getWebXmlSource() {
        return webXmlSource;
    }

    public final void setWebXmlSource(final String inWebXmlSource) {
        this.webXmlSource = inWebXmlSource;
    }

    public final String getJsDir() {
        return jsDir;
    }

    public final void setJsDir(final String inJsDir) {
        this.jsDir = inJsDir;
    }

    public final String getImageDir() {
        return imageDir;
    }

    public final void setImageDir(final String inImageDir) {
        this.imageDir = inImageDir;
    }

    public final String getCssDir() {
        return cssDir;
    }

    public final void setCssDir(final String inCssDir) {
        this.cssDir = inCssDir;
    }

    public final List<String> getExternalHosts() {
        return externalHosts;
    }

    public final void setExternalHosts(final List<String> inExternalHosts) {
        this.externalHosts = inExternalHosts;
    }

    public final Map<String, String> getJsBundles() {
        return jsBundles;
    }

    public final void setJsBundles(final Map<String, String> inJsBundles) {
        this.jsBundles = inJsBundles;
    }

    public final Map<String, String> getCssBundles() {
        return cssBundles;
    }

    public final void setCssBundles(final Map<String, String> inCssBundles) {
        this.cssBundles = inCssBundles;
    }

    public final boolean isGenerateWebXml() {
        return generateWebXml;
    }

    public final void setGenerateWebXml(final boolean inGenerateWebXml) {
        this.generateWebXml = inGenerateWebXml;
    }

    public final int getYuiJsLineBreak() {
        return yuiJsLineBreak;
    }

    public final void setYuiJsLineBreak(final int inYuiJsLineBreak) {
        this.yuiJsLineBreak = inYuiJsLineBreak;
    }

    public final boolean isYuiJsMunge() {
        return yuiJsMunge;
    }

    public final void setYuiJsMunge(final boolean inYuiJsMunge) {
        this.yuiJsMunge = inYuiJsMunge;
    }

    public final boolean isYuiJsVerbose() {
        return yuiJsVerbose;
    }

    public final void setYuiJsVerbose(final boolean inyuiJsVerbose) {
        this.yuiJsVerbose = inyuiJsVerbose;
    }

    public final boolean isYuiJsPreserveAllSemicolons() {
        return yuiJsPreserveAllSemicolons;
    }

    public final void setYuiJsPreserveAllSemicolons(final boolean inYuiJsPreserveAllSemicolons) {
        this.yuiJsPreserveAllSemicolons = inYuiJsPreserveAllSemicolons;
    }

    public final boolean isYuiJsDisableOptimizations() {
        return yuiJsDisableOptimizations;
    }

    public final void setYuiJsDisableOptimizations(final boolean inYuiJsDisableOptimizations) {
        this.yuiJsDisableOptimizations = inYuiJsDisableOptimizations;
    }

    public final int getYuiCssLineBreak() {
        return yuiCssLineBreak;
    }

    public final void setYuiCssLineBreak(final int inYuiCssLineBreak) {
        this.yuiCssLineBreak = inYuiCssLineBreak;
    }

    public final boolean isUploadFiles() {
        return uploadFiles;
    }

    public final void setUploadFiles(final boolean inUploadFiles) {
        this.uploadFiles = inUploadFiles;
    }

    public final File getS3SettingsFile() {
        return s3SettingsFile;
    }

    public final void setS3SettingsFile(final File inS3SettingsFile) {
        this.s3SettingsFile = inS3SettingsFile;
    }

    public final String getVersionGeneratorClass() {
        return versionGeneratorClass;
    }

    public final void setVersionGeneratorClass(final String inVersionGeneratorClass) {
        this.versionGeneratorClass = inVersionGeneratorClass;
    }

    private List<AbstractProcessor> setupProcessors() {
        List<AbstractProcessor> processors = new ArrayList<AbstractProcessor>();
        processors.add(new YuiJsProcessor(this));
        processors.add(new YuiCssProcessor(this));
        processors.add(new DefaultImageProcessor(this));
        if (bundlesAreConfigured()) {
            processors.add(new BundleProcessor(this));
        }
        if (isGenerateWebXml()) {
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
        if (!getCssDir().endsWith("/")) {
            setCssDir(getCssDir() + "/");
        }
        if (!getJsDir().endsWith("/")) {
            setJsDir(jsDir + "/");
        }
        if (!getImageDir().endsWith("/")) {
            setImageDir(getImageDir() + "/");
        }
        if (!getCssDir().startsWith("/")) {
            setCssDir("/" + getCssDir());
        }
        if (!getJsDir().startsWith("/")) {
            setJsDir("/" + getJsDir());
        }
        if (!getImageDir().startsWith("/")) {
            setImageDir("/" + imageDir);
        }
    }

    private void writeWarpDriveConfigFile() throws IOException {
        File file = new File(project.getBuild().getOutputDirectory(), Runtime.RUNTIME_CONFIG_FILE);

        boolean created = file.getParentFile().mkdirs();
        if (created) {
            getLog().info(String.format("Created directory: %s", file.getParentFile()));
        }

        FileWriter writer = null;
        getLog().info("Writing WarpDrive configfile to: " + file.getName());
        try {
            writer = new FileWriter(file);
            writeBooleanValue(Runtime.DEV_MODE_KEY, isDevelopmentMode(), writer);
            writeStringValue(org.kriand.warpdrive.Runtime.VERSION_KEY, version, writer);
            writeStringValue(Runtime.IMAGE_DIR_KEY, getImageDir(), writer);
            writeStringValue(Runtime.JS_DIR_KEY, getJsDir(), writer);
            writeStringValue(Runtime.CSS_DIR_KEY, getCssDir(), writer);
            writeExternalHostsConfig(writer);
            writeBundleConfig(getCssBundles(), writer);
            writeBundleConfig(getJsBundles(), writer);

        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private void writeStringValue(final String key, final String value, final Writer writer) throws IOException {
        writer.write(key);
        writer.write('=');
        writer.write(value);
        writer.write('\n');
    }

    private void writeBooleanValue(final String key, final boolean value, final Writer writer) throws IOException {
        writer.write(key);
        writer.write('=');
        writer.write(String.valueOf(value));
        writer.write('\n');
    }

    private void writeExternalHostsConfig(final Writer writer) throws IOException {
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

    private void writeBundleConfig(final Map<String, String> bundle, final Writer writer) throws IOException {
        if (bundle == null || bundle.isEmpty()) {
            return;
        }
        for (Map.Entry<String, String> entry : bundle.entrySet()) {
            writer.write(Runtime.BUNDLE_PREFIX_KEY);
            writer.write(entry.getKey());
            writer.write('=');
            String[] bundleEntries = entry.getValue().split(",");
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
