package com.polopoly.jenkins;

import hudson.tasks.junit.CaseResult;
import hudson.tasks.junit.TestAction;
import hudson.tasks.junit.TestObject;
import hudson.tasks.junit.TestResult;

public class KnowBugAnnotatorAction extends TestAction {
    private final static String SOLR_QUERY= "http://prodtest03:8983/solr/select/?" + "start=0&rows=100&indent=on&" +
    		"q=-status:OK+AND+test:";
    
    private CaseResult testObject;

    public KnowBugAnnotatorAction(CaseResult testObject) {
        this.testObject = testObject;
        System.err.println("DEBUG KnownBugs "  + this  + " " + testObject);
    }

    public String getIconFileName() {
        return null;
    }

    public String getDisplayName() {
        return "Know bug test result";
    }

    public String getUrlName() {
        return "known-bugs";
    }

    @Override
    public String annotate(String testName) {
        System.err.println("DEBUG annotating " + testName);
        return SOLR_QUERY + testName;
        //return "KnownBugs " + testObject + ": " + testResult;
    }
}
