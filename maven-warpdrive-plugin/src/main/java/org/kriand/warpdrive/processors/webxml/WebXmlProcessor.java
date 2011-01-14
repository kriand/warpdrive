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
package org.kriand.warpdrive.processors.webxml;

import org.kriand.warpdrive.filter.WarpDriveFilter;
import org.kriand.warpdrive.mojo.WarpDriveMojo;
import org.kriand.warpdrive.processors.AbstractProcessor;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

/**
 *
 * User: kriand
 * Date: Mar 3, 2010
 * Time: 9:30:46 PM
 */
public class WebXmlProcessor extends AbstractProcessor {

    /**
     *
     */
    private static final String FILTER_NAME = WarpDriveFilter.class.getName();

    /**
     * @param mojo
     */
    public WebXmlProcessor(final WarpDriveMojo mojo) {
        super(mojo);
    }

    /**
     * @throws Exception
     */
    public final void process() throws Exception {
        getLog().info("Processing web.xml found here: " + getMojo().getWebXmlSource());
        File webXmlSource = new File(getMojo().getWebXmlSource());
        File webXmlTarget = new File(getMojo().getProject().getBuild().getDirectory(), "warpdrive-web.xml");
        boolean created = webXmlTarget.getParentFile().mkdirs();
        if (created) {
            getLog().info(String.format("Created directoty %s", webXmlTarget.getParentFile()));
        }
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(webXmlSource);
        configureWarpDriveFilter(doc);
        XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
        OutputStream os = new FileOutputStream(webXmlTarget);
        try {
            out.output(doc, os);
        } finally {
            IOUtils.closeQuietly(os);
        }
    }

    /**
     * @param doc
     * @throws JDOMException
     */
    private void configureWarpDriveFilter(final Document doc) throws JDOMException {
        if (getMojo().getExternalHosts() != null && getMojo().getExternalHosts().size() > 0) {
            getMojo().getLog().warn("Configuring filter even though external hosts are defined. You probably want to set the configureFilter option to false");
        }
        Element root = doc.getRootElement();
        getLog().debug("Removing old filter-config");
        removeFilterDefinitions(doc, root);
        getLog().debug("Adding new filter-config");
        addFilterDefinitions(root);
    }

    /**
     * @param root
     */
    private void addFilterDefinitions(final Element root) {
        Element filterDef = new Element("filter", root.getNamespace());
        Element filterName = new Element("filter-name", root.getNamespace());
        filterName.setText(FILTER_NAME);

        Element filterClass = new Element("filter-class", root.getNamespace());
        filterClass.setText(WarpDriveFilter.class.getName());

        filterDef.addContent(filterName).addContent(filterClass);

        Element filterMappingJs = new Element("filter-mapping", root.getNamespace());
        Element filterMappingJsUrl = new Element("url-pattern", root.getNamespace());
        filterMappingJsUrl.setText(getMojo().getJsDir() + "*");
        filterMappingJs.addContent((Element) filterName.clone()).addContent(filterMappingJsUrl);

        Element filterMappingCss = new Element("filter-mapping", root.getNamespace());
        Element filterMappingCssUrl = new Element("url-pattern", root.getNamespace());
        filterMappingCssUrl.setText(getMojo().getCssDir() + "*");
        filterMappingCss.addContent((Element) filterName.clone()).addContent(filterMappingCssUrl);

        Element filterMappingImages = new Element("filter-mapping", root.getNamespace());
        Element filterMappingImagesUrl = new Element("url-pattern", root.getNamespace());
        filterMappingImagesUrl.setText(getMojo().getImageDir() + "*");
        filterMappingImages.addContent((Element) filterName.clone()).addContent(filterMappingImagesUrl);

        root.addContent(filterDef).addContent(filterMappingJs).addContent(filterMappingCss).addContent(filterMappingImages);
    }

    /**
     * @param doc
     * @param root
     * @throws JDOMException
     */
    private void removeFilterDefinitions(final Document doc, final Element root) throws JDOMException {
        XPath xpath = XPath.newInstance("//ns:filter-name");
        xpath.addNamespace("ns", root.getNamespaceURI());
        List<Element> warpDriveFilterElements = xpath.selectNodes(doc);
        for (Element e : warpDriveFilterElements) {
            if (FILTER_NAME.equals(e.getText())) {
                e.getParentElement().detach();
            }
        }
    }
}
