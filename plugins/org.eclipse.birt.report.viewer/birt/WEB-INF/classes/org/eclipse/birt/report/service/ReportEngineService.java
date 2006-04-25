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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;

import org.apache.axis.AxisFault;
import org.eclipse.birt.core.data.DataType;
import org.eclipse.birt.core.data.DataTypeUtil;
import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.core.framework.IPlatformContext;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.core.framework.PlatformServletContext;
import org.eclipse.birt.data.engine.api.IResultMetaData;
import org.eclipse.birt.report.IBirtConstants;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.EngineConstants;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.HTMLActionHandler;
import org.eclipse.birt.report.engine.api.HTMLEmitterConfig;
import org.eclipse.birt.report.engine.api.HTMLRenderContext;
import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.HTMLServerImageHandler;
import org.eclipse.birt.report.engine.api.IDataExtractionTask;
import org.eclipse.birt.report.engine.api.IDataIterator;
import org.eclipse.birt.report.engine.api.IExtractionResults;
import org.eclipse.birt.report.engine.api.IGetParameterDefinitionTask;
import org.eclipse.birt.report.engine.api.IRenderTask;
import org.eclipse.birt.report.engine.api.IReportDocument;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportEngineFactory;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IResultSetItem;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.IRunTask;
import org.eclipse.birt.report.engine.api.IScalarParameterDefn;
import org.eclipse.birt.report.engine.api.PDFRenderContext;
import org.eclipse.birt.report.engine.api.ReportParameterConverter;
import org.eclipse.birt.report.soapengine.api.Column;
import org.eclipse.birt.report.soapengine.api.ResultSet;
import org.eclipse.birt.report.utility.ParameterAccessor;

public class ReportEngineService
{
	private static ReportEngineService instance;

	/**
	 * Report engine instance.
	 */
	private IReportEngine engine = null;

	/**
	 * Static engine config instance.
	 */
	private EngineConfig config = null;

	/**
	 * Image directory for report images and charts.
	 */
	private String imageDirectory = null;

	/**
	 * URL accesses images.
	 */
	private String imageBaseUrl = null;

	/**
	 * Image handler instance.
	 */
	private HTMLServerImageHandler imageHandler = null;

	/**
	 * Web app context path.
	 */
	private String contextPath = null;

