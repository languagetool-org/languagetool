/*
 * Copyright © 2011 jbundle.org. All rights reserved.
 */
package org.languagetool.webstart;

/**
 * Start up the web service listener.
 * @author don
 */
public class HttpServiceActivator extends org.languagetool.osgi.HttpServiceActivator
{
    public String getServletClass()
    {
        return org.apache.catalina.servlets.DefaultServlet.class.getName();    // Override this to enable config admin.
    }
}
