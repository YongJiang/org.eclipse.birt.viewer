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

package org.eclipse.birt.report.presentation.aggregation.dialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.birt.report.context.ViewerAttributeBean;
import org.eclipse.birt.report.presentation.aggregation.IFragment;
import org.eclipse.birt.report.presentation.aggregation.parameter.CheckboxParameterFragment;
import org.eclipse.birt.report.presentation.aggregation.parameter.ComboBoxParameterFragment;
import org.eclipse.birt.report.presentation.aggregation.parameter.ParameterGroupFragment;
import org.eclipse.birt.report.presentation.aggregation.parameter.RadioButtonParameterFragment;
import org.eclipse.birt.report.presentation.aggregation.parameter.TextBoxParameterFragment;
import org.eclipse.birt.report.service.BirtViewerReportDesignHandle;
import org.eclipse.birt.report.service.api.IViewerReportDesignHandle;
import org.eclipse.birt.report.service.api.IViewerReportService;
import org.eclipse.birt.report.service.api.InputOptions;
import org.eclipse.birt.report.service.api.ParameterDefinition;
import org.eclipse.birt.report.service.api.ParameterGroupDefinition;
import org.eclipse.birt.report.service.api.ReportServiceException;
import org.eclipse.birt.report.utility.ParameterAccessor;

/**
 * Fragment help rendering parameter page in side bar.
 * <p>
 * 
 * @see BaseFragment
 */
public class ParameterDialogFragment extends BaseDialogFragment
{

	/**
	 * Get unique id of the corresponding UI gesture.
	 * 
	 * @return id
	 */
	public String getClientId( )
	{
		return "parameterDialog"; //$NON-NLS-1$
	}

	/**
	 * Get name of the corresponding UI gesture.
	 * 
	 * @return id
	 */
	public String getClientName( )
	{
		return "Parameter"; //$NON-NLS-1$
	}

	protected void doService( HttpServletRequest request,
			HttpServletResponse response ) throws ServletException, IOException
	{
		Collection fragments = new ArrayList( );
		IViewerReportService service = getReportService( );
		Collection parameters = null;
		InputOptions options = new InputOptions( );
		options.setOption( InputOptions.OPT_REQUEST, request );

		ViewerAttributeBean attrBean = (ViewerAttributeBean) request
				.getAttribute( "attributeBean" ); //$NON-NLS-1$		
		assert attrBean != null;
		
		try
		{
			parameters = service
					.getParameterDefinitions( attrBean.getReportDesignHandle( ), options, true );
		}
		catch ( ReportServiceException e )
		{
			// TODO What to do here???
			e.printStackTrace( );
		}

		if ( parameters != null )
		{
			Iterator iParameters = parameters.iterator( );
			while ( iParameters != null && iParameters.hasNext( ) )
			{
				Object parameter = iParameters.next( );
				if ( parameter == null )
				{
					continue;
				}

				IFragment fragment = null;
				if ( parameter instanceof ParameterGroupDefinition )
				{
					fragment = new ParameterGroupFragment(
							(ParameterGroupDefinition) parameter );
				}
				else if ( parameter instanceof ParameterDefinition )
				{
					ParameterDefinition scalarParameter = (ParameterDefinition) parameter;

					if ( !scalarParameter.isHidden( ) )
					{
						switch ( scalarParameter.getControlType( ) )
						{
							case ParameterDefinition.TEXT_BOX :
							{
								fragment = new TextBoxParameterFragment(
										scalarParameter );
								break;
							}
							case ParameterDefinition.LIST_BOX :
							{
								fragment = new ComboBoxParameterFragment(
										scalarParameter );
								break;
							}
							case ParameterDefinition.RADIO_BUTTON :
							{
								fragment = new RadioButtonParameterFragment(
										scalarParameter );
								break;
							}
							case ParameterDefinition.CHECK_BOX :
							{
								fragment = new CheckboxParameterFragment(
										scalarParameter );
								break;
							}
						}
					}
				}

				if ( fragment != null )
				{
					fragment.setJSPRootPath( JSPRootPath );
					fragments.add( fragment );
				}
			}
		}

		request.setAttribute( "fragments", fragments ); //$NON-NLS-1$
	}
}