	/**
	 * Constructor.
	 * 
	 * @param config
	 */
	public ReportEngineService( ServletConfig servletConfig )
	{
		System.setProperty( "RUN_UNDER_ECLIPSE", "false" ); //$NON-NLS-1$ //$NON-NLS-2$

		if ( servletConfig == null )
		{
			return;
		}

		config = new EngineConfig( );

		// Register new image handler
		HTMLEmitterConfig emitterConfig = new HTMLEmitterConfig( );
		emitterConfig.setActionHandler( new HTMLActionHandler( ) );
		imageHandler = new HTMLServerImageHandler( );
		emitterConfig.setImageHandler( imageHandler );
		config.getEmitterConfigs( ).put( "html", emitterConfig ); //$NON-NLS-1$

		// Prepare image directory.
		imageDirectory = servletConfig.getServletContext( ).getInitParameter(
				ParameterAccessor.INIT_PARAM_IMAGE_DIR );

		if ( imageDirectory == null || imageDirectory.trim( ).length( ) <= 0
				|| ParameterAccessor.isRelativePath( imageDirectory ) )
		{
			imageDirectory = servletConfig.getServletContext( ).getRealPath(
					"/report/images" ); //$NON-NLS-1$
		}

		// Prepare image base url.
		imageBaseUrl = "/run?__imageID="; //$NON-NLS-1$

		// Prepare log directory.
		String logDirectory = servletConfig.getServletContext( )
				.getInitParameter( ParameterAccessor.INIT_PARAM_LOG_DIR );

		if ( logDirectory == null || logDirectory.trim( ).length( ) <= 0
				|| ParameterAccessor.isRelativePath( logDirectory ) )
		{
			logDirectory = servletConfig.getServletContext( ).getRealPath(
					"/logs" ); //$NON-NLS-1$
		}

		// Prepare log level.
		String logLevel = servletConfig.getServletContext( ).getInitParameter(
				ParameterAccessor.INIT_PARAM_LOG_LEVEL );
		Level level = Level.OFF;
		if ( "SEVERE".equalsIgnoreCase( logLevel ) ) //$NON-NLS-1$
		{
			level = Level.SEVERE;
		} else if ( "WARNING".equalsIgnoreCase( logLevel ) ) //$NON-NLS-1$
		{
			level = Level.WARNING;
		} else if ( "INFO".equalsIgnoreCase( logLevel ) ) //$NON-NLS-1$
		{
			level = Level.INFO;
		} else if ( "CONFIG".equalsIgnoreCase( logLevel ) ) //$NON-NLS-1$
		{
			level = Level.CONFIG;
		} else if ( "FINE".equalsIgnoreCase( logLevel ) ) //$NON-NLS-1$
		{
			level = Level.FINE;
		} else if ( "FINER".equalsIgnoreCase( logLevel ) ) //$NON-NLS-1$
		{
			level = Level.FINER;
		} else if ( "FINEST".equalsIgnoreCase( logLevel ) ) //$NON-NLS-1$
		{
			level = Level.FINEST;
		} else if ( "OFF".equalsIgnoreCase( logLevel ) ) //$NON-NLS-1$
		{
			level = Level.OFF;
		}
		config.setLogConfig( logDirectory, level );

		// Prepare ScriptLib location
		String scriptLibDir = servletConfig.getServletContext( )
				.getInitParameter( ParameterAccessor.INIT_PARAM_SCRIPTLIB_DIR );
		if ( scriptLibDir == null || scriptLibDir.trim( ).length( ) <= 0
				|| ParameterAccessor.isRelativePath( scriptLibDir ) )
		{
			scriptLibDir = servletConfig.getServletContext( ).getRealPath(
					"/scriptlib" ); //$NON-NLS-1$
		}

		ArrayList jarFileList = new ArrayList( );
		if ( scriptLibDir != null )
		{
			File dir = new File( scriptLibDir );
			getAllJarFiles( dir, jarFileList );
		}

		String scriptlibClassPath = ""; //$NON-NLS-1$
		for ( int i = 0; i < jarFileList.size( ); i++ )
			scriptlibClassPath += EngineConstants.PROPERTYSEPARATOR
					+ ( ( File ) jarFileList.get( i ) ).getAbsolutePath( );

		if ( scriptlibClassPath.startsWith( EngineConstants.PROPERTYSEPARATOR ) )
			scriptlibClassPath = scriptlibClassPath
					.substring( EngineConstants.PROPERTYSEPARATOR.length( ) );

		System.setProperty( EngineConstants.WEBAPP_CLASSPATH_KEY,
				scriptlibClassPath );

		config.setEngineHome( "" ); //$NON-NLS-1$
	}

	/**
	 * Get engine instance.
	 * 
	 * @return
	 */
	public static ReportEngineService getInstance( )
	{
		return instance;
	}

	/**
	 * Get engine instance.
	 * 
	 * @return engine instance
	 */
	public static void initEngineInstance( ServletConfig servletConfig )
			throws BirtException
	{
		if ( ReportEngineService.instance != null )
		{
			return;
		}
		ReportEngineService.instance = new ReportEngineService( servletConfig );
	}

	/**
	 * Get all the files under the specified folder (including all the files
	 * under sub-folders)
	 * 
	 * @param dir -
	 *            the folder to look into
	 * @param fileList -
	 *            the fileList to be returned
	 */
	private void getAllJarFiles( File dir, ArrayList fileList )
	{
		if ( dir.exists( ) && dir.isDirectory( ) )
		{
			File[] files = dir.listFiles( );
			if ( files == null )
				return;

			for ( int i = 0; i < files.length; i++ )
			{
				File file = files[i];
				if ( file.isFile( ) )
				{
					if ( file.getName( ).endsWith( ".jar" ) ) //$NON-NLS-1$
						fileList.add( file );
				} else if ( file.isDirectory( ) )
				{
					getAllJarFiles( file, fileList );
				}
			}
		}
	}

