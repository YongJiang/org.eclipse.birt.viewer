/*************************************************************************************
 * Copyright (c) 2004 Actuate Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Actuate Corporation - Initial implementation.
 ************************************************************************************/

package org.eclipse.birt.report.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.service.ReportEngineService;
import org.eclipse.birt.report.utility.ParameterAccessor;

/**
 * Servlet Context Listener for BIRT viewer web application. Do some necessary
 * jobs when web application servelt loading it or destroying it.
 * <p>
 */
public class ViewerServletContextListener implements ServletContextListener
{

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */
	public void contextDestroyed( ServletContextEvent event )
	{
		// When trying to destroy application, shutdown Platform and ReportEngineService.
		//Platform.shutdown( );
		//ReportEngineService.shutdown( );

		// Reset initialized parameter
		ParameterAccessor.reset( );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	public void contextInitialized( ServletContextEvent event )
	{
		ParameterAccessor.initParameters( event.getServletContext( ) );
	}
}
