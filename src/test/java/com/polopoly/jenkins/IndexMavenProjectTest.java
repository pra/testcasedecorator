package com.polopoly.jenkins;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Map;

import hudson.maven.MavenBuild;
import hudson.maven.MavenModule;
import hudson.maven.MavenModuleSet;
import hudson.maven.MavenModuleSetBuild;
import hudson.maven.reporters.SurefireReport;
import hudson.maven.reporters.SurefireAggregatedReport;
import hudson.model.Result;
import hudson.tasks.Maven.MavenInstallation;
import hudson.tasks.junit.CaseResult;
import hudson.tasks.junit.TestResult;

import org.codehaus.plexus.util.FileUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.core.CoreContainer;


public class IndexMavenProjectTest extends HudsonTestCase
{
    SolrServerLookup serverLookup;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        serverLookup = new EmbeddedSolrServerLookup();
    }
       
    @Test
    public void test() throws Exception
    {
//        File solr = createTmpDir();
//        FileUtils.copyFileToDirectory("src/test/resources/solr/solr.xml", solr.getAbsolutePath());
        SolrServer server = serverLookup.get();
        serverLookup.get().deleteByQuery("*:*");
        
        //File root = jenkins.root;
        File pom = new File("src/test/testproject/pom.xml");
        //File pom = new File(this.getClass().getResource("testproject/pom.xml").toURI());

//        configureDefaultMaven();
        MavenModuleSet m = createMavenProject("10-3_JBoss");
        MavenInstallation mavenInstallation = configureMaven3();
        m.setMaven( mavenInstallation.getName() );

        
        m.setRootPOM(pom.getAbsolutePath());
        m.setGoals("clean test");
        TestReportIndexer indexer = new TestReportIndexer(serverLookup, "10-3", "JBoss");
        m.getPublishersList().add(indexer);

        MavenModule mm = m.getModule("test:test");
        assertNotNull("No maven test:test module" + mm);
        
        // XXX Can't find a good way to get this stable. Unstable, not
        MavenModuleSetBuild b = assertBuildStatus(Result.UNSTABLE, m.scheduleBuild2(0).get());
        
        Map<MavenModule, MavenBuild> builds = b.getModuleLastBuilds();
        MavenBuild modBuild = builds.values().iterator().next();
        
        TestResult tr = modBuild.getAction(SurefireReport.class).getResult();
        assertEquals(1,tr.getFailedTests().size());
        CaseResult cr = tr.getFailedTests().get(0);
        assertEquals("test.MyMainTest",cr.getClassName());
        assertEquals("returnHelloReturnsPsycho",cr.getName());
        assertNotNull("Error details should not be null", cr.getErrorDetails());
        assertNotNull("Error stacktrace should not be null", cr.getErrorStackTrace());

        SolrQuery query = new SolrQuery();
        query.setQuery( "suite:test.MyMainTest" );
        QueryResponse rsp = serverLookup.get().query( query );
        SolrDocumentList docs = rsp.getResults();
        assertEquals("Not correct number of results", 2, docs.getNumFound());
    }

}
