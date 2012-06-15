package com.polopoly.jenkins;

import hudson.Extension;
import hudson.model.RootAction;

@Extension
public class SolrAction implements RootAction {

    public String getIconFileName()
    {
        return "search.gif";
    }

    public String getDisplayName()
    {
        return "Search Test Results";
    }

    public String getUrlName()
    {
        return "/solr";
    }

}