	/**
	 * Set Engine context.
	 * 
	 * @param servletContext
	 * @param request
	 */
	synchronized public void setEngineContext( ServletContext servletContext,
			HttpServletRequest request )
	{
		if ( engine == null )
		{
			IPlatformContext platformContext = new PlatformServletContext(
					servletContext );
			config.setPlatformContext( platformContext );

			try
			{
				Platform.startup( config );
			}
			catch ( BirtException e )
			{
				// TODO remove this output.

				e.printStackTrace( );
			}

			IReportEngineFactory factory = ( IReportEngineFactory ) Platform
					.createFactoryObject( IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY );
			engine = factory.createReportEngine( config );

			contextPath = request.getContextPath( );
		}
	}

	/**
	 * Open report design.
	 * 
	 * @param report
	 * @return
	 */
	synchronized public IReportRunnable openReportDesign( String report )
			throws EngineException
	{
		return engine.openReportDesign( report );
	}

	/**
	 * Open report design by using the input stream
	 * 
	 * @param reportStream -
	 *            the input stream
	 * @return IReportRunnable
	 * @throws EngineException
	 */
	synchronized public IReportRunnable openReportDesign(
			InputStream reportStream ) throws EngineException
	{
		return engine.openReportDesign( reportStream );
	}

	/**
	 * createGetParameterDefinitionTask.
	 * 
	 * @param runnable
	 * @return
	 */
	public IGetParameterDefinitionTask createGetParameterDefinitionTask(
			IReportRunnable runnable )
	{
		IGetParameterDefinitionTask task = null;

		try
		{
			synchronized ( this.getClass( ) )
			{
				task = engine.createGetParameterDefinitionTask( runnable );
			}
		}
		catch ( Exception e )
		{
		}

		return task;
	}

	/**
	 * Open report document from archive,
	 * 
	 * @param docName -
	 *            the name of the report document
	 * @return
	 */
	public IReportDocument openReportDocument( String docName )
	{

		IReportDocument document = null;

		try
		{
			synchronized ( this.getClass( ) )
			{
				document = engine.openReportDocument( docName );
			}
		}
		catch ( Exception e )
		{
		}

		return document;
	}

	/**
	 * Render image.
	 * 
	 * @param imageId
	 * @param outputStream
	 * @throws EngineException
	 */
	public void renderImage( String imageId, OutputStream outputStream )
			throws RemoteException
	{
		assert ( this.imageHandler != null );

		try
		{
			this.imageHandler.getImage( outputStream, this.imageDirectory,
					imageId );
		}
		catch ( EngineException e )
		{
			AxisFault fault = new AxisFault( );
			fault
					.setFaultCode( new QName(
							"ReportEngineService.renderImage( )" ) ); //$NON-NLS-1$
			fault.setFaultString( e.getLocalizedMessage( ) );
			throw fault;
		}

	}

	/**
	 * Create HTML render context.
	 * 
	 * @param svgFlag
	 * @return
	 */
	private HTMLRenderContext createHTMLrenderContext( boolean svgFlag )
	{
		HTMLRenderContext renderContext = new HTMLRenderContext( );
		renderContext.setImageDirectory( imageDirectory );
		renderContext.setBaseImageURL( contextPath + imageBaseUrl );
		renderContext.setBaseURL( this.contextPath + "/frameset" ); //$NON-NLS-1$
		renderContext
				.setSupportedImageFormats( svgFlag ? "PNG;GIF;JPG;BMP;SVG" : "PNG;GIF;JPG;BMP" ); //$NON-NLS-1$ //$NON-NLS-2$
		return renderContext;
	}

	/**
	 * Create PDF render context.
	 * 
	 * @return
	 */
	private PDFRenderContext createPDFrenderContext( )
	{
		PDFRenderContext renderContext = new PDFRenderContext( );
		renderContext.setBaseURL( this.contextPath + "/frameset" ); //$NON-NLS-1$
		renderContext.setSupportedImageFormats( "PNG;GIF;JPG;BMP" ); //$NON-NLS-1$
		return renderContext;
	}

	/**
	 * Run and render a report,
	 * 
	 * @param runnable
	 * @param outputStream
	 * @param format
	 * @param locale
	 * @param parameters
	 * @param svgFlag
	 * @throws IOException
	 */
	public void runAndRenderReport( HttpServletRequest request,
			IReportRunnable runnable, OutputStream outputStream, String format,
			Locale locale, Map parameters, boolean masterPage, boolean svgFlag )
			throws RemoteException
	{
		runAndRenderReport( request, runnable, outputStream, format, locale,
				parameters, masterPage, svgFlag, null, null );
	}

