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

package org.eclipse.birt.report.utility;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;

/**
 * Utilites class for all types of URl related operatnios.
 * <p>
 */
public class ParameterAccessor
{
	/**
	 * URL parameter names.
	 */
	public static final String PARAM_REPORT				= "__report"; //$NON-NLS-1$
	public static final String PARAM_REPORT_DOCUMENT		= "__document"; //$NON-NLS-1$
	public static final String PARAM_FORMAT				= "__format"; //$NON-NLS-1$
	public static final String PARAM_FORMAT_HTML			= "html"; //$NON-NLS-1$
	public static final String PARAM_FORMAT_PDF			= "pdf"; //$NON-NLS-1$
	public static final String PARAM_LOCALE				= "__locale"; //$NON-NLS-1$
	public static final String PARAM_SVG					= "__svg"; //$NON-NLS-1$
	public static final String PARAM_PAGE					= "__page"; //$NON-NLS-1$
	public static final String PARAM_ISNULL				= "__isnull"; //$NON-NLS-1$
	public static final String PARAM_MASTERPAGE			= "__masterpage"; //$NON-NLS-1$
	public static final String PARAM_DESIGNER				= "__designer"; //$NON-NLS-1$
	public static final String PARAM_OVERWRITE			= "__overwrite"; //$NON-NLS-1$
	public static final String PARAM_IMAGEID				= "__imageID"; //$NON-NLS-1$
	public static final String PARAM_BOOKMARK				= "__bookmark"; //$NON-NLS-1$

	/**
	 * Parametrer passed over by export data form.
	 */
	public static final String PARAM_IID					= "iid"; //$NON-NLS-1$
	public static final String PARAM_RESULTSETNAME		= "ResultSetName"; //$NON-NLS-1$
	public static final String PARAM_SELECTEDCOLUMNNUMBER	= "SelectedColumnNumber"; //$NON-NLS-1$
	public static final String PARAM_SELECTEDCOLUMN		= "SelectedColumn"; //$NON-NLS-1$
		
	/**
	 * Servlet configuration parameter names.
	 */
	public static final String INIT_PARAM_LOCALE			= "BIRT_VIEWER_LOCALE"; //$NON-NLS-1$
	public static final String INIT_PARAM_REPORT_DIR		= "BIRT_VIEWER_WORKING_FOLDER"; //$NON-NLS-1$
	public static final String INIT_PARAM_IMAGE_DIR		= "BIRT_VIEWER_IMAGE_DIR"; //$NON-NLS-1$
	public static final String INIT_PARAM_LOG_DIR			= "BIRT_VIEWER_LOG_DIR"; //$NON-NLS-1$
	public static final String INIT_PARAM_LOG_LEVEL		= "BIRT_VIEWER_LOG_LEVEL"; //$NON-NLS-1$
	public static final String INIT_PARAM_SCRIPTLIB_DIR	= "BIRT_VIEWER_SCRIPTLIB_DIR"; //$NON-NLS-1$
	
	/**
	 * Report working folder.
	 */
	private static String workingFolder = null;
	
	/**
	 * Current web application locale.
	 */
	private static Locale webAppLocale = null;
	
	/**
	 * Initial the parameters class.
	 * Web.xml is in UTF-8 format. No need to do encoding convertion.
	 * 
	 * @param config Servlet configuration
	 */
	public synchronized static void initParameters( ServletConfig config )
	{
		// Report root.in the web.xml has higher priority.
		workingFolder =  config.getServletContext( ).getInitParameter( INIT_PARAM_REPORT_DIR );
		
		if ( workingFolder == null || workingFolder.trim( ).length( ) <= 0 )
		{
			// Use birt dir as default report root.
			workingFolder = config.getServletContext( ).getRealPath( "/" ); //$NON-NLS-1$
		}
		
		// Report root could be empty. .WAR
		// Clear out report location.
		if ( workingFolder != null && workingFolder.trim( ).endsWith( File.separator ) )
		{
			workingFolder = workingFolder.trim( ).substring( 0, workingFolder.trim( ).length( ) - 1 );
		}
		
		webAppLocale = getLocaleFromString( config.getServletContext( ).getInitParameter( INIT_PARAM_LOCALE ) );
	}

	/**
	 * Get report file name.
	 * 
	 * @param request http request
	 * @return report file name
	 */
	public static String getReport( HttpServletRequest request )
	{
		String fileName = getParameter( request, PARAM_REPORT );
		
		if ( fileName == null )
		{
			fileName = ""; //$NON-NLS-1$
		}
		else
		{
			fileName = fileName.trim( );			
		}

		// Get absolute report location.
		if ( isRelativePath( fileName ) )
		{
			if ( fileName.startsWith( File.separator ) )
			{
				fileName = workingFolder + fileName;
			}
			else
			{
				fileName = workingFolder + File.separator + fileName;
			}
		}

		return fileName;
	}

