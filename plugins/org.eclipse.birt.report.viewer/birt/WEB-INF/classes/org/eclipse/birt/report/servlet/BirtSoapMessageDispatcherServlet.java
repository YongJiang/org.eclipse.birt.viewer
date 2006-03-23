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

package org.eclipse.birt.report.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.axis.transport.http.AxisServlet;
import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.report.service.ReportEngineService;

public class BirtSoapMessageDispatcherServlet extends AxisServlet
{

	private static ReportEngineService reportEngineService = null;

	public void init( ServletConfig config ) throws ServletException
	{
		super.init( config );
		try
		{
			InitReportEngineService( config );
		}
		catch ( BirtException e )
		{
			throw new ServletException( );
		}
	}

	private void InitReportEngineService( ServletConfig config )
			throws BirtException
	{
		if ( reportEngineService != null )
		{
			return;
		}

		synchronized ( this )
		{
			if ( reportEngineService != null )
			{
				return;
			}
			ReportEngineService.initEngineInstance( config );
			reportEngineService = ReportEngineService.getInstance( );
		}
	}

	public void doGet( HttpServletRequest request, HttpServletResponse response )
			throws ServletException, IOException
	{
		super.doGet( request, response );
	}

	public void doPost( HttpServletRequest request, HttpServletResponse response )
			throws ServletException, IOException
	{
		super.doPost( request, response );
	}
}