	/**
	 * Run and render a report,
	 * 
	 * @param runnable
	 * @param outputStream
	 * @param locale
	 * @param parameters
	 * @param svgFlag
	 * @param activeIds
	 * @throws IOException
	 */
	public void runAndRenderReport( HttpServletRequest request,
			IReportRunnable runnable, ByteArrayOutputStream outputStream,
			Locale locale, Map parameters, boolean masterPage, boolean svgFlag,
			List activeIds ) throws RemoteException
	{
		runAndRenderReport( request, runnable, outputStream,
				ParameterAccessor.PARAM_FORMAT_HTML, locale, parameters,
				masterPage, svgFlag, Boolean.TRUE, activeIds );
	}

	synchronized private void runAndRenderReport( HttpServletRequest request,
			IReportRunnable runnable, OutputStream outputStream, String format,
			Locale locale, Map parameters, boolean masterPage, boolean svgFlag,
			Boolean embeddable, List activeIds ) throws RemoteException
	{
		assert runnable != null;

		// Render options
		HTMLRenderOption option = new HTMLRenderOption( );
		option.setOutputStream( outputStream );
		option.setOutputFormat( format );
		option.setMasterPageContent( masterPage );

		if ( embeddable != null )
		{
			option.setEmbeddable( embeddable.booleanValue( ) );
		}

		if ( activeIds != null )
		{
			option.setInstanceIDs( activeIds );
		}

		IRunAndRenderTask runAndRenderTask = null;

		synchronized ( this.getClass( ) )
		{
			runAndRenderTask = engine.createRunAndRenderTask( runnable );
		}

		runAndRenderTask.setLocale( locale );
		if ( parameters != null )
		{
			runAndRenderTask.setParameterValues( parameters );
		}
		runAndRenderTask.setRenderOption( option );

		HashMap context = new HashMap( );

		// context.put( DataEngine.DATASET_CACHE_OPTION, Boolean.TRUE );
		context.put( "org.eclipse.birt.data.engine.dataset.cache.option",
				Boolean.TRUE );
		context.put( EngineConstants.APPCONTEXT_BIRT_VIEWER_HTTPSERVET_REQUEST,
				request );
		context.put( EngineConstants.APPCONTEXT_CLASSLOADER_KEY,
				ReportEngineService.class.getClassLoader( ) );

		if ( !ParameterAccessor.PARAM_FORMAT_PDF.equalsIgnoreCase( format ) )
		{
			context.put( EngineConstants.APPCONTEXT_HTML_RENDER_CONTEXT,
					createHTMLrenderContext( svgFlag ) );
		} else
		{
			context.put( EngineConstants.APPCONTEXT_PDF_RENDER_CONTEXT,
					createPDFrenderContext( ) );
		}

		runAndRenderTask.setAppContext( context );

		try
		{
			runAndRenderTask.run( );
		}
		catch ( BirtException e )
		{
			AxisFault fault = new AxisFault( );
			fault.setFaultCode( new QName(
					"ReportEngineService.runAndRenderReport( )" ) ); //$NON-NLS-1$
			fault.setFaultString( e.getLocalizedMessage( ) );
			throw fault;
		} finally
		{
			runAndRenderTask.close( );
		}
	}

	/**
	 * Run report.
	 * 
	 * @param runnable
	 * @param archive
	 * @param documentName
	 * @param locale
	 * @param parameters
	 * @throws RemoteException
	 */
	public void runReport( HttpServletRequest request,
			IReportRunnable runnable, String documentName, Locale locale,
			HashMap parameters ) throws RemoteException
	{
		assert runnable != null;

		// Preapre the run report task.
		IRunTask runTask = null;
		synchronized ( this.getClass( ) )
		{
			runTask = engine.createRunTask( runnable );
		}
		runTask.setLocale( locale );
		runTask.setParameterValues( parameters );

		HashMap context = new HashMap( );
		// context.put( DataEngine.DATASET_CACHE_OPTION, Boolean.TRUE );
		context.put( "org.eclipse.birt.data.engine.dataset.cache.option",
				Boolean.TRUE );
		context.put( EngineConstants.APPCONTEXT_BIRT_VIEWER_HTTPSERVET_REQUEST,
				request );
		context.put( EngineConstants.APPCONTEXT_CLASSLOADER_KEY,
				ReportEngineService.class.getClassLoader( ) );
		runTask.setAppContext( context );

		// Run report.
		try
		{
			runTask.run( documentName );
		}
		catch ( BirtException e )
		{
			// Any Birt exception.
			AxisFault fault = new AxisFault( );
			fault
					.setFaultCode( new QName(
							"ReportEngineService.runReport( )" ) ); //$NON-NLS-1$
			fault.setFaultString( e.getLocalizedMessage( ) );
			throw fault;
		} finally
		{
			runTask.close( );
		}
	}

