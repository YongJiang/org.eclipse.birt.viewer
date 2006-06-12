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

package org.eclipse.birt.report.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import org.eclipse.birt.report.engine.api.HTMLRenderContext;
import org.eclipse.birt.report.engine.api.IAction;
import org.eclipse.birt.report.engine.api.IHTMLActionHandler;
import org.eclipse.birt.report.engine.api.IReportDocument;
import org.eclipse.birt.report.utility.ParameterAccessor;

/**
 * HTML action handler for url generation.
 */
class ViewerHTMLActionHandler implements IHTMLActionHandler
{

	/**
	 * Logger for this handler.
	 */

	protected Logger log = Logger.getLogger( ViewerHTMLActionHandler.class
			.getName( ) );

	/**
	 * Document instance.
	 */

	protected IReportDocument document = null;

	/**
	 * Locale of the requester.
	 */

	protected Locale locale = null;

	/**
	 * Page number of the action requester.
	 */

	protected long page = -1;

	/**
	 * if the page is embedded, the bookmark should always be a url to submit.
	 */
	protected boolean isEmbeddable = false;

	/**
	 * RTL option setting by the command line or URL parameter.
	 */

	protected boolean isRtl = false;

	/**
	 * Constructor.
	 */
	public ViewerHTMLActionHandler( )
	{
	}

	/**
	 * Constructor.
	 * 
	 * @param document
	 * @param page
	 * @param locale
	 */

	public ViewerHTMLActionHandler( IReportDocument document, long page,
			Locale locale, boolean isEmbeddable, boolean isRtl )
	{
		this.document = document;
		this.page = page;
		this.locale = locale;
		this.isEmbeddable = isEmbeddable;
		this.isRtl = isRtl;
	}

	/**
	 * Get URL
	 */
	public String getURL( IAction actionDefn, Object context )
	{
		if ( actionDefn == null )
		{
			return null;
		}
		switch ( actionDefn.getType( ) )
		{
			case IAction.ACTION_BOOKMARK :
			{
				return buildBookmarkAction( actionDefn, context );
			}
			case IAction.ACTION_HYPERLINK :
			{
				return actionDefn.getActionString( );
			}
			case IAction.ACTION_DRILLTHROUGH :
			{
				return buildDrillAction( actionDefn, context );
			}
		}

		return null;
	}

	/**
	 * Build URL for bookmark.
	 * 
	 * @param action
	 * @param context
	 * @return the bookmark url
	 */

	protected String buildBookmarkAction( IAction action, Object context )
	{
		StringBuffer link = new StringBuffer( );

		boolean realBookmark = false;

		if ( this.document != null )
		{
			long pageNumber = this.document
					.getPageNumber( action.getBookmark( ) );
			realBookmark = ( pageNumber == this.page && !isEmbeddable );
		}

		String bookmark = action.getBookmark( );
		try
		{
			bookmark = URLEncoder.encode( bookmark,
					ParameterAccessor.UTF_8_ENCODE );
		}
		catch ( UnsupportedEncodingException e )
		{
			// Does nothing
		}

		String baseURL = null;
		if ( context != null && context instanceof HTMLRenderContext )
		{
			baseURL = ( (HTMLRenderContext) context ).getBaseURL( );
		}

		link.append( baseURL );
		link.append( ParameterAccessor.QUERY_CHAR );
		link.append( ParameterAccessor.PARAM_REPORT_DOCUMENT );
		link.append( ParameterAccessor.EQUALS_OPERATOR );
		String documentName = document.getName( );

		try
		{
			documentName = URLEncoder.encode( documentName,
					ParameterAccessor.UTF_8_ENCODE );
		}
		catch ( UnsupportedEncodingException e )
		{
			// Does nothing
		}
		link.append( documentName );

		if ( locale != null )
		{
			link.append( ParameterAccessor.getQueryParameterString(
					ParameterAccessor.PARAM_LOCALE, locale.toString( ) ) );
		}
		if ( isRtl )
		{
			link.append( ParameterAccessor.getQueryParameterString(
					ParameterAccessor.PARAM_RTL, String.valueOf( isRtl ) ) );
		}

		if ( realBookmark )
		{
			link.append( "#" ); //$NON-NLS-1$
			link.append( bookmark );
		}
		else
		{
			link.append( ParameterAccessor.getQueryParameterString(
					ParameterAccessor.PARAM_BOOKMARK, bookmark ) );
		}

		return link.toString( );
	}

