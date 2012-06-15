package com.polopoly.jenkins;

import java.util.Date;

public class BuildData {
    private String projectName;
    private String buildId;
    private String branch;
    private String plattform;
    private Date buildDate;

    public BuildData(String projectName,
        String branch,
        String plattform,
        String buildId,
        Date buildDate)
    {
        this.projectName = projectName;
        this.buildId = buildId;
        this.branch = branch;
        this.plattform = plattform;
        this.buildDate = buildDate;
    }


    public String getProjectName()
    {
        return projectName;
    }


    public String getBuildId()
    {
        return buildId;
    }


    public String getBranch()
    {
        return branch;
    }


    public String getPlattform()
    {
        return plattform;
    }
   
    public Date getBuildDate()
    {
        return buildDate;
    }
}
