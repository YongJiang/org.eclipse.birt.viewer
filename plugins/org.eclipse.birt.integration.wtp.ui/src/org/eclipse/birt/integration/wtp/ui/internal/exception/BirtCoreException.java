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

package org.eclipse.birt.integration.wtp.ui.internal.exception;

import org.eclipse.birt.integration.wtp.ui.BirtWTPUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Implement CoreException
 * 
 */
public class BirtCoreException extends CoreException
{

	/**
	 * Serial Version ID
	 */
	private static final long serialVersionUID = -5114000549371051902L;

	/**
	 * Default Constructor
	 * 
	 * @param status
	 */
	public BirtCoreException( IStatus status )
	{
		super( status );
	}

	/**
	 * Cast CoreException with message and exception
	 * 
	 * @param message
	 * @param e
	 */
	public static CoreException getException( String message, Exception e )
	{
		if ( message == null )
			message = e.getMessage( );

		Status status = new Status( IStatus.ERROR, BirtWTPUIPlugin.PLUGIN_ID,
				IStatus.ERROR, message, e );

		return new CoreException( status );
	}

}
