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

package org.eclipse.birt.report.context;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.birt.report.engine.api.IReportDocument;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.ReportParameterConverter;
import org.eclipse.birt.report.service.BirtViewerReportDesignHandle;
import org.eclipse.birt.report.service.ReportEngineService;
import org.eclipse.birt.report.service.api.ParameterDefinition;
import org.eclipse.birt.report.service.api.IViewerReportDesignHandle;
import org.eclipse.birt.report.service.api.IViewerReportService;
import org.eclipse.birt.report.service.api.InputOptions;
import org.eclipse.birt.report.service.api.ReportServiceException;
import org.eclipse.birt.report.utility.ParameterAccessor;

/**
 * Data bean for viewing request. Birt viewer distributes process logic into
 * viewer fragments. Each fragment seperates its front-end and back-end process
 * into jsp page and "code behand" fragment class. Viewer attribute bean serves
 * as:
 * <ol>
 * <li> object that carries the data shared among different fragments</li>
 * <li> object that carries the date shared between front-end jsp page and
 * back-end class</li>
 * </ol>
 * In current implementation, ViewerAttributeBean uses request scope.
 * <p>
 */
abstract public class BaseAttributeBean
{

	/**
	 * Identify the incoming request category.
	 */
	protected String category;

	/**
	 * Need to store the exception.
	 */
	protected Exception exception = null;

	/**
	 * Get report parameters passed in by URL.
	 */
	protected HashMap parameters = null;

	/**
	 * Whether missing parameters.
	 */
	protected boolean missingParameter = false;

	/**
	 * scalar parameter bean.
	 */
	protected ParameterAttributeBean parameterBean = null;

	/**
	 * Report design name.
	 */
	protected String reportDesignName = null;

	/**
	 * Report document name.
	 */
	protected String reportDocumentName = null;

	/**
	 * Report title.
	 */
	protected String reportTitle = null;

	/**
	 * Report page number.
	 */
	protected String reportPage = null;

	/**
	 * Current locale.
	 */
	protected Locale locale = null;

	/**
	 * Enable master page content.
	 */
	protected boolean masterPageContent = true;

	/**
	 * In designer context.
	 */
	protected boolean isDesigner = false;

	/**
	 * Bookmark.
	 */
	protected String bookmark = null;

	/**
	 * Report format of the request.
	 */
	
	protected String format = ParameterAccessor.PARAM_FORMAT_HTML;

	/**
	 * Abstract methods.
	 */
	abstract protected void __init( HttpServletRequest request )
			throws Exception;

	abstract protected IViewerReportService getReportService( );

	abstract protected void __finalize( ) throws Throwable;

	/**
	 * Default constructor.
	 */
	public BaseAttributeBean( )
	{
	}

	/**
	 * Constructor.
	 */
	public BaseAttributeBean( HttpServletRequest request )
	{
		try
		{
			init( request );
		}
		catch ( Exception e )
		{
			this.exception = e;
		}
	}

	/**
	 * Template init implementation.
	 * 
	 * @param request
	 * @throws Exception
	 */
	protected void init( HttpServletRequest request ) throws Exception
	{
		this.locale = ParameterAccessor.getLocale( request );
		this.__init( request );
	}

	/*
	 * Prepare the report parameters
	 */
	protected void __initParameters( HttpServletRequest request )
			throws Exception
	{
		IViewerReportDesignHandle design = getDesignHandle( request );
		InputOptions options = new InputOptions( );
		options.setOption( InputOptions.OPT_REQUEST, request );
		options.setOption( InputOptions.OPT_LOCALE, locale );

		Collection parameterList = this.getReportService( )
				.getParameterDefinitions( design, options, false );

		// TODO: Change parameters to be Map, not HashMap
		this.parameters = (HashMap) getParsedParameters( design, parameterList,
				request, options );

		this.missingParameter = validateParameters( parameterList,
				this.parameters );
	}

	protected IViewerReportDesignHandle getDesignHandle(
			HttpServletRequest request )
	{
		return new BirtViewerReportDesignHandle( null, reportDesignName );
	}

	/**
	 * @return Returns the parameterBean.
	 */
	public ParameterAttributeBean getParameterBean( )
	{
		return parameterBean;
	}

	/**
	 * @param parameterBean
	 *            The parameterBean to set.
	 */
	public void setParameterBean( ParameterAttributeBean parameterBean )
	{
		this.parameterBean = parameterBean;
	}

