package com.polopoly.jenkins;

import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import javax.xml.parsers.ParserConfigurationException;

import hudson.maven.reporters.SurefireReport;
import hudson.tasks.junit.TestResult;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.CoreContainer;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

public class SolrDocumentBuilderTest {
    private static TestResult report;
    private SolrDocumentBuilder documentBuilder;
    private static SolrServer server;
    private Date buildDate;
    
    @BeforeClass
    public static void setupReport() throws IOException, ParserConfigurationException, SAXException, SolrServerException {
        report = new TestResult();
        report.parse(new File("src/test/resources/TEST-test.MyMainTest.xml"));
        
        SolrServerLookup solrLookup = new EmbeddedSolrServerLookup();
        server = solrLookup.get();
        server.deleteByQuery("*:*");
    }
    
    @Before
    public void init() throws ParseException, SolrServerException, IOException, ParserConfigurationException, SAXException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        buildDate = formatter.parse("2001-11-02");
        
        BuildData buildData = new BuildData("10-3_JBoss", "10-3","JBoss", "12344", buildDate);
        documentBuilder = new SolrDocumentBuilder(buildData, report);
    }

    /*
    @Test
    public void validateRestult() {
        assertEquals("Not correct number of tests", 2, report.getTotalCount());        
    }
    */
    
    @Test
    public void testSuite()
    {
        Collection<SolrInputDocument> docs = documentBuilder.getDocuments();
        assertEquals("Not correct number of documents", 2, docs.size());
    }
    
    @Test
    public void testOkCase() {
        Collection<SolrInputDocument> docs = documentBuilder.getDocuments();
        SolrInputDocument document = docs.iterator().next();
        assertNotNull("No document", document);
        assertEquals("Not correct project", "10-3_JBoss", document.getFieldValue("project")); 
        assertEquals("Not correct suite", "test.MyMainTest", document.getFieldValue("suite"));
        assertEquals("Not correct name", "test.MyMainTest.returnHelloReturnsHello", document.getFieldValue("test"));
        assertEquals("Not correct status", "PASSED", document.getFieldValue("status"));
        assertEquals("Not correct id", "10-3_JBoss.test.MyMainTest.returnHelloReturnsHello.12344", document.getFieldValue("id"));
        assertEquals("Not correct branch", "10-3", document.getFieldValue("branch"));
        assertEquals("Not correct plattform", "JBoss", document.getFieldValue("plattform"));
        assertEquals("Not correct year", 2001, document.getFieldValue("year"));
        assertEquals("Not correct month", 11, document.getFieldValue("month"));
        assertEquals("Not correct day", 2, document.getFieldValue("day"));
        assertEquals("Not correct datestamp", buildDate, document.getFieldValue("datestamp"));   

    }

    @Test
    public void testErrorCase() {
        Collection<SolrInputDocument> docs = documentBuilder.getDocuments();
        Iterator<SolrInputDocument> it = docs.iterator();
        it.next();
        SolrInputDocument document = it.next();
        assertEquals("Not correct id", "10-3_JBoss.test.MyMainTest.returnHelloReturnsPsycho.12344", document.getFieldValue("id"));
        assertEquals("Not correct name", "test.MyMainTest.returnHelloReturnsPsycho", document.getFieldValue("test"));
        assertEquals("Not correct status", "FAILED", document.getFieldValue("status"));
        assertEquals("Not correct ticket", 1234, document.getFieldValue("ticket"));
        assertThat("Wrong err message", (String)document.getFieldValue("error"), containsString("Not hello"));
        assertThat("Wrong stack message", (String)document.getFieldValue("stack"), containsString("Assert.java:123"));
        
    }
    
    @Test
    public void testIndex() throws SolrServerException, IOException {
        server.add(documentBuilder.getDocuments());
        server.commit();
        SolrQuery query = new SolrQuery();
        query.setQuery( "suite:test.MyMainTest" );
        QueryResponse rsp = server.query( query );
        SolrDocumentList docs = rsp.getResults();
        assertEquals("Not correct amount of tests", 2, docs.getNumFound());
    }

}