	/**
	 * Render report page.
	 * 
	 * @param reportDocument
	 * @param pageNumber
	 * @param svgFlag
	 * @return report page content
	 * @throws RemoteException
	 */
	public ByteArrayOutputStream renderReport( HttpServletRequest request,
			IReportDocument reportDocument, long pageNumber,
			boolean masterPage, boolean svgFlag, List activeIds, Locale locale )
			throws RemoteException
	{
		assert reportDocument != null;
		assert pageNumber > 0 && pageNumber < reportDocument.getPageCount( );

		ByteArrayOutputStream out = new ByteArrayOutputStream( );

		// Create render task.
		IRenderTask renderTask = null;
		synchronized ( this.getClass( ) )
		{
			renderTask = engine.createRenderTask( reportDocument );
		}

		HashMap context = new HashMap( );
		context.put( EngineConstants.APPCONTEXT_HTML_RENDER_CONTEXT,
				createHTMLrenderContext( svgFlag ) );
		context.put( EngineConstants.APPCONTEXT_BIRT_VIEWER_HTTPSERVET_REQUEST,
				request );
		context.put( EngineConstants.APPCONTEXT_CLASSLOADER_KEY,
				ReportEngineService.class.getClassLoader( ) );
		renderTask.setAppContext( context );

		// Render option
		HTMLRenderOption setting = new HTMLRenderOption( );
		setting.setOutputStream( out );
		setting.setOutputFormat( IBirtConstants.RENDERFORMAT );
		setting.setEmbeddable( true );
		setting.setInstanceIDs( activeIds );
		setting.setMasterPageContent( masterPage );
		setting.setActionHandle( new ViewerHTMLActionHandler( reportDocument,
				pageNumber, locale ) );

		renderTask.setRenderOption( setting );
		renderTask.setLocale( locale );

		// Render designated page.
		try
		{
			renderTask.setPageNumber( pageNumber );
			renderTask.render( );
		}
		catch ( BirtException e )
		{
			AxisFault fault = new AxisFault( );
			fault.setFaultCode( new QName(
					"ReportEngineService.renderReport( )" ) ); //$NON-NLS-1$
			fault.setFaultString( e.getLocalizedMessage( ) );
			throw fault;
		}
		catch ( Exception e )
		{
			AxisFault fault = new AxisFault( );
			fault.setFaultCode( new QName(
					"ReportEngineService.renderReport( )" ) ); //$NON-NLS-1$
			fault.setFaultString( e.getLocalizedMessage( ) );
			throw fault;
		} finally
		{
			renderTask.close( );
		}

		return out;
	}

