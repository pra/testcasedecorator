package com.polopoly.jenkins;

import javax.servlet.ServletContext;

import hudson.Plugin;
import hudson.util.PluginServletFilter;

/**
 * @plugin
 */
public class SolrPlugin extends Plugin {
    private SolrFilter filter;

    /** {@inheritDoc} */
    @Override
    public void setServletContext(ServletContext context) {
            super.setServletContext(context);
            System.err.println("DEBUG setServletContext " + context);
    }

    @Override
    public void start() throws Exception {
            super.start();

            this.filter = new SolrFilter();
            PluginServletFilter.addFilter(filter);
    }



    @SuppressWarnings("deprecation")
    @Override
    public void postInitialize() throws Exception {
            super.postInitialize();

    }

    @Override
    public void stop() throws Exception {
            super.stop();
    }
}