	/**
	 * Get report document name.
	 * 
	 * @param request http request
	 * @return report file name
	 */
	public static String getReportDocument( HttpServletRequest request )
	{
		String fileName = getParameter( request, PARAM_REPORT_DOCUMENT );
		
		if ( fileName == null )
		{
			fileName = ""; //$NON-NLS-1$
		}
		else
		{
			fileName = fileName.trim( );			
		}

		if ( fileName == null || fileName.length() <= 0 )
		{
			fileName = getReport( request );
			fileName = fileName.substring( 0, fileName.lastIndexOf( '.' ) ) + ".rptdocument"; //$NON-NLS-1$
		}

		if ( fileName != null && fileName.length() > 0 )
		{
			// Get absolute report location.
			if ( isRelativePath( fileName ) )
			{
				if ( fileName.startsWith( File.separator ) )
				{
					fileName = workingFolder + fileName;
				}
				else
				{
					fileName = workingFolder + File.separator + fileName;
				}
			}
		}
		
		return fileName;
	}

	/**
	 * Check whether the viewer allows master page content or not.
	 * 
	 * @param request
	 * @return
	 */
	public static boolean isMasterPageContent( HttpServletRequest request )
	{
		boolean isMasterPageContent = true;
		
		if ( "false".equalsIgnoreCase( getParameter( request, PARAM_MASTERPAGE ) ) ) //$NON-NLS-1$
		{
			isMasterPageContent = false;
		}
		
		return isMasterPageContent;
	}

	/**
	 * Check whether the viewer is used in designer or not.
	 *  
	 * @param request
	 * @return
	 */
	public static boolean isDesigner( HttpServletRequest request )
	{
		boolean inDEsigner = false;
		
		if ( "true".equalsIgnoreCase( getParameter( request, PARAM_DESIGNER ) ) ) //$NON-NLS-1$
		{
			inDEsigner = true;
		}
		
		return inDEsigner;
	}

	/**
	 * Check whether report design will overwrite report doc or not.
	 *  
	 * @param request
	 * @return
	 */
	public static boolean isOverwrite( HttpServletRequest request )
	{
		boolean overwrite = false;
		
		if ( "true".equalsIgnoreCase( getParameter( request, PARAM_OVERWRITE ) ) ) //$NON-NLS-1$
		{
			overwrite = true;
		}
		
		return overwrite;
	}

	/**
	 * Checks if a given file name contains relative path.
	 * 
	 * @param fileName
	 *            The file name.
	 * @return A <code>boolean</code> value indicating if the file name
	 *         contains relative path or not.
	 */
	public static boolean isRelativePath( String fileName )
	{
		if (File.separatorChar == '\\')
		{
			// Win32
			return fileName != null && fileName.indexOf( ':' ) <= 0;
			
		}
		else if (File.separatorChar == '/')
		{
			// Unix
			return fileName != null && !fileName.startsWith("/"); //$NON-NLS-1$
		}
		
		return false;
	}

	/**
	 * Get report format.
	 * 
	 * @param request http request
	 * @return report format
	 */
	public static String getFormat( HttpServletRequest request )
	{
		String format = getParameter( request, PARAM_FORMAT );
		if( format != null && 
			format.length() > 0)
		{
			return format;
		}

		return PARAM_FORMAT_HTML; // The default format is html. 
	}

	/**
	 * Get report locale from Http request.
	 * 
	 * @param request http request
	 * @return report locale
	 */
	public static Locale getLocale( HttpServletRequest request )
	{
		return getLocaleFromString( getParameter( request, PARAM_LOCALE ) );
	}

	/**
	 * Get report page from Http request.
	 * 
	 * @param request http request
	 * @return report locale
	 */
	public static int getPage( HttpServletRequest request )
	{
		int page = getParameterAsInt( request, PARAM_PAGE );
		return page <= 0 ? 1 : page; // The default page value is 1.
	}

	/**
	 * Get web application locale.
	 * 
	 * @return report locale
	 */
	public static Locale getWebAppLocale( )
	{
		return webAppLocale;
	}

	/**
	 * Check whether enable svg support or not.
	 * 
	 * @param request http request
	 * @return whether or not render content toolbar
	 */
	public static boolean getSVGFlag( HttpServletRequest request )
	{
		boolean svg = false;
		
		if ( "true".equalsIgnoreCase( getParameter( request, PARAM_SVG ) ) ) //$NON-NLS-1$
		{
			svg = true;
		}

		return svg;
	}