	/**
	 * Get query result sets.
	 * 
	 * @param document
	 * @return
	 * @throws RemoteException
	 */
	public ResultSet[] getResultSets( IReportDocument document )
			throws RemoteException
	{
		assert document != null;

		ResultSet[] resultSetArray = null;

		IDataExtractionTask dataTask = null;
		synchronized ( this.getClass( ) )
		{
			dataTask = engine.createDataExtractionTask( document );
		}

		try
		{
			List resultSets = dataTask.getResultSetList( );

			if ( resultSets != null && resultSets.size( ) > 0 )
			{
				resultSetArray = new ResultSet[resultSets.size( )];
				for ( int k = 0; k < resultSets.size( ); k++ )
				{
					resultSetArray[k] = new ResultSet( );
					IResultSetItem resultSetItem = ( IResultSetItem ) resultSets
							.get( k );
					assert resultSetItem != null;

					resultSetArray[k].setQueryName( resultSetItem
							.getResultSetName( ) );

					IResultMetaData metaData = resultSetItem
							.getResultMetaData( );
					assert metaData != null;

					Column[] columnArray = new Column[metaData.getColumnCount( )];
					for ( int i = 0; i < metaData.getColumnCount( ); i++ )
					{
						columnArray[i] = new Column( );

						String name = metaData.getColumnName( i );
						columnArray[i].setName( name );

						String label = metaData.getColumnLabel( i );
						if ( label == null || label.length( ) <= 0 )
						{
							label = name;
						}
						columnArray[i].setLabel( label );

						columnArray[i].setVisibility( new Boolean( true ) );
					}
					resultSetArray[k].setColumn( columnArray );
				}
			}
		}
		catch ( BirtException e )
		{
			e.printStackTrace( );
			AxisFault fault = new AxisFault( );
			fault
					.setFaultCode( new QName(
							"ReportEngineService.getMetaData( )" ) ); //$NON-NLS-1$
			fault.setFaultString( e.getLocalizedMessage( ) );
			throw fault;
		}
		catch ( Exception e )
		{
			e.printStackTrace( );
			AxisFault fault = new AxisFault( );
			fault
					.setFaultCode( new QName(
							"ReportEngineService.getMetaData( )" ) ); //$NON-NLS-1$
			fault.setFaultString( e.getLocalizedMessage( ) );
			throw fault;
		} finally
		{
			dataTask.close( );
		}

		return resultSetArray;
	}

	/**
	 * Extract data.
	 * 
	 * @param document
	 * @param id
	 * @param columns
	 * @param filters
	 * @param locale
	 * @param outputStream
	 * @throws RemoteException
	 */
	public void extractData( IReportDocument document, String resultSetName,
			Set columns, Locale locale, OutputStream outputStream )
			throws RemoteException
	{
		assert document != null;
		assert resultSetName != null && resultSetName.length( ) > 0;
		assert columns != null && !columns.isEmpty( );

		String[] columnNames = new String[columns.size( )];
		Iterator iSelectedColumns = columns.iterator( );
		for ( int i = 0; iSelectedColumns.hasNext( ); i++ )
		{
			columnNames[i] = ( String ) iSelectedColumns.next( );
		}

		IDataExtractionTask dataTask = null;
		IExtractionResults result = null;
		IDataIterator iData = null;
		try
		{
			synchronized ( this.getClass( ) )
			{
				dataTask = engine.createDataExtractionTask( document );
			}
			dataTask.selectResultSet( resultSetName );
			dataTask.selectColumns( columnNames );
			dataTask.setLocale( locale );

			result = dataTask.extract( );
			if ( result != null )
			{
				iData = result.nextResultIterator( );

				if ( iData != null && columnNames.length > 0 )
				{
					StringBuffer buf = new StringBuffer( );

					// Captions
					buf.append( columnNames[0] );

					for ( int i = 1; i < columnNames.length; i++ )
					{
						buf.append( ',' ); //$NON-NLS-1$
						buf.append( columnNames[i] );
					}

					buf.append( '\n' );
					outputStream.write( buf.toString( ).getBytes( ) );
					
					buf.delete( 0, buf.length( ) );

					// Data
					while ( iData.next( ) )
					{
						String value = null;

						try
						{
							value = cvsConvertor( ( String ) DataTypeUtil
									.convert( iData.getValue( columnNames[0] ),
											DataType.STRING_TYPE ) );
						}
						catch ( Exception e )
						{
							value = null;
						}

						if ( value != null )
						{
							buf.append( value );
						}

						for ( int i = 1; i < columnNames.length; i++ )
						{
							buf.append( ',' ); //$NON-NLS-1$

							try
							{
								value = cvsConvertor( ( String ) DataTypeUtil
										.convert( iData
												.getValue( columnNames[i] ),
												DataType.STRING_TYPE ) );
							}
							catch ( Exception e )
							{
								value = null;
							}

							if ( value != null )
							{
								buf.append( value );
							}
						}

						buf.append('\n');
						outputStream.write( buf.toString( ).getBytes( ) );
						buf.delete( 0, buf.length( ) );
					}
				}
			}
		}
		catch ( Exception e )
		{
			AxisFault fault = new AxisFault( );
			fault
					.setFaultCode( new QName(
							"ReportEngineService.extractData( )" ) ); //$NON-NLS-1$
			fault.setFaultString( e.getLocalizedMessage( ) );
			throw fault;
		} finally
		{
			if ( iData != null )
			{
				iData.close( );
			}

			if ( result != null )
			{
				result.close( );
			}

			if ( dataTask != null )
			{
				dataTask.close( );
			}
		}
	}

