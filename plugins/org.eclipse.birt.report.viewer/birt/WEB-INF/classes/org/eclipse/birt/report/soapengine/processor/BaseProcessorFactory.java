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

package org.eclipse.birt.report.soapengine.processor;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import org.apache.axis.AxisFault;
import org.eclipse.birt.report.soapengine.api.ReportIdType;
import org.eclipse.birt.report.soapengine.processor.api.IProcessorFactory;

/**
 * Processor factory class.
 */
public class BaseProcessorFactory implements IProcessorFactory
{
	/**
	 * Processor factory instance.
	 */
	protected static IProcessorFactory instance = null;
	
	/**
	 * Get processor factory instance.
	 * 
	 * @return
	 * @throws MarshalException
	 * @throws ValidationException
	 * @throws UnsupportedEncodingException
	 * @throws FileNotFoundException
	 */
	public static synchronized IProcessorFactory getInstance( ) throws AxisFault
	{
		if ( instance != null )
		{
			return instance;
		}
		
		try
		{ 
			instance = ( IProcessorFactory ) Class.forName( "" )
				.newInstance( ); //$NON-NLS-1$
		}
		catch ( Exception e )
		{
			instance = null;
		}
		
		if ( instance == null )
		{
			instance = new BaseProcessorFactory( );
		}
		
		instance.init( );
		
		return instance;
	}

	/**
	 * Initializes the ERNI config manager instance. Read ERNI_Config.xml
	 * 
	 * @throws UnsupportedEncodingException
	 * @throws ValidationException
	 * @throws MarshalException
	 * @throws FileNotFoundException 
	 */
	public void init( ) throws AxisFault
	{
	}

	public IComponentProcessor createProcessor( String category, ReportIdType component )
	{
		if ( component != null )
		{
			if ( ReportIdType._Document.equalsIgnoreCase( component.getValue( ) ) )
			{
				return new DocumentProcessor( );
			}
			else if ( ReportIdType._Table.equalsIgnoreCase( component.getValue( ) ) )
			{
				return new TableProcessor( );
			}
		}
		
		return null;
	}
}