	/**
	 * @return Returns the reportTitle.
	 */
	public String getReportTitle( )
	{
		return reportTitle;
	}

	/**
	 * @return Returns the reportPage.
	 */
	public String getReportPage( )
	{
		return reportPage;
	}

	/**
	 * @return Returns the locale.
	 */
	public Locale getLocale( )
	{
		return locale;
	}

	/**
	 * @return Returns the useTestConfig.
	 */
	public boolean isDesigner( )
	{
		return isDesigner;
	}

	/**
	 * @return Returns the exception.
	 */
	public Exception getException( )
	{
		return exception;
	}

	/**
	 * @return Returns the reportDocumentName.
	 */
	public String getReportDocumentName( )
	{
		return reportDocumentName;
	}

	/**
	 * @return Returns the bookmark.
	 */
	public String getBookmark( )
	{
		return bookmark;
	}

	/**
	 * @return Returns the parameters.
	 */
	public HashMap getParameters( )
	{
		return parameters;
	}

	/**
	 * @return Returns the masterPageContent.
	 */
	public boolean isMasterPageContent( )
	{
		return masterPageContent;
	}

	/**
	 * @return Returns the missingParameter.
	 */
	public boolean isMissingParameter( )
	{
		return missingParameter;
	}

	/**
	 * @return Returns incoming request's category.
	 */
	public String getCategory( )
	{
		return category;
	}

	/**
	 * @return report design name.
	 */
	public String getReportDesignName( )
	{
		return reportDesignName;
	}
	
	/**
	 * @return the format
	 */
	
	public String getFormat( )
	{
		return format;
	}

	/**
	 * Override default finalizer.
	 * 
	 * @exception Throwable
	 * @return
	 */
	protected void finalize( ) throws Throwable
	{
		try
		{
			__finalize( );
		}
		finally
		{
			super.finalize( );
		}
	}

	/**
	 * Check whether missing parameter or not.
	 * 
	 * @param task
	 * @param parameters
	 * @return
	 */
	protected static boolean validateParameters( Collection parameterList,
			Map parameters )
	{
		assert parameters != null;

		boolean missingParameter = false;

		for ( Iterator iter = parameterList.iterator( ); iter.hasNext( ); )
		{
			ParameterDefinition  parameterObj = (ParameterDefinition ) iter
					.next( );

			String parameterName = parameterObj.getName( );
			Object parameterValue = parameters.get( parameterName );

			if ( parameterObj.isHidden( ) )
			{
				continue;
			}

			if ( parameterValue == null && !parameterObj.allowNull( ) )
			{
				missingParameter = true;
				break;
			}

			if ( ParameterDefinition .TYPE_STRING == parameterObj.getDataType( ) )
			{
				String parameterStringValue = (String) parameterValue;
				if ( parameterStringValue != null
						&& parameterStringValue.length( ) <= 0
						&& !parameterObj.allowBlank( ) )
				{
					missingParameter = true;
					break;
				}
			}
		}

		return missingParameter;
	}

	private Map getParsedParameters( IViewerReportDesignHandle design,
			Collection parameterList, HttpServletRequest request,
			InputOptions options ) throws ReportServiceException
	{
		Map params = new HashMap( );
		for ( Iterator iter = parameterList.iterator( ); iter.hasNext( ); )
		{
			ParameterDefinition  parameterObj = (ParameterDefinition ) iter
					.next( );

			String paramName = parameterObj.getName( );

			Object paramValueObj = this.getParamValueObject( request,
					parameterObj );
			if ( paramValueObj == null )
				paramValueObj = getReportService( ).getParameterDefaultValue(
						design, parameterObj.getName( ), options );

			params.put( paramName, paramValueObj );
		}
		return params;
	}

	protected Object getParamValueObject( HttpServletRequest request,
			ParameterDefinition  parameterObj ) throws ReportServiceException
	{
		String paramName = parameterObj.getName( );
		String format = parameterObj.getDisplayFormat( );
		if ( ParameterAccessor.isReportParameterExist( request, paramName ) )
		{
			ReportParameterConverter converter = new ReportParameterConverter(
					format, locale );
			// Get value from http request
			String paramValue = ParameterAccessor.getReportParameter( request,
					paramName, null );
			return converter.parse( paramValue, parameterObj.getDataType( ) );
		}
		return null;
	}
}