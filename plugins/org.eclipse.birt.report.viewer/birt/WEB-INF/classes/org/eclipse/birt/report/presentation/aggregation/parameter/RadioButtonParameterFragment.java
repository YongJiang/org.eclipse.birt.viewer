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

package org.eclipse.birt.report.presentation.aggregation.parameter;

import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.birt.report.context.ScalarParameterBean;
import org.eclipse.birt.report.engine.api.IGetParameterDefinitionTask;
import org.eclipse.birt.report.engine.api.IParameterSelectionChoice;
import org.eclipse.birt.report.engine.api.ReportParameterConverter;
import org.eclipse.birt.report.model.api.ScalarParameterHandle;
import org.eclipse.birt.report.utility.ParameterAccessor;

/**
 * Fragment help rendering scalar parameter.
 * <p>
 * 
 * @see org.eclipse.birt.report.viewer.aggregation.BaseFragment
 */
public class RadioButtonParameterFragment extends ScalarParameterFragment
{

	/**
	 * Protected constructor.
	 * 
	 * @param parameter
	 *            parameter definition reference.
	 */
	public RadioButtonParameterFragment( ScalarParameterHandle parameter )
	{
		super( parameter );
	}

	protected void prepareParameterBean( HttpServletRequest request,
			IGetParameterDefinitionTask task, ScalarParameterBean parameterBean, String format, Locale locale )
	{
		Collection selectionList = task.getSelectionList( parameter.getName( ) );

		if ( selectionList != null )
		{
			ReportParameterConverter converter = new ReportParameterConverter( format,
					locale );

			for ( Iterator iter = selectionList.iterator( ); iter.hasNext( ); )
			{
				IParameterSelectionChoice selectionItem = (IParameterSelectionChoice) iter.next( );

				String value = converter.format( selectionItem.getValue( ) );
				String label = selectionItem.getLabel( );
				label = ( label == null || label.length( ) <= 0 ) ? value
						: label;
				label = ParameterAccessor.htmlEncode( label );

				parameterBean.getSelectionList( ).add( label );
				parameterBean.getSelectionTable( ).put( label, value );
			}
		}
	}
}