	/**
	 * Get report locale from a given string.
	 * 
	 * @param locale locale string
	 * @return report locale
	 */
	public static Locale getLocaleFromString( String locale )
	{
		if (locale == null || locale.length( ) <= 0)
		{
			return Locale.getDefault( );
		}
		
		int index = locale.indexOf( '_' );

		if ( index != -1 )
		{
			String language = locale.substring( 0, index );
			String country = locale.substring( index + 1 );
			return new Locale( language, country );
		}
		
		return new Locale( locale );
	}

	/**
	 * Get report locale in string.
	 * 
	 * @param request http request
	 * @return report String
	 */
	public static String getLocaleString( HttpServletRequest request )
	{
		return getParameter( request, PARAM_LOCALE );
	}

	/**
	 * Get report parameter by given name.
	 * 
	 * @param request http request
	 * @param name parameter name
	 * @param defaultValue default parameter value
	 * @return parameter value
	 */
	public static String getReportParameter( HttpServletRequest request, String name, String defaultValue )
	{
		assert request != null && name != null;
		
		String value = getParameter( request, name );
		
		if ( value == null || ( (String) value ).trim( ).length( ) <= 0 )	// Treat it as blank value.
		{
			value = ""; //$NON-NLS-1$
		}

		Map paramMap =  request.getParameterMap( );
		String ISOName = toISOString( name );

		if ( paramMap == null || !paramMap.containsKey( ISOName ) )
		{
			value = defaultValue;
		}
		
		Set nullParams = getParameterValues( request, PARAM_ISNULL );
		
		if ( nullParams != null && nullParams.contains( name ) )
		{
			value = null;
		}
		
		return value;
	}

	/**
	 * Check whether report parameter exists in the url.
	 * 
	 * @param request http request
	 * @param name parameter name
	 * @return whether report parameter exists in the url
	 */
	public static boolean isReportParameterExist( HttpServletRequest request, String name )
	{
		assert request != null && name != null;
		
		boolean isExist = false;

		Map paramMap =  request.getParameterMap( );
		String ISOName = toISOString( name );

		if ( paramMap != null && paramMap.containsKey( ISOName ) )
		{
			isExist = true;
		}
		
		Set nullParams = getParameterValues( request, PARAM_ISNULL );
		
		if ( nullParams != null && nullParams.contains( name ) )
		{
			isExist = true;
		}
		
		return isExist;
	}

	/**
	 * Get query string with new parameter value.
	 * 
	 * @param request http request
	 * @param name parameter name
	 * @param value default parameter value
	 * @return new query string with new parameter value
	 */
	public static String getEncodedQueryString( HttpServletRequest request, String name, String value )
	{
		String queryString = ""; //$NON-NLS-1$
		Enumeration e = request.getParameterNames( );
		Set nullParams = getParameterValues( request, PARAM_ISNULL );
		boolean isFirst = true;
		
		while ( e.hasMoreElements( ) )
		{
			String paramName = ( String ) e.nextElement( );
			
			if ( paramName != null && !paramName.equalsIgnoreCase( PARAM_ISNULL ))
			{
				String paramValue = getParameter( request, paramName, false );
				
				if ( nullParams != null
						&& nullParams.remove( toUTFString( paramName ) )
						&& !paramName.equalsIgnoreCase( name ))	// Parameter value is null.
				{
					paramName = urlEncode( paramName, "ISO-8859-1" ); //$NON-NLS-1$
					queryString += ( isFirst? "" : "&" ) + PARAM_ISNULL + "=" + paramName; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					isFirst = false;
					continue;
				}
				
				if ( paramName.equalsIgnoreCase( name ) )
				{
					paramValue = value;
				}
				
				paramName = urlEncode( paramName, "ISO-8859-1" ); //$NON-NLS-1$
				paramValue = urlEncode( paramValue, "UTF-8" ); //$NON-NLS-1$
				queryString += ( isFirst? "" : "&" ) + paramName + "=" + paramValue; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				isFirst = false;
			}
		}
		
		if ( nullParams != null && nullParams.size( ) > 0 )
		{
			Iterator i = nullParams.iterator( );
			
			while ( i.hasNext( ) )
			{
				String paramName = (String) i.next( );
				
				if ( paramName != null && !paramName.equalsIgnoreCase( name ) )
				{
					paramName = urlEncode( paramName, "UTF-8" ); //$NON-NLS-1$
					queryString += ( isFirst? "" : "&" ) + PARAM_ISNULL + "=" + paramName; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					isFirst = false;
				}
			}
		}

		if ( getParameter( request, name ) == null )
		{
			String paramValue = value;
			paramValue = urlEncode( paramValue, "UTF-8" ); //$NON-NLS-1$
			queryString += ( isFirst? "" : "&" ) + name + "=" + paramValue; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			isFirst = false;
		}

		return queryString;
	}

