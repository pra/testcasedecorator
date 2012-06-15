package com.polopoly.jenkins;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public class SolrFilter implements Filter {
    /** {@inheritDoc} */
    public void init(FilterConfig config) throws ServletException {
        System.err.println("DEBUG filterInit " + config);
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                    throws IOException, ServletException {
        final HttpServletRequest httpRequest = (HttpServletRequest) request;

        final String requestURI = httpRequest.getRequestURI();
        //System.err.println("DEBUG doFilter " + requestURI);
        chain.doFilter(request, response);
    }

    public void destroy()
    {
        // TODO Auto-generated method stub
        
    }
}
