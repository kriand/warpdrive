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
package net.kristianandersen.warpdrive.filter;

import net.kristianandersen.warpdrive.mojo.WarpDriveMojo;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: kriand
 * Date: Mar 3, 2010
 * Time: 9:30:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class FilterConfigurator {

    private final static String FILTER_NAME = WarpDriveFilter.class.getName();

    private WarpDriveMojo mojo;

    public FilterConfigurator(WarpDriveMojo mojo) {
        this.mojo = mojo;
    }

    public void configureWebXml() throws JDOMException, IOException {
        File webXml = new File(mojo.webXml);
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(webXml);
        configureWarpDriveFilter(doc);
        configureMimeMappings(doc);
        XMLOutputter output = new XMLOutputter(Format.getPrettyFormat());
        output.output(doc, new FileOutputStream(webXml));
    }

    private void configureWarpDriveFilter(Document doc) throws JDOMException, IOException {

        Element root = doc.getRootElement();
        removeFilterDefinitions(doc, root);
        if (mojo.configureFilter) {
            if(mojo.externalHosts != null && mojo.externalHosts.size() > 0) {
                mojo.getLog().warn("Configuring filter even though external hosts are defined. You probably want to set the configureFilter option to false");                
            }
            addFilterDefinitions(root);
        }

    }

    private void configureMimeMappings(Document doc) throws JDOMException, IOException {

        Element root = doc.getRootElement();
        removeMimeMapping("css.gz", doc, root);
        removeMimeMapping("js.gz", doc, root);
        if (mojo.configureFilter) {
            if(mojo.externalHosts != null && mojo.externalHosts.size() > 0) {
                mojo.getLog().warn("Configuring mime-mappings even though external hosts are defined. You probably want to set the configureFilter option to false");
            }
            addMimeMapping(root, "css.gz", "text/css");
            addMimeMapping(root, "js.gz", "text/javasscript");
        }

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

    private void addMimeMapping(Element root, String ext, String mType) {
        Element mimeMapping = new Element("mime-mapping", root.getNamespace());
        Element extension = new Element("extension", root.getNamespace());
        extension.setText(ext);
        Element mimeType = new Element("mime-type", root.getNamespace());
        mimeType.setText(mType);
        mimeMapping.addContent(extension).addContent(mimeType);        
        root.addContent(mimeMapping);
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

    private void removeMimeMapping(String extension, Document doc, Element root) throws JDOMException {
        XPath xpath = XPath.newInstance("//ns:extension");
        xpath.addNamespace("ns", root.getNamespaceURI());
        List<Element> mimetypeExtensionElements = xpath.selectNodes(doc);
        for (Element e : mimetypeExtensionElements) {
            if (extension.equals(e.getText())) {
                e.getParentElement().detach();
            }
        }
    }

}
