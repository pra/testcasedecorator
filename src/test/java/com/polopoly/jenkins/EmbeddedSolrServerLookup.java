package com.polopoly.jenkins;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.xml.sax.SAXException;

public class EmbeddedSolrServerLookup extends SolrServerLookup {

    
    protected SolrServer setup() throws IOException, ParserConfigurationException, SAXException
    {
        System.setProperty("solr.solr.home", "src/test/resources/solr");
        CoreContainer.Initializer initializer = new CoreContainer.Initializer();
        CoreContainer coreContainer = initializer.initialize();
        
        File home = new File( "src/test/resources/solr" );
        File f = new File( home, "solr.xml" );
        CoreContainer container = new CoreContainer();
        //container.load( home.getAbsolutePath(), f );
        
        return new EmbeddedSolrServer(coreContainer, "collection1");
    }
    
    

}
