
package org.eclipse.birt.report.service.actionhandler;

import java.io.ByteArrayOutputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;

import org.apache.axis.AxisFault;
import org.eclipse.birt.report.IBirtConstants;
import org.eclipse.birt.report.context.BaseAttributeBean;
import org.eclipse.birt.report.context.IContext;
import org.eclipse.birt.report.resource.BirtResources;
import org.eclipse.birt.report.resource.ResourceConstants;
import org.eclipse.birt.report.service.api.InputOptions;
import org.eclipse.birt.report.service.api.OutputOptions;
import org.eclipse.birt.report.service.api.ReportServiceException;
import org.eclipse.birt.report.soapengine.api.Data;
import org.eclipse.birt.report.soapengine.api.GetUpdatedObjectsResponse;
import org.eclipse.birt.report.soapengine.api.Operation;
import org.eclipse.birt.report.soapengine.api.Oprand;
import org.eclipse.birt.report.soapengine.api.Page;
import org.eclipse.birt.report.soapengine.api.Update;
import org.eclipse.birt.report.soapengine.api.UpdateContent;
import org.eclipse.birt.report.soapengine.api.UpdateData;

public abstract class AbstractGetPageActionHandler
		extends
			AbstractBaseActionHandler
{

	protected BaseAttributeBean __bean;

	protected String __docName;

	protected long __pageNumber;

	protected long __totalPageNumber;

	protected boolean __isCompleted = true;
	
	protected boolean __useBookmark = false;

	protected String __bookmark;

	protected boolean __svgFlag;

	protected ByteArrayOutputStream __page = null;

	protected ArrayList __activeIds = null;

	/**
	 * 
	 * @param bean
	 * @return
	 */
	abstract protected String __getReportDocument( );

	/**
	 * 
	 * @param docName
	 * @throws RemoteException
	 */
	abstract protected void __checkDocumentExists( ) throws RemoteException;

	/**
	 * 
	 * @param context
	 * @param operation
	 * @param response
	 */
	public AbstractGetPageActionHandler( IContext context, Operation operation,
			GetUpdatedObjectsResponse response )
	{
		super( context, operation, response );
	}

	protected void __execute( ) throws RemoteException
	{
		try
		{
			prepareParameters( );
			doExecution( );
			prepareResponse( );
		}
		catch ( ReportServiceException e )
		{
			AxisFault fault = new AxisFault( );
			fault.setFaultReason( e.getLocalizedMessage( ) );
			throw fault;
		}
	}

	protected void prepareParameters( ) throws ReportServiceException,
			RemoteException
	{
		__bean = context.getBean( );
		__docName = __getReportDocument( );
		__checkDocumentExists( );
			
		// Get total page count.
		InputOptions getPageCountOptions = new InputOptions( );
		getPageCountOptions.setOption( InputOptions.OPT_LOCALE, __bean.getLocale( ) );
		getPageCountOptions.setOption( InputOptions.OPT_RTL, new Boolean( __bean.isRtl( ) ) );
		getPageCountOptions.setOption( InputOptions.OPT_REQUEST, context.getRequest( ) );
		OutputOptions outputOptions = new OutputOptions( );
		InputOptions options = new InputOptions( );
		
		__totalPageNumber = getReportService( ).getPageCount( __docName, getPageCountOptions,
				outputOptions );
		Boolean isCompleted = ( Boolean ) outputOptions.getOption(
				OutputOptions.OPT_REPORT_GENERATION_COMPLETED );
		if ( isCompleted != null )
		{
			__isCompleted = isCompleted.booleanValue( );
		}
		
		__bookmark = getBookmark( operation.getOprand( ), __bean );

		if ( isToc( operation.getOprand( ), __bean ) )
		{
			__bookmark = ( getReportService( ) ).findTocByName( __docName,
					__bookmark, options );

		}
		__pageNumber = getPageNumber( context.getRequest( ), operation
				.getOprand( ), __docName );

		// No valid page number check bookmark from soap message.
		if ( !isValidPageNumber( context.getRequest( ), __pageNumber, __docName ) )
		{
			options.setOption( InputOptions.OPT_REQUEST, context.getRequest( ) );
			__pageNumber = getReportService( ).getPageNumberByBookmark(
					__docName, __bookmark, options );

			if ( !isValidPageNumber( context.getRequest( ), __pageNumber,
					__docName ) )
			{
				AxisFault fault = new AxisFault( );
				fault.setFaultReason( BirtResources.getFormattedString(
						ResourceConstants.ACTION_EXCEPTION_INVALID_BOOKMARK,
						new String[]{__bookmark} ) );
				throw fault;
			}
			__useBookmark = true;
		}

		// Verify the page number again.
		if ( !isValidPageNumber( context.getRequest( ), __pageNumber, __docName ) )
		{
			AxisFault fault = new AxisFault( );
			fault
					.setFaultReason( BirtResources
							.getString( ResourceConstants.ACTION_EXCEPTION_INVALID_PAGE_NUMBER ) );
			throw fault;
		}

		__svgFlag = getSVGFlag( operation.getOprand( ) );
	}

	protected void doExecution( ) throws ReportServiceException,
			RemoteException
	{
		InputOptions options = new InputOptions( );
		options.setOption( InputOptions.OPT_LOCALE, __bean.getLocale( ) );
		options
				.setOption( InputOptions.OPT_RTL, new Boolean( __bean.isRtl( ) ) );
		options.setOption( InputOptions.OPT_REQUEST, context.getRequest( ) );
		options.setOption( InputOptions.OPT_SVG_FLAG, new Boolean( __svgFlag ) );
		options.setOption( InputOptions.OPT_IS_MASTER_PAGE_CONTENT,
				new Boolean( __bean.isMasterPageContent( ) ) );

		__activeIds = new ArrayList( );
		__page = getReportService( ).getPage( __docName, __pageNumber + "", //$NON-NLS-1$
				options, __activeIds );
		
	}

	protected void prepareResponse( ) throws ReportServiceException,
			RemoteException
	{
		// Update instruction for document part.
		UpdateContent content = new UpdateContent( );
		content.setContent( __page.toString( ) );
		content.setTarget( "Document" ); //$NON-NLS-1$
		content.setInitializationId( parseReportId( __activeIds ) );
		if ( __useBookmark )
		{
			content.setBookmark( __bookmark );
		}
		Update updateDocument = new Update( );
		updateDocument.setUpdateContent( content );

		// Update instruction for nav bar.
		UpdateData updateData = new UpdateData( );
		updateData.setTarget( "navigationBar" ); //$NON-NLS-1$
		Page pageObj = new Page( );
		pageObj.setPageNumber( String.valueOf( __pageNumber ) );
		pageObj.setTotalPage( String.valueOf( __totalPageNumber ) );
		Data data = new Data( );
		data.setPage( pageObj );
		updateData.setData( data );
		Update updateNavbar = new Update( );
		updateNavbar.setUpdateData( updateData );

		response.setUpdate( new Update[]{updateDocument, updateNavbar} );
	}
	
	/**
	 * Check whether the page number is valid or not.
	 * 
	 * @param pageNumber
	 * @param document
	 * @return
	 * @throws RemoteException
	 * @throws ReportServiceException
	 */
	protected boolean isValidPageNumber( HttpServletRequest request,
			long pageNumber, String documentName ) throws RemoteException,
			ReportServiceException
	{
		InputOptions options = new InputOptions( );
		options.setOption( InputOptions.OPT_REQUEST, request );
		return pageNumber > 0 && pageNumber <= __totalPageNumber;
	}

	/**
	 * Get page number from incoming soap request.
	 * 
	 * @param params
	 * @param document
	 * @return
	 * @throws RemoteException
	 * @throws ReportServiceException
	 */
	protected long getPageNumber( HttpServletRequest request, Oprand[] params,
			String documentName ) throws RemoteException,
			ReportServiceException
	{
		long pageNumber = -1;
		if ( params != null && params.length > 0 )
		{
			for ( int i = 0; i < params.length; i++ )
			{
				if ( IBirtConstants.OPRAND_PAGENO.equalsIgnoreCase( params[i]
						.getName( ) ) )
				{
					try
					{
						pageNumber = Integer.parseInt( params[i].getValue( ) );
					}
					catch ( NumberFormatException e )
					{
						pageNumber = -1;
					}
					if ( pageNumber <= 0 || pageNumber > __totalPageNumber )
					{
						AxisFault fault = new AxisFault( );
						fault.setFaultCode( new QName(
								"DocumentProcessor.getPageNumber( )" ) ); //$NON-NLS-1$
						fault.setFaultString( BirtResources
								.getString( ResourceConstants.ACTION_EXCEPTION_INVALID_PAGE_NUMBER ) );
						throw fault;
					}

					break;
				}
			}
		}

		return pageNumber;
	}

	/**
	 * Get page number by bookmark.
	 * 
	 * @param params
	 * @param bean
	 * @param document
	 * @return
	 * @throws RemoteException
	 */
	protected String getBookmark( Oprand[] params, BaseAttributeBean bean )
	{
		assert bean != null;

		String bookmark = null;
		if ( params != null && params.length > 0 )
		{
			for ( int i = 0; i < params.length; i++ )
			{
				if ( IBirtConstants.OPRAND_BOOKMARK.equalsIgnoreCase( params[i]
						.getName( ) ) )
				{
					bookmark = params[i].getValue( );
					break;
				}
			}
		}

		// Then use url bookmark.
		if ( bookmark == null || bookmark.length( ) <= 0 )
		{
			bookmark = bean.getBookmark( );
		}

		return bookmark;
	}
}
