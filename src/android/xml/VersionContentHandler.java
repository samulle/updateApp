package com.samulle.plugin.update.xml;


import com.samulle.plugin.update.version.Version;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * version的xml解析
 */

public class VersionContentHandler extends DefaultHandler {

    private String tagName = null;
    private Version version = null;

    public VersionContentHandler() {
        super();
    }

    public Version getVersion() {
        return version;
    }

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        this.tagName = localName;
        if("update".equals(tagName)) {
            version = new Version();
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        tagName = "";
        super.endElement(uri, localName, qName);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        String temp = new String(ch, start, length);
        if("version".equals(tagName)) {
            version.setVersionCode(temp);
        }
        else if("name".equals(tagName)) {
            version.setApkName(temp);
        }
        else if("url".equals(tagName)) {
            version.setUrl(temp);
        }
        else if("content".equals(tagName)) {
            version.setMsg(temp);
        }

    }
}
