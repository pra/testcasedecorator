package com.polopoly.jenkins;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Saveable;
import hudson.tasks.junit.CaseResult;
import hudson.tasks.junit.TestAction;
import hudson.tasks.junit.TestDataPublisher;
import hudson.tasks.junit.TestObject;
import hudson.tasks.junit.TestResult;
import hudson.tasks.junit.TestResultAction.Data;

public class KnownBugAnnotator extends TestDataPublisher {
    @DataBoundConstructor 
    public KnownBugAnnotator() {
        System.err.println("DEBUG construct");
    }
    
    @Extension
    public static final class DescriptorImpl extends Descriptor<TestDataPublisher> {
            @Override
            public String getDisplayName() {
                    return "Add Known Bug links links to failing JUnit tests";
            }
    }
    
    @Override
    public Data getTestData(final AbstractBuild<?, ?> build, final Launcher launcher, 
                            final BuildListener listener, final TestResult testResult) 
    throws IOException,InterruptedException {
        return new KnownBugData(build);
    }
    
    public static class KnownBugData extends Data implements Saveable {
        private final AbstractBuild<?,?> build;
        
        public KnownBugData(AbstractBuild<?,?> build) {
            this.build = build;
        }
        
        @Override
        public List<? extends TestAction> getTestAction(TestObject testObject) {
            if(testObject instanceof CaseResult && !((CaseResult)testObject).isPassed()) {
                return Arrays.asList(new KnowBugAnnotatorAction((CaseResult)testObject));
            }
            else {
                return Collections.EMPTY_LIST;
            }
        }

        public void save() throws IOException
        {
            System.err.println("DEBUG saving");
            build.save();
        }
    }
    
}
