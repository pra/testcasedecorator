package com.polopoly.jenkins;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hudson.tasks.junit.CaseResult;
import hudson.tasks.junit.SuiteResult;
import hudson.tasks.junit.TestResult;

import org.apache.solr.common.SolrInputDocument;

/**
 * Builds the solr document from test and build data.
 * 
 * Needs the following solr schema:
 * @author pra
 *
 */
public class SolrDocumentBuilder {
    private TestResult report;
    private Collection<SuiteResult> suites;
    private BuildData buildData;
    private Pattern findTicketPattern;
    
    
    public SolrDocumentBuilder(BuildData buildData, TestResult report)
    {
        this.report = report;
        this.buildData = buildData;
        this.suites = report.getSuites();
        this.findTicketPattern = Pattern.compile("#(\\d+)#");
    }

    public Collection<SolrInputDocument> getDocuments()
    {
        Collection<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
        
        for (SuiteResult sr : report.getSuites()) {
            for (CaseResult cr : sr.getCases()) {
                
                SolrInputDocument doc = new SolrInputDocument();
                
                doc.addField("id", buildData.getProjectName() + "." + 
                            cr.getFullName() + "." +
                            buildData.getBuildId());
                doc.addField("project", buildData.getProjectName());
                doc.addField("suite", sr.getName());
                doc.addField("test", cr.getFullName());
                doc.addField("branch", buildData.getBranch());
                doc.addField("plattform", buildData.getPlattform());
                
                doc.addField("datestamp", buildData.getBuildDate());
                Calendar cal = Calendar.getInstance();
                cal.setTime(buildData.getBuildDate());
                doc.addField("year", cal.get(Calendar.YEAR));
                doc.addField("month", cal.get(Calendar.MONTH) + 1);
                doc.addField("day", cal.get(Calendar.DAY_OF_MONTH));

                if(!cr.isPassed()) {
                    String m = cr.getErrorDetails();
                    
                    doc.addField("error", m);
                    doc.addField("stack", cr.getErrorStackTrace());
                    
                    Matcher matcher = findTicketPattern.matcher(m);
                    if(matcher.find()) {
                        String t = matcher.group(1);
                        if(t != null) {
                            doc.addField("ticket", Integer.valueOf(t).intValue());
                        }
                    }
                }
                
                doc.addField("status", cr.isPassed() ? "PASSED" : "FAILED");
                docs.add(doc);
            }
        }
        
        return docs;
        
    }

}
