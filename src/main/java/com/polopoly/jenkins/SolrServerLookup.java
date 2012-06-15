package com.polopoly.jenkins;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.solr.client.solrj.SolrServer;
import org.xml.sax.SAXException;

public abstract class SolrServerLookup {
    private transient SolrServer server;

    public synchronized SolrServer get() throws IOException,
        ParserConfigurationException, SAXException
    {
       if(server == null) {
           server = setup();
       }
       return server;
    }

    abstract protected SolrServer setup() throws IOException, ParserConfigurationException, SAXException;

}