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
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.report.context.IContext;
import org.eclipse.birt.report.presentation.aggregation.IFragment;
import org.eclipse.birt.report.resource.BirtResources;
import org.eclipse.birt.report.utility.ParameterAccessor;

abstract public class BaseReportEngineServlet extends HttpServlet
{
	/**
	 * TODO: what's this?
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Versioning.
	 */
	protected static boolean openSource = true;

	/**
	 * Viewer fragment references.
	 */
	protected IFragment engine = null;

	/**
	 * Abstract methods.
	 */
	abstract protected void __init( ServletConfig config );

	abstract protected boolean __authenticate( HttpServletRequest request,
			HttpServletResponse response );

	abstract protected IContext __getContext( HttpServletRequest request,
			HttpServletResponse response );

	abstract protected void __handleNonSoapException( IContext context,
			Exception exception ) throws ServletException, IOException;

	/**
	 * Check version.
	 * 
	 * @return
	 */
	public static boolean isOpenSource( )
	{
		return openSource;
	}

	/**
	 * Servlet init.
	 * 
	 * @param config
	 * @exception ServletException
	 * @return
	 */
	public void init( ServletConfig config ) throws ServletException
	{
		super.init( config );
		ParameterAccessor.initParameters( config );
		BirtResources.initResource( ParameterAccessor.getWebAppLocale( ) );
		__init( config );
	}

	/**
	 * Handle HTTP GET method.
	 * 
	 * @param request
	 *            incoming http request
	 * @param response
	 *            http response
	 * @exception ServletException
	 * @exception IOException
	 * @return
	 */
	public void doGet( HttpServletRequest request, HttpServletResponse response )
			throws ServletException, IOException
	{
		if ( !__authenticate( request, response ) )
		{
			return;
		}
		
		IContext context = __getContext( request, response );

		if ( context.getBean( ).getException( ) != null )
		{
			__handleNonSoapException( context, context.getBean( )
					.getException( ) );
		}
		else
		{
			try
			{
				engine.service( context.getRequest( ), context.getResponse( ) );
			}
			catch ( BirtException e )
			{
				__handleNonSoapException( context, e );
			}
		}
	}

	/**
	 * Handle HTTP POST method.
	 * 
	 * @param request
	 *            incoming http request
	 * @param response
	 *            http response
	 * @exception ServletException
	 * @exception IOException
	 * @return
	 */
	public void doPost( HttpServletRequest request, HttpServletResponse response )
			throws ServletException, IOException
	{
		doGet( request, response );
	}
}
