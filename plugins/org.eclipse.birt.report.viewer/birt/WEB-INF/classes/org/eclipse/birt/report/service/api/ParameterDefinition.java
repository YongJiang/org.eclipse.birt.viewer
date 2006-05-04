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

package org.eclipse.birt.report.service.api;

import java.util.Collection;

/**
 * Viewer representation of a parameter definition
 * 
 * TODO: Add more javadoc
 * 
 */
public class ParameterDefinition
{

	// TODO: These are copied from IScalarParameterDefinition...
	public static final int TEXT_BOX = 0;

	public static final int LIST_BOX = 1;

	public static final int RADIO_BUTTON = 2;

	public static final int CHECK_BOX = 3;

	public static final int AUTO = 0;

	public static final int LEFT = 1;

	public static final int CENTER = 2;

	public static final int RIGHT = 3;

	public static final int TYPE_ANY = 0;

	public static final int TYPE_STRING = 1;

	public static final int TYPE_FLOAT = 2;

	public static final int TYPE_DECIMAL = 3;

	public static final int TYPE_DATE_TIME = 4;

	public static final int TYPE_BOOLEAN = 5;

	public static final int SELECTION_LIST_NONE = 0;

	public static final int SELECTION_LIST_DYNAMIC = 1;

	public static final int SELECTION_LIST_STATIC = 2;

	private String name;

	private String pattern;

	private String displayFormat;

	private String displayName;

	private String helpText;

	private String promptText;

	private int dataType;

	private int controlType;

	private boolean hidden;

	private boolean allowNull;

	private boolean allowBlank;

	private boolean mustMatch;

	private boolean concealValue;

	private ParameterGroupDefinition group;

	private Collection selectionList;

	public ParameterDefinition( String name, String pattern,
			String displayFormat, String displayName, String helpText,
			String promptText, int dataType, int controlType, boolean hidden,
			boolean allowNull, boolean allowBlank, boolean mustMatch,
			boolean concealValue, ParameterGroupDefinition group,
			Collection selectionList )
	{
		this.name = name;
		this.pattern = pattern;
		this.displayFormat = displayFormat;
		this.displayName = displayName;
		this.helpText = helpText;
		this.promptText = promptText;
		this.dataType = dataType;
		this.controlType = controlType;
		this.hidden = hidden;
		this.allowNull = allowNull;
		this.allowBlank = allowBlank;
		this.mustMatch = mustMatch;
		this.concealValue = concealValue;
		this.group = group;
		this.selectionList = selectionList;
	}

	public String getName( )
	{
		return name;
	}

	public String getPattern( )
	{
		return pattern;
	}

	public String getDisplayFormat( )
	{
		return displayFormat;
	}

	public String getDisplayName( )
	{
		return displayName;
	}

	public String getHelpText( )
	{
		return helpText;
	}

	public String getPromptText( )
	{
		return promptText;
	}

	public int getDataType( )
	{
		return dataType;
	}

	public int getControlType( )
	{
		return controlType;
	}

	public boolean isHidden( )
	{
		return hidden;
	}

	public boolean allowNull( )
	{
		return allowNull;
	}

	public boolean allowBlank( )
	{
		return allowBlank;
	}

	public boolean mustMatch( )
	{
		return mustMatch;
	}

	public boolean concealValue( )
	{
		return concealValue;
	}

	public ParameterGroupDefinition getGroup( )
	{
		return group;
	}

	public Collection getSelectionList( )
	{
		return selectionList;
	}

	public boolean equals( Object obj )
	{
		if ( name == null || !( obj instanceof ParameterDefinition ) )
			return false;
		ParameterDefinition other = ( ParameterDefinition ) obj;
		return name.equals( other.getName() );
	}

	public int hashCode( )
	{
		if ( name == null )
			return 0;
		return name.hashCode( );
	}

}