	/**
	 * CSV format convertor. Here is the rule.
	 * 
	 * 1) Fields with embedded commas must be delimited with double-quote
	 * characters. 2) Fields that contain double quote characters must be
	 * surounded by double-quotes, and the embedded double-quotes must each be
	 * represented by a pair of consecutive double quotes. 3) A field that
	 * contains embedded line-breaks must be surounded by double-quotes. 4)
	 * Fields with leading or trailing spaces must be delimited with
	 * double-quote characters.
	 * 
	 * @param value
	 * @return
	 * @throws RemoteException
	 */
	private String cvsConvertor( String value ) throws RemoteException
	{
		if ( value == null )
		{
			return null;
		}

		value = value.replaceAll( "\"", "\"\"" ); //$NON-NLS-1$  //$NON-NLS-2$

		boolean needQuote = false;
		needQuote = ( value.indexOf( ',' ) != -1 )
				|| ( value.indexOf( '"' ) != -1 ) //$NON-NLS-1$ //$NON-NLS-2$
				|| ( value.indexOf( 0x0A ) != -1 )
				|| value.startsWith( " " ) || value.endsWith( " " ); //$NON-NLS-1$ //$NON-NLS-2$
		value = needQuote ? "\"" + value + "\"" : value; //$NON-NLS-1$ //$NON-NLS-2$

		return value;
	}

	/**
	 * Prepare the report parameters.
	 * 
	 * @param request
	 * @param task
	 * @param configVars
	 * @param locale
	 * @return
	 */
	public HashMap parseParameters( HttpServletRequest request,
			IGetParameterDefinitionTask task, Map configVars, Locale locale )
	{
		assert task != null;
		HashMap params = new HashMap( );

		Collection parameterList = task.getParameterDefns( false );
		for ( Iterator iter = parameterList.iterator( ); iter.hasNext( ); )
		{
			IScalarParameterDefn parameterObj = ( IScalarParameterDefn ) iter
					.next( );

			String paramValue = null;
			Object paramValueObj = null;

			// ScalarParameterHandle paramHandle = ( ScalarParameterHandle )
			// parameterObj
			// .getHandle( );
			String paramName = parameterObj.getName( );
			String format = parameterObj.getDisplayFormat( );

			// Get default value from task
			ReportParameterConverter converter = new ReportParameterConverter(
					format, locale );

			if ( ParameterAccessor.isReportParameterExist( request, paramName ) )
			{
				// Get value from http request
				paramValue = ParameterAccessor.getReportParameter( request,
						paramName, paramValue );
				paramValueObj = converter.parse( paramValue, parameterObj
						.getDataType( ) );
			} else if ( ParameterAccessor.isDesigner( request )
					&& configVars.containsKey( paramName ) )
			{
				// Get value from test config
				String configValue = ( String ) configVars.get( paramName );
				ReportParameterConverter cfgConverter = new ReportParameterConverter(
						format, Locale.US );
				paramValueObj = cfgConverter.parse( configValue, parameterObj
						.getDataType( ) );
			} else
			{
				paramValueObj = task.getDefaultValue( parameterObj.getName( ) );
			}

			params.put( paramName, paramValueObj );
		}

		return params;
	}

	/**
	 * Check whether missing parameter or not.
	 * 
	 * @param task
	 * @param parameters
	 * @return
	 */
	public boolean validateParameters( IGetParameterDefinitionTask task,
			Map parameters )
	{
		assert task != null;
		assert parameters != null;

		boolean missingParameter = false;

		Collection parameterList = task.getParameterDefns( false );
		for ( Iterator iter = parameterList.iterator( ); iter.hasNext( ); )
		{
			IScalarParameterDefn parameterObj = ( IScalarParameterDefn ) iter
					.next( );
			// ScalarParameterHandle paramHandle = ( ScalarParameterHandle )
			// parameterObj
			// .getHandle( );

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

			if ( IScalarParameterDefn.TYPE_STRING == parameterObj.getDataType( ) )
			{
				String parameterStringValue = ( String ) parameterValue;
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
}
