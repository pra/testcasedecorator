package com.polopoly.jenkins;

import hudson.Extension;
import hudson.Launcher;
import hudson.maven.MavenBuild;
import hudson.maven.MavenModule;
import hudson.maven.MavenModuleSet;
import hudson.maven.MavenModuleSetBuild;
import hudson.maven.reporters.SurefireReport;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Saveable;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.tasks.junit.TestDataPublisher;
import hudson.tasks.junit.TestResultAction;
import hudson.tasks.junit.TestResultAction.Data;
import hudson.util.DescribableList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

public class MavenTestDataPublisherFix extends Recorder {

        private final DescribableList<TestDataPublisher, Descriptor<TestDataPublisher>> testDataPublishers;

        public MavenTestDataPublisherFix(DescribableList<TestDataPublisher, Descriptor<TestDataPublisher>> testDataPublishers) {
                super();
                this.testDataPublishers = testDataPublishers;
        }

        
        public BuildStepMonitor getRequiredMonitorService() {
                return BuildStepMonitor.STEP;
        }

        @Override
        public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {

                addTestDataPublishersToMavenModules(build, launcher, listener);
                addTestDataPublishersToBuildReport(build, launcher, listener);                
                return true;
        }

        private void addTestDataPublishersToMavenModules(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException,
                        InterruptedException {
                if (build instanceof MavenModuleSetBuild) {
                        Map<MavenModule, List<MavenBuild>> buildsMap = ((MavenModuleSetBuild) build).getModuleBuilds();
                        for (List<MavenBuild> mavenBuild : buildsMap.values()) {
                                MavenBuild lastBuild = RecorderUtils.getLastOrNullIfEmpty(mavenBuild);
                                if (lastBuild != null) {
                                        addTestDataPublishersToBuildReport(lastBuild, launcher, listener);
                                }
                        }
                }
        }

        private void addTestDataPublishersToBuildReport(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException,
                        InterruptedException {
                //SurefireReport report = build.getAction(SurefireReport.class);
                TestResultAction report = build.getAction(TestResultAction.class);
                if (report != null) {
                        List<Data> data = new ArrayList<Data>();
                        if (testDataPublishers != null) {
                                for (TestDataPublisher tdp : testDataPublishers) {
                                        Data d = tdp.getTestData(build, launcher, listener, report.getResult());
                                        if (d != null) {
                                                data.add(d);
                                        }
                                }
                        }

                        report.setData(data);
                        build.save();
                }
        }

 

        public DescribableList<TestDataPublisher, Descriptor<TestDataPublisher>> getTestDataPublishers() {
                return testDataPublishers;
        }

        @Extension
        public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

                @Override
                public String getDisplayName() {
                        return "Additional test report features (FIXED)";
                }

                @Override
                public boolean isApplicable(Class<? extends AbstractProject> jobType) {
                        return MavenModuleSet.class.isAssignableFrom(jobType) && !TestDataPublisher.all().isEmpty();
                }

                @Override
                public Publisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
                        DescribableList<TestDataPublisher, Descriptor<TestDataPublisher>> testDataPublishers =
                                        new DescribableList<TestDataPublisher, Descriptor<TestDataPublisher>>(Saveable.NOOP);
                        try {
                                testDataPublishers.rebuild(req, formData, TestDataPublisher.all());
                        }
                        catch (IOException e) {
                                throw new FormException(e, null);
                        }
                        return new MavenTestDataPublisherFix(testDataPublishers);
                }
        }
}