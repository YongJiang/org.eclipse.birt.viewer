package org.eclipse.birt.report.service.actionhandler;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.servlet.ServletOutputStream;
import javax.xml.namespace.QName;

import org.apache.axis.AxisFault;
import org.eclipse.birt.report.context.IContext;
import org.eclipse.birt.report.context.ViewerAttributeBean;
import org.eclipse.birt.report.service.BirtReportServiceFactory;
import org.eclipse.birt.report.service.api.IViewerReportService;
import org.eclipse.birt.report.service.api.InputOptions;
import org.eclipse.birt.report.service.api.ReportServiceException;
import org.eclipse.birt.report.soapengine.api.GetUpdatedObjectsResponse;
import org.eclipse.birt.report.soapengine.api.Operation;
import org.eclipse.birt.report.utility.ParameterAccessor;

public class BirtExtractDataActionHandler extends AbstractBaseActionHandler
{

	public BirtExtractDataActionHandler( IContext context, Operation operation,
			GetUpdatedObjectsResponse response )
	{
		super( context, operation, response );
	}

	protected void __execute( ) throws RemoteException
	{
		ViewerAttributeBean attrBean = ( ViewerAttributeBean ) context
				.getBean( );
		String docName = attrBean.getReportDocumentName( );
		String resultSetName = ParameterAccessor.getResultSetName( context
				.getRequest( ) );
		Collection columns = ParameterAccessor.getSelectedColumns( context
				.getRequest( ) );
		Set colSet = new HashSet( );
		colSet.addAll( columns );
		Set filters = Collections.EMPTY_SET;
		Locale locale = attrBean.getLocale( );
		InputOptions options = new InputOptions( );
		options.setOption( InputOptions.OPT_REQUEST, context.getRequest( ) );
		options.setOption( InputOptions.OPT_LOCALE, locale );
		try
		{
			ServletOutputStream out = context.getResponse( ).getOutputStream( );
			getReportService( ).extractResultSet( docName, resultSetName,
					colSet, filters, options, out );
		}
		catch ( ReportServiceException e )
		{
			throwAxisFault( e );
		}
		catch ( IOException e )
		{
			// TODO: Maybe not catch IOException here
			throwAxisFault( e );
		}
	}

	private void throwAxisFault( Exception e ) throws RemoteException
	{
		AxisFault fault = new AxisFault( );
		fault.setFaultCode( new QName(
				"BirtExtractDataActionHandler.execute( )" ) ); //$NON-NLS-1$
		fault.setFaultString( e.getLocalizedMessage( ) ); //$NON-NLS-1$
		throw fault;
	}

	protected IViewerReportService getReportService( )
	{
		return BirtReportServiceFactory.getReportService( );
	}

}