	/**
	 * Get bookmark. If page exists, ignore bookmark.
	 * 
	 * @param request
	 * @return
	 */
	public static String getBookmark( HttpServletRequest request )
	{
		int page = getParameterAsInt( request, PARAM_PAGE );
		return page < 1 ? getReportParameter( request, PARAM_BOOKMARK, null ) : null;
	}
	
	/**************************************************************************
	 * For export data
	 **************************************************************************/
	
	/**
	 * Get report element's iid.
	 * 
	 * @param request
	 * @return
	 */
	public static String getIId( HttpServletRequest request )
	{
		return getReportParameter( request, PARAM_IID, null );
	}
	
	/**
	 * Get result set name.
	 * 
	 * @param request
	 * @return
	 */
	public static String getResultSetName( HttpServletRequest request )
	{
		return getReportParameter( request, PARAM_RESULTSETNAME, null );
	}
	
	/**
	 * Get solected column name list.
	 * 
	 * @param request
	 * @return
	 */
	public static Collection getSelectedColumns( HttpServletRequest request )
	{
		ArrayList columns = new ArrayList( );
		
		int columnCount = getParameterAsInt( request, PARAM_SELECTEDCOLUMNNUMBER );
		for ( int i = 0; i < columnCount; i++ )
		{
			String paramName = PARAM_SELECTEDCOLUMN + String.valueOf( i );
			String columnName = getParameter( request, paramName );
			columns.add( columnName );
		}

		return columns;
	}
	
	/**************************************************************************
	 * Private routines
	 **************************************************************************/
	
	/**
	 * Get named parameter as integer from http request.
	 * parameter names and values are all in iso-8859-1 format in request.
	 * 
	 * @param request
	 * @param parameterName
	 * @return
	 */
	private static int getParameterAsInt( HttpServletRequest request, String parameterName )
	{
		int iValue = -1;
		String value = getParameter( request, parameterName );

		if( value != null && value.length() > 0)
		{
			try
			{
				iValue = Integer.parseInt( value );
			}
			catch ( NumberFormatException ex )
			{
				iValue = -1;
			}
		}
		return iValue;
	}

	/**
	 * Get named parameter from http request.
	 * parameter names and values are all in iso-8859-1 format in request.
	 * 
	 * @param request incoming http request
	 * @param parameterName parameter name in UTF-8 format
	 * @return
	 */
	private static String getParameter( HttpServletRequest request, String parameterName )
	{
		return getParameter( request, parameterName, true );
	}

	/**
	 * Get named parameter from http request.
	 * parameter names and values are all in iso-8859-1 format in request.
	 * 
	 * @param request incoming http request
	 * @param parameterName parameter name
	 * @param isUTF is parameter in UTF-8 formator not
	 * @return
	 */
	private static String getParameter( HttpServletRequest request, String parameterName, boolean isUTF )
	{
		String ISOParameterName = ( isUTF )? toISOString( parameterName ) : parameterName;
		return toUTFString( request.getParameter( ISOParameterName ) );
	}

	/**
	 * Get named parameters from http request.
	 * parameter names and values are all in iso-8859-1 format in request.
	 * 
	 * @param request incoming http request
	 * @param parameterName parameter name in UTF-8 format
	 * @return
	 */
	public static Set getParameterValues( HttpServletRequest request, String parameterName )
	{
		return getParameterValues( request, parameterName, true );
	}

	/**
	 * Get named parameters from http request.
	 * parameter names and values are all in iso-8859-1 format in request.
	 * 
	 * @param request incoming http request
	 * @param parameterName parameter name
	 * @param isUTF is parameter in UTF-8 formator not
	 * @return
	 */
	private static Set getParameterValues( HttpServletRequest request, String parameterName, boolean isUTF )
	{
		HashSet parameterValues = null;
		String ISOParameterName = ( isUTF )? toISOString( parameterName ) : parameterName;
		String[] ISOParameterValues = request.getParameterValues( ISOParameterName );
		
		if ( ISOParameterValues != null )
		{
			parameterValues = new HashSet( );
			
			for (int i = 0; i < ISOParameterValues.length; i++ )
			{
				parameterValues.add( toUTFString( ISOParameterValues[i] ) );
			}
		}

		return parameterValues;
	}

