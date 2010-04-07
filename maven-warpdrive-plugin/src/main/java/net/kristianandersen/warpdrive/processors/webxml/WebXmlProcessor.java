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
package net.kristianandersen.warpdrive.processors.webxml;

import net.kristianandersen.warpdrive.filter.WarpDriveFilter;
import net.kristianandersen.warpdrive.mojo.WarpDriveMojo;
import net.kristianandersen.warpdrive.processors.AbstractProcessor;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: kriand
 * Date: Mar 3, 2010
 * Time: 9:30:46 PM
 */
public class WebXmlProcessor extends AbstractProcessor {

    private final static String FILTER_NAME = WarpDriveFilter.class.getName();

    public WebXmlProcessor(int priority, WarpDriveMojo mojo) {
        super(priority, mojo);
    }

    public void process() throws Exception {
        log().info("Processing web.xml found here: " + mojo.webXml);
        File webXml = new File(mojo.webXml);
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(webXml);
        configureWarpDriveFilter(doc);
        XMLOutputter output = new XMLOutputter(Format.getPrettyFormat());
        output.output(doc, new FileOutputStream(webXml));
        log().info("web.xml processed OK");
    }

    private void configureWarpDriveFilter(Document doc) throws JDOMException {
        if (mojo.externalHosts != null && mojo.externalHosts.size() > 0) {
            mojo.getLog().warn("Configuring filter even though external hosts are defined. You probably want to set the configureFilter option to false");
        }
        Element root = doc.getRootElement();
        log().debug("Removing old filter-config");
        removeFilterDefinitions(doc, root);
        log().debug("Adding new filter-config");
        addFilterDefinitions(root);
    }

    private void addFilterDefinitions(Element root) {
        Element filterDef = new Element("filter", root.getNamespace());
        Element filterName = new Element("filter-name", root.getNamespace());
        filterName.setText(FILTER_NAME);

        Element filterClass = new Element("filter-class", root.getNamespace());
        filterClass.setText(WarpDriveFilter.class.getName());

        filterDef.addContent(filterName).addContent(filterClass);

        Element filterMappingJs = new Element("filter-mapping", root.getNamespace());
        Element filterMappingJsUrl = new Element("url-pattern", root.getNamespace());
        filterMappingJsUrl.setText(mojo.jsDir + "*");
        filterMappingJs.addContent((Element) filterName.clone()).addContent(filterMappingJsUrl);

        Element filterMappingCss = new Element("filter-mapping", root.getNamespace());
        Element filterMappingCssUrl = new Element("url-pattern", root.getNamespace());
        filterMappingCssUrl.setText(mojo.cssDir + "*");
        filterMappingCss.addContent((Element) filterName.clone()).addContent(filterMappingCssUrl);

        Element filterMappingImages = new Element("filter-mapping", root.getNamespace());
        Element filterMappingImagesUrl = new Element("url-pattern", root.getNamespace());
        filterMappingImagesUrl.setText(mojo.imageDir + "*");
        filterMappingImages.addContent((Element) filterName.clone()).addContent(filterMappingImagesUrl);

        root.addContent(filterDef).addContent(filterMappingJs).addContent(filterMappingCss).addContent(filterMappingImages);
    }

    private void removeFilterDefinitions(Document doc, Element root) throws JDOMException {
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