	/**
	 * builds URL for drillthrough action
	 * 
	 * @param action
	 *            instance of the IAction instance
	 * @param context
	 *            the context for building the action string
	 * @return a URL
	 */
	protected String buildDrillAction( IAction action, Object context )
	{
		String baseURL = null;
		if ( context != null && context instanceof HTMLRenderContext )
		{
			baseURL = ( (HTMLRenderContext) context ).getBaseURL( );
		}

		StringBuffer link = new StringBuffer( );
		String reportName = action.getReportName( );

		if ( reportName != null && !reportName.equals( "" ) ) //$NON-NLS-1$
		{
			String format = action.getFormat( );
			if ( ParameterAccessor.PARAM_FORMAT_PDF.equalsIgnoreCase( format ) )
			{
				link.append( baseURL.replaceFirst( "frameset", "run" ) ); //$NON-NLS-1$ //$NON-NLS-2$
			}
			else
			{
				link.append( baseURL );
			}

			link
					.append( reportName.toLowerCase( )
							.endsWith( ".rptdocument" ) ? "?__document=" : "?__report=" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

			try
			{
				link.append( URLEncoder.encode( reportName,
						ParameterAccessor.UTF_8_ENCODE ) );
			}
			catch ( UnsupportedEncodingException e1 )
			{
				// It should not happen. Does nothing
			}

			// add format support
			if ( format != null && format.length( ) > 0 )
			{
				link.append( ParameterAccessor.getQueryParameterString(
						ParameterAccessor.PARAM_FORMAT, format ) );
			}

			// Adds the parameters
			if ( action.getParameterBindings( ) != null )
			{
				Iterator paramsIte = action.getParameterBindings( ).entrySet( )
						.iterator( );
				while ( paramsIte.hasNext( ) )
				{
					Map.Entry entry = (Map.Entry) paramsIte.next( );
					try
					{
						String key = (String) entry.getKey( );
						Object valueObj = entry.getValue( );
						if ( valueObj != null )
						{
							String value = valueObj.toString( );
							link
									.append( ParameterAccessor
											.getQueryParameterString(
													URLEncoder
															.encode(
																	key,
																	ParameterAccessor.UTF_8_ENCODE ),
													URLEncoder
															.encode(
																	value,
																	ParameterAccessor.UTF_8_ENCODE ) ) );
						}
					}
					catch ( UnsupportedEncodingException e )
					{
						// Does nothing
					}
				}
			}

			// Adding overwrite.
			link
					.append( ParameterAccessor.getQueryParameterString(
							ParameterAccessor.PARAM_OVERWRITE, String
									.valueOf( true ) ) );

			// The search rules are not supported yet.
			if ( !ParameterAccessor.PARAM_FORMAT_PDF.equalsIgnoreCase( format )
					&& action.getBookmark( ) != null )
			{

				try
				{
					link.append( ParameterAccessor.getQueryParameterString(
							ParameterAccessor.PARAM_BOOKMARK, URLEncoder
									.encode( action.getBookmark( ),
											ParameterAccessor.UTF_8_ENCODE ) ) );
				}
				catch ( UnsupportedEncodingException e )
				{
					// Does nothing
				}

				if ( !action.isBookmark( ) )
				{
					link.append( ParameterAccessor.getQueryParameterString(
							ParameterAccessor._TOC, String.valueOf( true ) ) );

				}
			}
		}

		if ( locale != null )
		{
			link.append( ParameterAccessor.getQueryParameterString(
					ParameterAccessor.PARAM_LOCALE, locale.toString( ) ) );
		}
		if ( isRtl )
		{
			link.append( ParameterAccessor.getQueryParameterString(
					ParameterAccessor.PARAM_RTL, String.valueOf( isRtl ) ) );
		}

		return link.toString( );
	}
}