	/**
	 * Convert UTF-8 string into ISO-8895-1
	 * @param s UTF-8 string
	 * @return
	 */
	private static String toISOString( String s )
	{
		String ISOString = s;
		
		if ( s != null )
		{
			try
			{
				ISOString =  new String( s.getBytes( "UTF-8" ), "ISO-8859-1" ); //$NON-NLS-1$ //$NON-NLS-2$
			}
			catch ( UnsupportedEncodingException e )
			{
				ISOString = s;
			}
		}
		
		return ISOString;
	}
	
	/**
	 * Convert ISO-8895-1 string into UTF-8
	 * @param s ISO-8895-1 string
	 * @return
	 */
	private static String toUTFString( String s )
	{
		String UTFString = s;
		
		if ( s != null )
		{
			try
			{
				UTFString =  new String( s.getBytes( "ISO-8859-1" ), "UTF-8" ); //$NON-NLS-1$ //$NON-NLS-2$
			}
			catch ( UnsupportedEncodingException e )
			{
				UTFString = s;
			}
		}
		
		return UTFString;
	}

	/**
	 * URL encoding based on incoming encoding format.
	 * @param s string to be encoded.
	 * @param format encoding format. 
	 * @return
	 */
	private static String urlEncode( String s, String format )
	{
		assert "ISO-8859-1".equalsIgnoreCase( format ) || "UTF-8".equalsIgnoreCase( format ); //$NON-NLS-1$ //$NON-NLS-2$
		String encodedString = s;
		
		if ( s != null )
		{
			try
			{
				encodedString =  URLEncoder.encode( s, format ); //$NON-NLS-1$
			}
			catch ( UnsupportedEncodingException e )
			{
				encodedString = s;
			}
		}
		
		return encodedString;
	}

	/**
	 * Is request parameter a report parameter.
	 *  
	 * @param	paraName
	 * @return	boolean
	 */
	private static final boolean isReportParameter( String paraName )
	{
		boolean isReportParameter = false;
		
		if ( paraName != null &&
				paraName.trim( ).length( ) > 2 &&
				!paraName.substring( 0, 2 ).equalsIgnoreCase( "__" ) ) //$NON-NLS-1$
		{
			isReportParameter = true;
		}
		
		return isReportParameter;
	}

	/**
	 * Check whether the request is to get image.
	 * 
	 * @param request http request
	 * @return is get image or not
	 */
	public static boolean isGetImageOperator( HttpServletRequest request )
	{
		String imageName = getParameter( request, PARAM_IMAGEID );
		return imageName != null && imageName.length( ) > 0;
	}

	/**
	 * This function is used to encode an ordinary string that may contain
	 * characters or more than one consecutive spaces for appropriate HTML display.
	 * 
	 * @param      s
	 * @return	   String
	 */
	public static final String htmlEncode( String s )
	{
		String sHtmlEncoded = ""; //$NON-NLS-1$
		
		if ( s == null )
		{
			return null;
		}

		StringBuffer sbHtmlEncoded = new StringBuffer( );
		final char chrarry[] = s.toCharArray( );
		
		for ( int i = 0; i < chrarry.length; i++ )
		{
			char c = chrarry[i];
			
			switch ( c )
			{
				case '\t':
					sbHtmlEncoded.append( "&#09;" ); //$NON-NLS-1$
					break;
				case '\n':
					sbHtmlEncoded.append( "<br>" ); //$NON-NLS-1$
					break;
				case '\r':
					sbHtmlEncoded.append( "&#13;" ); //$NON-NLS-1$
					break;
				case ' ':
					sbHtmlEncoded.append( "&#32;" ); //$NON-NLS-1$
					break;
				case '"':
					sbHtmlEncoded.append( "&#34;" ); //$NON-NLS-1$
					break;
				case '\'':
					sbHtmlEncoded.append( "&#39;" ); //$NON-NLS-1$
					break;
				case '<':
					sbHtmlEncoded.append( "&#60;" ); //$NON-NLS-1$
					break;
				case '>':
					sbHtmlEncoded.append( "&#62;" ); //$NON-NLS-1$
					break;
				case '`':
					sbHtmlEncoded.append( "&#96;" ); //$NON-NLS-1$
					break;
				case '&':
					sbHtmlEncoded.append( "&#38;" ); //$NON-NLS-1$
					break;
				default:
					sbHtmlEncoded.append( c );
			}
		}
		
		sHtmlEncoded = sbHtmlEncoded.toString( );
		return sHtmlEncoded;
	}
	
	/**
	 * Get current working folder.
	 * 
	 * @return Returns the workingFolder.
	 */
	public static String getWorkingFolder( )
	{
		return workingFolder;
	}
}