package com.polopoly.jenkins;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.xml.sax.SAXException;

import hudson.Extension;
import hudson.Launcher;
import hudson.maven.MavenBuild;
import hudson.maven.MavenModule;
import hudson.maven.MavenModuleSet;
import hudson.maven.MavenModuleSetBuild;
import hudson.maven.reporters.SurefireReport;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Saveable;
import hudson.model.Descriptor.FormException;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.tasks.junit.SuiteResult;
import hudson.tasks.junit.TestDataPublisher;
import hudson.tasks.junit.TestResult;
import hudson.tasks.junit.CaseResult;
import hudson.util.DescribableList;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.common.SolrInputDocument;

public class TestReportIndexer extends Recorder {
    private final SolrServerLookup serverLookup;
    private final String branch;
    private final String plattform;
    
    public TestReportIndexer(SolrServerLookup serverLookup, String branch, String plattform) throws MalformedURLException  {
        this.serverLookup = serverLookup;
        this.branch = "".equals(branch.trim()) ? null : branch.trim();
        this.plattform = "".equals(plattform.trim()) ? null : plattform.trim();        
    }
    
    @DataBoundConstructor
    public TestReportIndexer(String branch, String plattform) throws MalformedURLException  {

        this(new CommonsHttpSolrServerLookup("http://localhost:8983/solr/"), branch, plattform);
    }
    
    public String getBranch()
    {
        return branch;
    }

    public String getPlattform()
    {
        return plattform;
    }

    public BuildStepMonitor getRequiredMonitorService()
    {
        return BuildStepMonitor.STEP;
    }
    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
            try {
                indexModules(build, launcher, listener);
                indexTestReport(build, launcher, listener);
            } catch (SolrServerException e) {
                throw new IOException(e);
            } catch (ParserConfigurationException e) {
                throw new IOException(e);

            } catch (SAXException e) {
                throw new IOException(e);
            }
            return true;
    }
    private void indexModules(AbstractBuild build, Launcher launcher,
        BuildListener listener) throws IOException, SolrServerException, ParserConfigurationException, SAXException
    {
        if (build instanceof MavenModuleSetBuild) {
            Map<MavenModule, List<MavenBuild>> buildsMap = ((MavenModuleSetBuild) build).getModuleBuilds();
            for (List<MavenBuild> mavenBuild : buildsMap.values()) {
                    MavenBuild lastBuild = getLastOrNullIfEmpty(mavenBuild);
                    if (lastBuild != null) {
                            
                        indexTestReport(lastBuild, launcher, listener);
                    }
            }
    }        
    }
    private void indexTestReport(AbstractBuild build, Launcher launcher,
        BuildListener listener) throws IOException, SolrServerException, ParserConfigurationException, SAXException
    {
        SurefireReport report = build.getAction(SurefireReport.class);
       
        if (report != null) {
            BuildData buildData = new BuildData(build.getProject().getDisplayName(),
                                                getBranch(), getPlattform(),build.getId(), build.getTime());

            indexReport(buildData, report.getResult());

        }
    }

    
    private void indexReport(BuildData buildData, TestResult report) throws SolrServerException, IOException, ParserConfigurationException, SAXException {
        if (report == null) {
            print("DEBUG no result");
            return;
        }
        SolrDocumentBuilder docBuilder = new SolrDocumentBuilder(buildData, report);
        Collection<SolrInputDocument> docs = docBuilder.getDocuments();
        serverLookup.get().add(docs);
        serverLookup.get().commit();
  
    }
    
    private void print(String m) {
        System.err.println("DEBUG " + m);
    }
    
    public static MavenBuild getLastOrNullIfEmpty(List<MavenBuild> builds) {
        if (builds.isEmpty()) {
                return null;
        } else {
                return builds.get(builds.size() - 1);
        }
    }
    
    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

            public String findBranch(String projectName) {
                String b = null;
                if (projectName != null) {
                    int i = projectName.indexOf("_");
                    if (i != -1) {
                        b = projectName.substring(0, i);
                    }
                }
                return b;
            }

            public String findPlattform(String projectName) {
                String p = null;
                if (projectName != null) {
                    int i = projectName.indexOf("_");
                    if (i != -1 && projectName.length() > i) {
                        p = projectName.substring(i + 1);
                    }
                }
                return p;
            }
            
            @Override
            public String getDisplayName() {
                    return "Index test reports";
            }

            @Override
            public boolean isApplicable(Class<? extends AbstractProject> jobType) {
                    return MavenModuleSet.class.isAssignableFrom(jobType);
            }

            @Override
            public Publisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
                return req.bindJSON(TestReportIndexer.class,formData);
            }
    }

}
