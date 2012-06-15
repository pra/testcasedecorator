package com.polopoly.jenkins;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.xml.sax.SAXException;

public class CommonsHttpSolrServerLookup extends SolrServerLookup {
    private final String url;
    
    public CommonsHttpSolrServerLookup(String url) {
        this.url = url;
    }
    
    @Override
    protected SolrServer setup() throws IOException,
        ParserConfigurationException, SAXException
    {
        return new CommonsHttpSolrServer(url);
    }

}
