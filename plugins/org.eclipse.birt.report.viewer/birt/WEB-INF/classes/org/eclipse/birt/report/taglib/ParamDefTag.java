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

package org.eclipse.birt.report.taglib;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.TagSupport;

import org.eclipse.birt.report.engine.api.IScalarParameterDefn;
import org.eclipse.birt.report.model.api.util.ParameterValidationUtil;
import org.eclipse.birt.report.resource.BirtResources;
import org.eclipse.birt.report.resource.ResourceConstants;
import org.eclipse.birt.report.service.BirtReportServiceFactory;
import org.eclipse.birt.report.service.ParameterDataTypeConverter;
import org.eclipse.birt.report.service.api.IViewerReportDesignHandle;
import org.eclipse.birt.report.service.api.InputOptions;
import org.eclipse.birt.report.service.api.ParameterDefinition;
import org.eclipse.birt.report.service.api.ParameterGroupDefinition;
import org.eclipse.birt.report.service.api.ParameterSelectionChoice;
import org.eclipse.birt.report.service.api.ReportServiceException;
import org.eclipse.birt.report.taglib.component.ParamDefField;
import org.eclipse.birt.report.taglib.component.ViewerField;
import org.eclipse.birt.report.taglib.util.BirtTagUtil;
import org.eclipse.birt.report.utility.BirtUtility;
import org.eclipse.birt.report.utility.DataUtil;
import org.eclipse.birt.report.utility.ParameterAccessor;

/**
 * This tag is used to generate html code for report parameter.
 * 
 */
public class ParamDefTag extends BodyTagSupport
{

	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = -1255870121526790060L;

	/**
	 * Parameter Definiation
	 */
	private ParamDefField param;

	/**
	 * Associated parameterPage tag object
	 */
	private RequesterTag requesterTag;

	/**
	 * Viewer supported attributes
	 */
	private ViewerField viewer;

	/**
	 * Current report parameter definition
	 */
	private ParameterDefinition paramDef;

	/**
	 * Input Options information
	 */
	private InputOptions options;

	/**
	 * Current locale setting
	 */
	private Locale locale;

	/**
	 * Current parameter format pattern
	 */
	private String pattern;

	/**
	 * value string
	 */
	private String valueString;

	/**
	 * display text string
	 */
	private String displayTextString;

	/**
	 * parameter group object name
	 */
	private String groupObjName;

	/**
	 * Whether imported js/style files
	 */
	private static final String IMPORT_FILES_ATTR = "IMPORT_FILES_FLAG"; //$NON-NLS-1$

	/**
	 * Initialize pageContext
	 * 
	 * @see javax.servlet.jsp.tagext.TagSupport#setPageContext(javax.servlet.jsp.PageContext)
	 */
	public void setPageContext( PageContext context )
	{
		super.setPageContext( context );
		param = new ParamDefField( );
	}

	/**
	 * When reach the end tag, fire this operation
	 * 
	 * @see javax.servlet.jsp.tagext.BodyTagSupport#doEndTag()
	 */
	public int doEndTag( ) throws JspException
	{
		try
		{
			if ( __validate( ) )
			{
				// included in parameterpage tag
				this.requesterTag = (RequesterTag) TagSupport
						.findAncestorWithClass( this, RequesterTag.class );
				if ( requesterTag != null )
				{
					this.viewer = requesterTag.viewer;
					if ( this.viewer.isCustom( ) )
					{
						__beforeEndTag( );
						__process( );
					}
				}
			}
		}
		catch ( Exception e )
		{
			__handleException( e );
		}
		return super.doEndTag( );
	}

	/**
	 * validate the tag
	 * 
	 * @return
	 * @throws Exception
	 */
	protected boolean __validate( ) throws Exception
	{
		if ( !param.validate( ) )
			return false;

		// validate parameter id if valid
		Pattern p = Pattern.compile( "^\\w+$" ); //$NON-NLS-1$
		Matcher m = p.matcher( param.getId( ) );
		if ( !m.find( ) )
		{
			throw new JspTagException( BirtResources
					.getMessage( ResourceConstants.TAGLIB_INVALID_ATTR_ID ) );
		}

		// validate parameter id if unique
		if ( pageContext.findAttribute( param.getId( ) ) != null )
		{
			throw new JspTagException( BirtResources
					.getMessage( ResourceConstants.TAGLIB_ATTR_ID_DUPLICATE ) );
		}

		return true;
	}

	/**
	 * Handle event before doEndTag
	 */
	protected void __beforeEndTag( )
	{
		// Save parameter id
		pageContext.setAttribute( param.getId( ), param.getName( ) );
	}

	/**
	 * process tag function
	 * 
	 * @throws Exception
	 */
	protected void __process( ) throws Exception
	{
		if ( viewer == null )
			return;

		HttpServletRequest request = (HttpServletRequest) pageContext
				.getRequest( );
		this.locale = BirtTagUtil.getLocale( request, viewer.getLocale( ) );

		// Create Input Options
		this.options = new InputOptions( );
		options.setOption( InputOptions.OPT_REQUEST, request );
		options.setOption( InputOptions.OPT_LOCALE, this.locale );
		options.setOption( InputOptions.OPT_RTL, Boolean.valueOf( viewer
				.getRtl( ) ) );

		// get report parameter definition list
		Collection paramDefList = viewer.getParameterDefList( );
		if ( paramDefList == null )
		{
			// initialize engine context
			BirtReportServiceFactory.getReportService( ).setContext(
					pageContext.getServletContext( ), options );

			// get report design handle
			IViewerReportDesignHandle designHandle = BirtTagUtil
					.getDesignHandle( request, viewer );
			viewer.setReportDesignHandle( designHandle );

			paramDefList = BirtReportServiceFactory.getReportService( )
					.getParameterDefinitions( designHandle, options, false );
			viewer.setParameterDefList( paramDefList );
		}

		// find current parameter definition object
		this.paramDef = BirtUtility.findParameterDefinition( paramDefList,
				param.getName( ) );
		if ( paramDef == null )
			return;

		// data type
		String dataType = ParameterDataTypeConverter.ConvertDataType( paramDef
				.getDataType( ) );

		// pattern format
		this.pattern = param.getPattern( );
		if ( this.pattern == null )
			this.pattern = paramDef.getPattern( );

		boolean isLocale = false;
		if ( "true".equalsIgnoreCase( param.getIsLocale( ) ) ) //$NON-NLS-1$
			isLocale = true;

		// handle parameter value
		if ( param.getValue( ) != null && param.getValue( ) instanceof String )
		{
			// convert parameter value to object
			Object valueObj = DataUtil.validate( dataType, this.pattern,
					(String) param.getValue( ), locale, isLocale );
			param.setValue( valueObj );
		}

		if ( param.getValue( ) == null )
		{
			Object defaultValue = BirtReportServiceFactory.getReportService( )
					.getParameterDefaultValue( viewer.getReportDesignHandle( ),
							param.getName( ), options );
			param.setValue( defaultValue );
		}

		// handle value string
		this.valueString = DataUtil.getDisplayValue( param.getValue( ) );
		if ( this.valueString == null )
			this.valueString = ""; //$NON-NLS-1$

		// handle parameter display text
		this.displayTextString = param.getDisplayText( );
		if ( this.displayTextString == null )
			this.displayTextString = ParameterValidationUtil.getDisplayValue(
					dataType, this.pattern, param.getValue( ), locale );
		if ( this.displayTextString == null )
			this.displayTextString = ""; //$NON-NLS-1$

		// handle title
		if ( param.getTitle( ) == null )
			param.setTitle( this.displayTextString );

		// cache parameter value
		requesterTag.addParameter( param.getName( ), param.getValue( ) );

		if ( paramDef.isHidden( ) )
		{
			// handle hidden parameter
			__handleHidden( );
		}
		else
		{
			// handle parameter section output
			switch ( paramDef.getControlType( ) )
			{
				case IScalarParameterDefn.TEXT_BOX :
					__handleTextBox( );
					break;
				case IScalarParameterDefn.LIST_BOX :
					__handleListBox( );
					break;
				case IScalarParameterDefn.RADIO_BUTTON :
					__handleRadioButton( );
					break;
				case IScalarParameterDefn.CHECK_BOX :
					__handleCheckBox( );
					break;
				default :
					break;
			}
		}
	}

	/**
	 * Handle output hidden type parameter
	 * 
	 * @throws Exception
	 */
	protected void __handleHidden( ) throws Exception
	{
		JspWriter writer = pageContext.getOut( );

		String encParamId = ParameterAccessor.htmlEncode( param.getId( ) );
		String encParamName = ParameterAccessor.htmlEncode( param.getName( ) );

		boolean isNullValue = param.getValue( ) == null;

		// parameter hidden value control
		writer.write( "<input type=\"hidden\" " ); //$NON-NLS-1$
		writer.write( " id=\"" + encParamId + "\" " ); //$NON-NLS-1$//$NON-NLS-2$
		if ( !isNullValue )
		{
			writer.write( " name=\"" + encParamName + "\" " ); //$NON-NLS-1$ //$NON-NLS-2$
			writer
					.write( " value=\"" + ParameterAccessor.htmlEncode( this.valueString ) + "\" " ); //$NON-NLS-1$ //$NON-NLS-2$
		}
		writer.write( " >\n" ); //$NON-NLS-1$

		// display text hidden object
		String displayTextId = encParamId + "_displayText"; //$NON-NLS-1$
		String displayTextName = ParameterAccessor.PREFIX_DISPLAY_TEXT
				+ encParamName;
		writer.write( "<input type=\"hidden\" " ); //$NON-NLS-1$
		writer.write( " id=\"" + displayTextId + "\" " ); //$NON-NLS-1$//$NON-NLS-2$
		if ( !isNullValue )
		{
			writer.write( " name=\"" + displayTextName + "\" " ); //$NON-NLS-1$ //$NON-NLS-2$
			writer
					.write( " value=\"" + ParameterAccessor.htmlEncode( this.displayTextString ) + "\" " ); //$NON-NLS-1$ //$NON-NLS-2$
		}
		writer.write( " >\n" ); //$NON-NLS-1$
	}

	/**
	 * Handle output general definitions for a control
	 * 
	 * @throws Exception
	 */
	protected void __handleGeneralDefinition( ) throws Exception
	{
		JspWriter writer = pageContext.getOut( );

		if ( param.getTitle( ) != null )
			writer.write( " title=\"" + param.getTitle( ) + "\" " ); //$NON-NLS-1$ //$NON-NLS-2$

		if ( param.getCssClass( ) != null )
			writer.write( " class=\"" + param.getCssClass( ) + "\" " ); //$NON-NLS-1$ //$NON-NLS-2$

		if ( param.getStyle( ) != null )
			writer.write( " style=\"" + param.getStyle( ) + "\" " ); //$NON-NLS-1$//$NON-NLS-2$
	}

	/**
	 * Handle output Text Box type parameter
	 * 
	 * @throws Exception
	 */
	protected void __handleTextBox( ) throws Exception
	{
		JspWriter writer = pageContext.getOut( );

		String encParamId = ParameterAccessor.htmlEncode( param.getId( ) );
		String encParamName = ParameterAccessor.htmlEncode( param.getName( ) );

		boolean isNullValue = param.getValue( ) == null;

		// display text hidden object
		String displayTextId = encParamId + "_displayText"; //$NON-NLS-1$
		String displayTextName = ParameterAccessor.PREFIX_DISPLAY_TEXT
				+ encParamName;
		writer.write( "<input type=\"hidden\" " ); //$NON-NLS-1$
		writer.write( " id=\"" + displayTextId + "\" " ); //$NON-NLS-1$//$NON-NLS-2$
		if ( paramDef.isRequired( ) || !isNullValue )
		{
			writer.write( " name=\"" + displayTextName + "\" " ); //$NON-NLS-1$ //$NON-NLS-2$
			writer
					.write( " value=\"" + ParameterAccessor.htmlEncode( this.displayTextString ) + "\" " ); //$NON-NLS-1$ //$NON-NLS-2$
		}
		writer.write( " >\n" ); //$NON-NLS-1$

		// parameter value hidden object
		String valueId = encParamId + "_value"; //$NON-NLS-1$
		writer.write( "<input type=\"hidden\" " ); //$NON-NLS-1$
		writer.write( " id=\"" + valueId + "\" " ); //$NON-NLS-1$//$NON-NLS-2$
		writer.write( " name=\"" + encParamName + "\" " ); //$NON-NLS-1$ //$NON-NLS-2$
		writer
				.write( " value=\"" + ParameterAccessor.htmlEncode( this.valueString ) + "\" " ); //$NON-NLS-1$ //$NON-NLS-2$
		writer.write( " >\n" ); //$NON-NLS-1$

		// isLocale hidden object
		String isLocaleId = encParamId + "_islocale"; //$NON-NLS-1$
		writer
				.write( "<input type=\"hidden\" id=\"" + isLocaleId + "\" value=\"" //$NON-NLS-1$ //$NON-NLS-2$
						+ encParamName + "\" >\n" ); //$NON-NLS-1$

		// set parameter pattern format
		if ( param.getPattern( ) != null )
		{
			writer
					.write( "<input type = 'hidden' name=\"" + encParamName + "_format\" \n" ); //$NON-NLS-1$ //$NON-NLS-2$
			writer
					.write( " value=\"" + ParameterAccessor.htmlEncode( param.getPattern( ) ) + "\">\n" ); //$NON-NLS-1$//$NON-NLS-2$
		}

		// onchange script
		writer.write( "\n<script language=\"JavaScript\">\n" ); //$NON-NLS-1$
		writer.write( "function handleParam" + encParamId + "( )\n" ); //$NON-NLS-1$ //$NON-NLS-2$
		writer.write( "{\n" ); //$NON-NLS-1$
		writer.write( "var inputCtl = document.getElementById(\"" + encParamId //$NON-NLS-1$
				+ "\");\n" ); //$NON-NLS-1$
		writer.write( "var valCtl = document.getElementById(\"" + valueId //$NON-NLS-1$
				+ "\");\n" ); //$NON-NLS-1$
		writer.write( "var displayCtl = document.getElementById(\"" //$NON-NLS-1$
				+ displayTextId + "\");\n" ); //$NON-NLS-1$
		writer.write( "var localeCtl = document.getElementById(\"" //$NON-NLS-1$
				+ isLocaleId + "\");\n" ); //$NON-NLS-1$
		writer.write( "displayCtl.value=inputCtl.value;\n" ); //$NON-NLS-1$
		writer.write( "valCtl.value=inputCtl.value;\n" ); //$NON-NLS-1$
		writer
				.write( "localeCtl.name='" + ParameterAccessor.PARAM_ISLOCALE + "';\n" ); //$NON-NLS-1$ //$NON-NLS-2$
		writer.write( "}\n" ); //$NON-NLS-1$
		writer.write( "</script>\n" ); //$NON-NLS-1$

		String controlType = paramDef.concealValue( ) ? "PASSWORD" : "TEXT"; //$NON-NLS-1$ //$NON-NLS-2$
		if ( paramDef.isRequired( ) )
		{
			writer.write( "<input type=\"" + controlType + "\" " ); //$NON-NLS-1$ //$NON-NLS-2$
			writer.write( " id=\"" + encParamId + "\" " ); //$NON-NLS-1$//$NON-NLS-2$
			__handleGeneralDefinition( );
			writer
					.write( " value=\"" + ParameterAccessor.htmlEncode( this.displayTextString ) + "\" " ); //$NON-NLS-1$ //$NON-NLS-2$ 
			writer.write( " onchange=\"handleParam" + encParamId + "( )\"" ); //$NON-NLS-1$ //$NON-NLS-2$
			writer.write( " >\n" ); //$NON-NLS-1$			
		}
		else
		{
			String nullValueId = encParamId + "_null"; //$NON-NLS-1$
			String radioTextValueId = encParamId + "_radio_input"; //$NON-NLS-1$
			String radioNullValueId = encParamId + "_radio_null"; //$NON-NLS-1$

			// onclick script
			writer.write( "\n<script language=\"JavaScript\">\n" ); //$NON-NLS-1$
			writer.write( "function switchParam" + encParamId + "( flag )\n" ); //$NON-NLS-1$ //$NON-NLS-2$
			writer.write( "{\n" ); //$NON-NLS-1$
			writer
					.write( "var inputCtl = document.getElementById(\"" + encParamId //$NON-NLS-1$
							+ "\");\n" ); //$NON-NLS-1$
			writer.write( "var displayCtl = document.getElementById(\"" //$NON-NLS-1$
					+ displayTextId + "\");\n" ); //$NON-NLS-1$
			writer
					.write( "var nullCtl = document.getElementById(\"" + nullValueId //$NON-NLS-1$
							+ "\");\n" ); //$NON-NLS-1$
			writer
					.write( "var radioTextCtl = document.getElementById(\"" + radioTextValueId //$NON-NLS-1$
							+ "\");\n" ); //$NON-NLS-1$
			writer
					.write( "var radioNullCtl = document.getElementById(\"" + radioNullValueId //$NON-NLS-1$
							+ "\");\n" ); //$NON-NLS-1$
			writer.write( "if( flag ) \n" ); //$NON-NLS-1$
			writer.write( "{\n" ); //$NON-NLS-1$
			writer.write( "	radioTextCtl.checked=true;\n" ); //$NON-NLS-1$
			writer.write( "	radioNullCtl.checked=false;\n" ); //$NON-NLS-1$
			writer.write( "	inputCtl.disabled=false;\n" ); //$NON-NLS-1$		
			writer.write( "	nullCtl.name='';\n" ); //$NON-NLS-1$
			writer.write( "	displayCtl.name='" + displayTextName + "';\n" ); //$NON-NLS-1$ //$NON-NLS-2$
			writer.write( "}\n" ); //$NON-NLS-1$
			writer.write( "else\n" ); //$NON-NLS-1$
			writer.write( "{\n" ); //$NON-NLS-1$
			writer.write( "	radioTextCtl.checked=false;\n" ); //$NON-NLS-1$
			writer.write( "	radioNullCtl.checked=true;\n" ); //$NON-NLS-1$
			writer.write( "	inputCtl.disabled=true;\n" ); //$NON-NLS-1$		
			writer
					.write( "	nullCtl.name='" + ParameterAccessor.PARAM_ISNULL + "';\n" ); //$NON-NLS-1$ //$NON-NLS-2$
			writer.write( "	displayCtl.name='';\n" ); //$NON-NLS-1$
			writer.write( "}\n" ); //$NON-NLS-1$			
			writer.write( "}\n" ); //$NON-NLS-1$
			writer.write( "</script>\n" ); //$NON-NLS-1$

			// Null Value hidden object
			writer.write( "<input type=\"hidden\" value=\"" //$NON-NLS-1$
					+ encParamName + "\" id=\"" + nullValueId + "\"" ); //$NON-NLS-1$ //$NON-NLS-2$
			if ( isNullValue )
				writer.write( " name=\"" //$NON-NLS-1$
						+ ParameterAccessor.PARAM_ISNULL + "\"" ); //$NON-NLS-1$
			writer.write( " >\n" ); //$NON-NLS-1$

			writer
					.write( "<input type=\"radio\" id=\"" + radioTextValueId + "\" " ); //$NON-NLS-1$//$NON-NLS-2$
			writer.write( " onclick=\"switchParam" + encParamId + "( true )\"" ); //$NON-NLS-1$ //$NON-NLS-2$
			if ( !isNullValue )
				writer.write( " checked " ); //$NON-NLS-1$
			writer.write( " >\n" ); //$NON-NLS-1$

			writer.write( "<input type=\"" + controlType + "\" " ); //$NON-NLS-1$ //$NON-NLS-2$
			writer.write( " id=\"" + encParamId + "\" " ); //$NON-NLS-1$//$NON-NLS-2$
			__handleGeneralDefinition( );
			writer
					.write( " value=\"" + ParameterAccessor.htmlEncode( this.displayTextString ) + "\" " ); //$NON-NLS-1$ //$NON-NLS-2$ 
			writer.write( " onchange=\"handleParam" + encParamId + "( )\"" ); //$NON-NLS-1$ //$NON-NLS-2$
			if ( isNullValue )
				writer.write( " disabled = 'true' " ); //$NON-NLS-1$
			writer.write( " >\n" ); //$NON-NLS-1$

			writer
					.write( "<input type=\"radio\" id=\"" + radioNullValueId + "\" " ); //$NON-NLS-1$//$NON-NLS-2$
			writer
					.write( " onclick=\"switchParam" + encParamId + "( false )\"" ); //$NON-NLS-1$ //$NON-NLS-2$
			if ( isNullValue )
				writer.write( " checked " ); //$NON-NLS-1$
			writer.write( " >" ); //$NON-NLS-1$
			writer
					.write( "<label id=\"" + ( radioNullValueId + "_label" ) + "\"" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			writer.write( " title=\"" + ITagConstants.NULL_VALUE + "\"" ); //$NON-NLS-1$ //$NON-NLS-2$
			writer.write( " for=\"" + radioNullValueId + "\">" ); //$NON-NLS-1$//$NON-NLS-2$
			writer.write( ITagConstants.NULL_VALUE );
			writer.write( "</label>" ); //$NON-NLS-1$
			writer.write( "</input>\n" ); //$NON-NLS-1$
		}
	}

	/**
	 * Handle output List Box type parameter
	 * 
	 * @throws Exception
	 */
	protected void __handleListBox( ) throws Exception
	{
		if ( paramDef.getGroup( ) != null && paramDef.getGroup( ).cascade( ) )
		{
			JspWriter writer = pageContext.getOut( );

			// Only import necessary files once.
			if ( pageContext.findAttribute( IMPORT_FILES_ATTR ) == null )
			{
				String baseURL = "/webcontent/"; //$NON-NLS-1$
				if ( viewer.getBaseURL( ) != null )
				{
					baseURL = viewer.getBaseURL( ) + baseURL;
				}
				else
				{
					baseURL = ( (HttpServletRequest) pageContext.getRequest( ) )
							.getContextPath( )
							+ baseURL;
				}

				// style files
				writer.write( "\n<LINK REL=\"stylesheet\" HREF=\"" + baseURL //$NON-NLS-1$
						+ "birt/styles/style.css\" TYPE=\"text/css\">\n" ); //$NON-NLS-1$

				// lib files
				writer
						.write( "\n<script src=\"" //$NON-NLS-1$
								+ baseURL
								+ "birt/ajax/lib/prototype.js\" type=\"text/javascript\"></script>\n" ); //$NON-NLS-1$

				writer
						.write( "<script src=\"" //$NON-NLS-1$
								+ baseURL
								+ "birt/ajax/utility/Debug.js\" type=\"text/javascript\"></script>\n" ); //$NON-NLS-1$
				writer
						.write( "<script src=\"" //$NON-NLS-1$
								+ baseURL
								+ "birt/ajax/utility/Constants.js\" type=\"text/javascript\"></script>\n" ); //$NON-NLS-1$
				writer
						.write( "<script src=\"" //$NON-NLS-1$
								+ baseURL
								+ "birt/ajax/utility/BirtUtility.js\" type=\"text/javascript\"></script>\n" ); //$NON-NLS-1$
				writer
						.write( "<script src=\"" //$NON-NLS-1$
								+ baseURL
								+ "birt/ajax/utility/BirtPosition.js\" type=\"text/javascript\"></script>\n" ); //$NON-NLS-1$

				writer
						.write( "<script src=\"" //$NON-NLS-1$
								+ baseURL
								+ "birt/ajax/core/BirtSoapRequest.js\" type=\"text/javascript\"></script>\n" ); //$NON-NLS-1$
				writer
						.write( "<script src=\"" //$NON-NLS-1$
								+ baseURL
								+ "birt/ajax/core/BirtEvent.js\" type=\"text/javascript\"></script>\n" ); //$NON-NLS-1$

				writer
						.write( "<script src=\"" //$NON-NLS-1$
								+ baseURL
								+ "birt/ajax/taglib/CascadingParameter.js\" type=\"text/javascript\"></script>\n" ); //$NON-NLS-1$
				writer
						.write( "<script src=\"" //$NON-NLS-1$
								+ baseURL
								+ "birt/ajax/taglib/ParameterGroup.js\" type=\"text/javascript\"></script>\n" ); //$NON-NLS-1$
				writer
						.write( "<script src=\"" //$NON-NLS-1$
								+ baseURL
								+ "birt/ajax/taglib/ParameterDefinition.js\" type=\"text/javascript\"></script>\n" ); //$NON-NLS-1$
				writer
						.write( "<script src=\"" //$NON-NLS-1$
								+ baseURL
								+ "birt/ajax/taglib/SoapResponseHelper.js\" type=\"text/javascript\"></script>\n" ); //$NON-NLS-1$
				writer
						.write( "<script src=\"" //$NON-NLS-1$
								+ baseURL
								+ "birt/ajax/taglib/ProgressBar.js\" type=\"text/javascript\"></script>\n" ); //$NON-NLS-1$

				// create ProgressBar div
				this.__createProgressBar( baseURL );

				writer.write( "<script language=\"JavaScript\">\n" ); //$NON-NLS-1$				
				writer
						.write( "var progressBar = new ProgressBar( \"progressBar\",\"mask\" );" ); //$NON-NLS-1$
				writer.write( "</script>\n" ); //$NON-NLS-1$

				pageContext.setAttribute( IMPORT_FILES_ATTR, Boolean.TRUE );
			}

			this.groupObjName = "group_" + this.viewer.getId( ) + "_" + paramDef.getGroup( ).getName( );//$NON-NLS-1$ //$NON-NLS-2$
			if ( pageContext.findAttribute( this.groupObjName ) == null )
			{
				writer
						.write( "<script  language=\"JavaScript\">var " + this.groupObjName + " = new ParameterGroup( );</script>\n" ); //$NON-NLS-1$ //$NON-NLS-2$
				pageContext.setAttribute( this.groupObjName, Boolean.TRUE );
			}

			// get parameter list from cascading group
			Collection selectionList = getParameterSelectionListForCascadingGroup( );
			__handleCommonListBox( selectionList );
			__handleCascadingListBox( );
		}
		else
		{
			// get parameter list
			Collection selectionList = BirtReportServiceFactory
					.getReportService( ).getParameterSelectionList(
							viewer.getReportDesignHandle( ), options,
							param.getName( ) );

			__handleCommonListBox( selectionList );
		}

	}

	/**
	 * Create Progress bar div
	 * 
	 * @param baseURL
	 * @throws Exception
	 */
	protected void __createProgressBar( String baseURL ) throws Exception
	{
		JspWriter writer = pageContext.getOut( );

		writer
				.write( "<DIV ID=\"mask\" STYLE=\"display:none;position:absolute;z-index:200\">\n" ); //$NON-NLS-1$
		writer.write( "</DIV>\n" ); //$NON-NLS-1$

		writer
				.write( "<DIV ID=\"progressBar\" STYLE=\"display:none;position:absolute;z-index:300\">\n" ); //$NON-NLS-1$
		writer
				.write( "<TABLE WIDTH=\"250px\" CLASS=\"birtviewer_progressbar\" CELLSPACING=\"10px\">\n" ); //$NON-NLS-1$
		writer.write( "	<TR>\n" ); //$NON-NLS-1$
		writer.write( "		<TD ALIGN=\"center\">\n" ); //$NON-NLS-1$
		writer.write( "			<B>" //$NON-NLS-1$
				+ BirtResources.getMessage( "birt.viewer.progressbar.prompt" ) //$NON-NLS-1$
				+ "</B>\n" ); //$NON-NLS-1$
		writer.write( "		</TD>\n" ); //$NON-NLS-1$
		writer.write( "	</TR>\n" ); //$NON-NLS-1$
		writer.write( "	<TR>\n" ); //$NON-NLS-1$
		writer.write( "		<TD ALIGN=\"center\">\n" ); //$NON-NLS-1$
		writer.write( "			<IMG SRC=\"" + baseURL //$NON-NLS-1$
				+ "birt/images/Loading.gif\" ALT=\"Progress Bar Image\"/>\n" ); //$NON-NLS-1$
		writer.write( "		</TD>\n" ); //$NON-NLS-1$
		writer.write( "	</TR>\n" ); //$NON-NLS-1$
		writer.write( "	<TR>\n" ); //$NON-NLS-1$
		writer.write( "		<TD ALIGN=\"center\">\n" ); //$NON-NLS-1$
		writer
				.write( "			<DIV ID=\"cancelTaskButton\" STYLE=\"display:block\">\n" ); //$NON-NLS-1$
		writer.write( "				<TABLE WIDTH=\"100%\">\n" ); //$NON-NLS-1$
		writer.write( "					<TR>\n" ); //$NON-NLS-1$
		writer.write( "						<TD ALIGN=\"center\">\n" ); //$NON-NLS-1$
		writer.write( "							<INPUT TYPE=\"BUTTON\" VALUE=\"" //$NON-NLS-1$
				+ BirtResources.getMessage( "birt.viewer.dialog.cancel" ) //$NON-NLS-1$
				+ "\" \n" ); //$NON-NLS-1$
		writer.write( "									TITLE=\"" //$NON-NLS-1$
				+ BirtResources.getMessage( "birt.viewer.dialog.cancel" ) //$NON-NLS-1$
				+ "\" \n" ); //$NON-NLS-1$
		writer.write( "									CLASS=\"birtviewer_progressbar_button\"/>\n" ); //$NON-NLS-1$
		writer.write( "						</TD>\n" ); //$NON-NLS-1$
		writer.write( "					</TR>\n" ); //$NON-NLS-1$
		writer.write( "				</TABLE>\n" ); //$NON-NLS-1$
		writer.write( "			</DIV>\n" ); //$NON-NLS-1$
		writer.write( "		</TD>\n" ); //$NON-NLS-1$
		writer.write( "	</TR>\n" ); //$NON-NLS-1$
		writer.write( "</TABLE>\n" ); //$NON-NLS-1$
		writer.write( "</DIV>\n" ); //$NON-NLS-1$
		writer.write( "<INPUT TYPE=\"HIDDEN\" ID=\"taskid\" VALUE=''/>\n" ); //$NON-NLS-1$
	}

	/**
	 * Handle Common List Box type parameter( not cascading parameter )
	 * 
	 * @param selectionList
	 * 
	 * @throws Exception
	 */
	protected void __handleCommonListBox( Collection selectionList )
			throws Exception
	{
		JspWriter writer = pageContext.getOut( );

		String encParamId = ParameterAccessor.htmlEncode( param.getId( ) );
		String encParamName = ParameterAccessor.htmlEncode( param.getName( ) );

		String displayTextId = encParamId + "_displayText"; //$NON-NLS-1$
		String displayTextName = ParameterAccessor.PREFIX_DISPLAY_TEXT
				+ encParamName;

		boolean isSelected = false;
		boolean isNullValue = param.getValue( ) == null;
		String valueId = encParamId + "_value"; //$NON-NLS-1$
		String nullValueId = encParamId + "_null"; //$NON-NLS-1$

		String radioSelectId = encParamId + "_radio_select"; //$NON-NLS-1$
		String radioTextId = encParamId + "_radio_input"; //$NON-NLS-1$
		String inputTextId = encParamId + "_input"; //$NON-NLS-1$

		String isLocaleId = encParamId + "_islocale"; //$NON-NLS-1$		
		String patternId = encParamId + "_pattern"; //$NON-NLS-1$
		String patternName = encParamName + "_format"; //$NON-NLS-1$

		if ( !paramDef.mustMatch( ) )
		{
			writer.write( "\n<script language=\"JavaScript\">\n" ); //$NON-NLS-1$

			// function for updating controls status
			writer.write( "function updateParam" + encParamId + "( flag )\n" ); //$NON-NLS-1$ //$NON-NLS-2$
			writer.write( "{\n" ); //$NON-NLS-1$
			writer
					.write( "var radioSelectCtl = document.getElementById(\"" + radioSelectId + "\");\n" ); //$NON-NLS-1$//$NON-NLS-2$
			writer
					.write( "if( radioSelectCtl ) radioSelectCtl.checked = flag;\n" ); //$NON-NLS-1$
			writer
					.write( "var radioTextCtl = document.getElementById(\"" + radioTextId + "\");\n" ); //$NON-NLS-1$//$NON-NLS-2$
			writer.write( "if( radioTextCtl ) radioTextCtl.checked = !flag;\n" ); //$NON-NLS-1$
			writer
					.write( "var selectCtl = document.getElementById(\"" + encParamId + "\");\n" ); //$NON-NLS-1$//$NON-NLS-2$
			writer.write( "if( selectCtl ) selectCtl.disabled = !flag;\n" ); //$NON-NLS-1$
			writer
					.write( "var inputCtl = document.getElementById(\"" + inputTextId + "\");\n" ); //$NON-NLS-1$//$NON-NLS-2$
			writer.write( "if( inputCtl ) inputCtl.disabled = flag;\n" ); //$NON-NLS-1$			

			// If input parameter in text field,enable locale control
			writer
					.write( "var localeCtl = document.getElementById(\"" + isLocaleId + "\");\n" ); //$NON-NLS-1$//$NON-NLS-2$
			writer.write( "if( localeCtl )\n" ); //$NON-NLS-1$
			writer.write( "{\n" ); //$NON-NLS-1$
			writer.write( "  if( flag )\n" ); //$NON-NLS-1$
			writer.write( "    localeCtl.name = '';\n" ); //$NON-NLS-1$
			writer.write( "  else\n" ); //$NON-NLS-1$
			writer.write( "    localeCtl.name = \"" //$NON-NLS-1$
					+ ParameterAccessor.PARAM_ISLOCALE + "\";\n" ); //$NON-NLS-1$
			writer.write( "}\n" ); //$NON-NLS-1$

			// If input parameter in text field,enable pattern control
			writer
					.write( "var patternCtl = document.getElementById(\"" + patternId + "\");\n" ); //$NON-NLS-1$//$NON-NLS-2$
			writer.write( "if( patternCtl )\n" ); //$NON-NLS-1$
			writer.write( "{\n" ); //$NON-NLS-1$
			writer.write( "  if( flag )\n" ); //$NON-NLS-1$
			writer.write( "    patternCtl.name = '';\n" ); //$NON-NLS-1$
			writer.write( "  else\n" ); //$NON-NLS-1$
			writer.write( "    patternCtl.name = \"" //$NON-NLS-1$
					+ patternName + "\";\n" ); //$NON-NLS-1$
			writer.write( "}\n" ); //$NON-NLS-1$

			writer.write( "if( flag )\n" ); //$NON-NLS-1$
			writer.write( "{\n" ); //$NON-NLS-1$
			writer.write( "  if( selectCtl.selectedIndex >= 0 )\n" ); //$NON-NLS-1$
			writer
					.write( "    handleParam" + encParamId + "( selectCtl.options[selectCtl.selectedIndex] );\n" ); //$NON-NLS-1$ //$NON-NLS-2$
			writer.write( "  else\n" ); //$NON-NLS-1$
			writer.write( "  {\n" ); //$NON-NLS-1$
			writer
					.write( "    var nullCtl = document.getElementById(\"" + nullValueId + "\");\n" ); //$NON-NLS-1$//$NON-NLS-2$
			writer
					.write( "    if( nullCtl ) nullCtl.name=\"" + ParameterAccessor.PARAM_ISNULL //$NON-NLS-1$
							+ "\";\n" ); //$NON-NLS-1$
			writer
					.write( "    var valCtl = document.getElementById(\"" + valueId //$NON-NLS-1$
							+ "\");\n" ); //$NON-NLS-1$
			writer.write( "    if( valCtl ) valCtl.name = '';\n" ); //$NON-NLS-1$
			writer.write( "    if( valCtl ) valCtl.value = '';\n" ); //$NON-NLS-1$
			writer.write( "    var displayCtl = document.getElementById(\"" //$NON-NLS-1$
					+ displayTextId + "\");\n" ); //$NON-NLS-1$			
			writer.write( "    if( displayCtl ) displayCtl.value = '';\n" ); //$NON-NLS-1$
			writer.write( "    if( displayCtl ) displayCtl.name = '';\n" ); //$NON-NLS-1$
			writer.write( "  }\n" ); //$NON-NLS-1$
			writer.write( "}\n" ); //$NON-NLS-1$
			writer.write( "else\n" ); //$NON-NLS-1$
			writer.write( "{\n" ); //$NON-NLS-1$
			writer.write( "  handleTextParam" + encParamId + "( );\n" ); //$NON-NLS-1$ //$NON-NLS-2$			
			writer.write( "}\n" ); //$NON-NLS-1$

			writer.write( "}\n" ); //$NON-NLS-1$

			// function for handling text input
			writer.write( "function handleTextParam" + encParamId + "( )\n" ); //$NON-NLS-1$ //$NON-NLS-2$
			writer.write( "{\n" ); //$NON-NLS-1$
			writer
					.write( "var inputCtl = document.getElementById(\"" + inputTextId //$NON-NLS-1$
							+ "\");\n" ); //$NON-NLS-1$			

			writer.write( "var valCtl = document.getElementById(\"" + valueId //$NON-NLS-1$
					+ "\");\n" ); //$NON-NLS-1$
			writer
					.write( "if( valCtl ) valCtl.name = \"" + encParamName + "\";\n" ); //$NON-NLS-1$//$NON-NLS-2$		
			writer.write( "if( valCtl ) valCtl.value = inputCtl.value;\n" ); //$NON-NLS-1$

			writer.write( "var displayCtl = document.getElementById(\"" //$NON-NLS-1$
					+ displayTextId + "\");\n" ); //$NON-NLS-1$
			writer
					.write( "if( displayCtl ) displayCtl.name = \"" + displayTextName + "\";\n" ); //$NON-NLS-1$ //$NON-NLS-2$
			writer
					.write( "if( displayCtl ) displayCtl.value = inputCtl.value;\n" ); //$NON-NLS-1$

			writer
					.write( "var nullCtl = document.getElementById(\"" + nullValueId + "\");\n" ); //$NON-NLS-1$//$NON-NLS-2$
			writer.write( "if( nullCtl ) nullCtl.name='';\n" ); //$NON-NLS-1$

			writer
					.write( "var localeCtl = document.getElementById(\"" + isLocaleId + "\");\n" ); //$NON-NLS-1$//$NON-NLS-2$
			writer
					.write( "if( localeCtl ) localeCtl.name = \"" + ParameterAccessor.PARAM_ISLOCALE + "\";\n" ); //$NON-NLS-1$ //$NON-NLS-2$

			writer.write( "}\n" ); //$NON-NLS-1$

			writer.write( "</script>\n" ); //$NON-NLS-1$
		}

		// onchange script
		writer.write( "\n<script language=\"JavaScript\">\n" ); //$NON-NLS-1$
		writer.write( "function handleParam" + encParamId + "( option )\n" ); //$NON-NLS-1$ //$NON-NLS-2$
		writer.write( "{\n" ); //$NON-NLS-1$
		writer.write( "if( !option ) return;\n" ); //$NON-NLS-1$

		writer.write( "var valCtl = document.getElementById(\"" + valueId //$NON-NLS-1$
				+ "\");\n" ); //$NON-NLS-1$
		writer.write( "var displayCtl = document.getElementById(\"" //$NON-NLS-1$
				+ displayTextId + "\");\n" ); //$NON-NLS-1$
		writer
				.write( "var nullCtl = document.getElementById(\"" + nullValueId + "\");\n" ); //$NON-NLS-1$//$NON-NLS-2$
		writer.write( "var label = option.text;\n" ); //$NON-NLS-1$
		writer.write( "var value = option.value;\n" ); //$NON-NLS-1$
		writer.write( "if( label == \"" + ITagConstants.NULL_VALUE + "\")\n" ); //$NON-NLS-1$//$NON-NLS-2$
		writer.write( "{\n" ); //$NON-NLS-1$
		writer
				.write( "  if( nullCtl ) nullCtl.name=\"" + ParameterAccessor.PARAM_ISNULL //$NON-NLS-1$
						+ "\";\n" ); //$NON-NLS-1$
		writer.write( "  if( valCtl ) valCtl.name = '';\n" ); //$NON-NLS-1$
		writer.write( "  if( valCtl ) valCtl.value = '';\n" ); //$NON-NLS-1$
		writer.write( "  if( displayCtl ) displayCtl.value = '';\n" ); //$NON-NLS-1$
		writer.write( "  if( displayCtl ) displayCtl.name = '';\n" ); //$NON-NLS-1$
		writer.write( "}\n" ); //$NON-NLS-1$
		writer.write( "else\n" ); //$NON-NLS-1$
		writer.write( "{\n" ); //$NON-NLS-1$
		writer.write( "  if( nullCtl ) nullCtl.name='';\n" ); //$NON-NLS-1$
		writer
				.write( "  if( valCtl ) valCtl.name = \"" + encParamName + "\";\n" ); //$NON-NLS-1$//$NON-NLS-2$
		writer.write( "  if( valCtl ) valCtl.value = value;\n" ); //$NON-NLS-1$		
		writer
				.write( "  if( displayCtl ) displayCtl.name = \"" + displayTextName + "\";\n" ); //$NON-NLS-1$ //$NON-NLS-2$
		writer.write( "  if( displayCtl ) displayCtl.value = label;\n" ); //$NON-NLS-1$
		writer.write( "}\n" ); //$NON-NLS-1$

		writer.write( "}\n" ); //$NON-NLS-1$
		writer.write( "</script>\n" ); //$NON-NLS-1$

		String onChange = "handleParam" + encParamId + "( this.options[this.selectedIndex] )"; //$NON-NLS-1$ //$NON-NLS-2$
		if ( !paramDef.mustMatch( ) )
		{
			String onClick = "updateParam" + encParamId + "( true )"; //$NON-NLS-1$ //$NON-NLS-2$
			writer.write( "<input type=\"radio\" " ); //$NON-NLS-1$
			writer.write( " id=\"" + radioSelectId + "\"" ); //$NON-NLS-1$//$NON-NLS-2$
			writer.write( " onclick=\"" + onClick + "\"" ); //$NON-NLS-1$//$NON-NLS-2$
			writer.write( " >\n" ); //$NON-NLS-1$
		}

		// select control
		writer.write( "<select " ); //$NON-NLS-1$
		writer.write( " id=\"" + encParamId + "\"" ); //$NON-NLS-1$//$NON-NLS-2$			
		__handleGeneralDefinition( );
		writer.write( " onchange=\"" + onChange + "\"" ); //$NON-NLS-1$ //$NON-NLS-2$
		writer.write( " >\n" ); //$NON-NLS-1$

		// blank item
		if ( paramDef.mustMatch( ) && !paramDef.isRequired( ) )
		{
			writer.write( "<option value='' " ); //$NON-NLS-1$
			if ( param.getValue( ) != null
					&& DataUtil.getString( param.getValue( ) ).length( ) <= 0 )
			{
				writer.write( " selected " ); //$NON-NLS-1$
				isSelected = true;
			}
			writer.write( "></option>\n" ); //$NON-NLS-1$
		}

		for ( Iterator iter = selectionList.iterator( ); iter.hasNext( ); )
		{
			ParameterSelectionChoice selectionItem = (ParameterSelectionChoice) iter
					.next( );

			Object value = selectionItem.getValue( );
			try
			{
				// try convert value to parameter definition data type
				value = DataUtil.convert( value, paramDef.getDataType( ) );
			}
			catch ( Exception e )
			{
				value = null;
			}

			// Convert parameter value using standard format
			String displayValue = DataUtil.getDisplayValue( value );
			if ( displayValue == null )
				continue;

			// If label is null or blank, then use the format parameter
			// value for display
			String label = selectionItem.getLabel( );
			if ( label == null || label.length( ) <= 0 )
				label = ParameterValidationUtil.getDisplayValue( null,
						this.pattern, value, this.locale );

			label = label != null ? label : ""; //$NON-NLS-1$
			writer.write( "<option value=\"" //$NON-NLS-1$
					+ ParameterAccessor.htmlEncode( displayValue ) + "\"" ); //$NON-NLS-1$
			if ( displayValue.equals( DataUtil.getDisplayValue( param
					.getValue( ) ) ) )
			{
				isSelected = true;
				writer.write( " selected" ); //$NON-NLS-1$
				if ( param.getDisplayText( ) == null )
				{
					this.displayTextString = label;
				}
				else
				{
					label = param.getDisplayText( );
				}
			}
			writer.write( ">" ); //$NON-NLS-1$
			writer.write( ParameterAccessor.htmlEncode( label ) );
			writer.write( "</option>\n" ); //$NON-NLS-1$
		}

		String defaultValueText = null;
		if ( !isSelected && paramDef.mustMatch( ) )
		{
			Object defaultValue = BirtReportServiceFactory.getReportService( )
					.getParameterDefaultValue( viewer.getReportDesignHandle( ),
							param.getName( ), options );
			if ( defaultValue == null )
			{
				isNullValue = true;
			}
			else
			{
				isNullValue = false;
				defaultValueText = DataUtil.getDisplayValue( defaultValue );
				if ( defaultValueText != null )
					this.valueString = defaultValueText;

				String defaultDisplayText = ParameterValidationUtil
						.getDisplayValue( null, this.pattern, defaultValue,
								locale );
				if ( defaultDisplayText != null )
					this.displayTextString = defaultDisplayText;

				writer.write( "<option " ); //$NON-NLS-1$
				writer
						.write( " value=\"" + ParameterAccessor.htmlEncode( this.valueString ) + "\" " ); //$NON-NLS-1$ //$NON-NLS-2$					
				writer.write( " selected >" ); //$NON-NLS-1$
				writer.write( ParameterAccessor
						.htmlEncode( this.displayTextString )
						+ "</option>\n" ); //$NON-NLS-1$
			}
		}

		// null value item
		if ( !paramDef.isRequired( ) )
		{
			writer.write( "<option value=''" ); //$NON-NLS-1$
			if ( isNullValue )
				writer.write( " selected" ); //$NON-NLS-1$					
			writer.write( " >" ); //$NON-NLS-1$
			writer.write( ITagConstants.NULL_VALUE + "</option>\n" ); //$NON-NLS-1$
		}

		writer.write( "</select>\n" ); //$NON-NLS-1$

		if ( !paramDef.mustMatch( ) )
		{
			// isLocale hidden object
			writer.write( "<input type = 'hidden' " ); //$NON-NLS-1$
			writer.write( " id=\"" + isLocaleId + "\" " ); //$NON-NLS-1$ //$NON-NLS-2$
			writer.write( " value=\"" + encParamName + "\" " ); //$NON-NLS-1$ //$NON-NLS-2$
			writer.write( " >\n" ); //$NON-NLS-1$

			// set parameter pattern format
			if ( param.getPattern( ) != null )
			{
				writer.write( "<input type = 'hidden' " ); //$NON-NLS-1$
				writer.write( " name=\"" + patternName + "\" " ); //$NON-NLS-1$ //$NON-NLS-2$
				writer
						.write( " value=\"" + ParameterAccessor.htmlEncode( param.getPattern( ) ) + "\">\n" ); //$NON-NLS-1$//$NON-NLS-2$
			}

			String onClick = "updateParam" + encParamId + "( false );"; //$NON-NLS-1$ //$NON-NLS-2$			
			writer.write( "<input type=\"radio\" " ); //$NON-NLS-1$
			writer.write( " id=\"" + radioTextId + "\"" ); //$NON-NLS-1$//$NON-NLS-2$
			writer.write( " onclick=\"" + onClick + "\"" ); //$NON-NLS-1$//$NON-NLS-2$
			writer.write( " >\n" ); //$NON-NLS-1$

			writer.write( "<input type=\"text\" " ); //$NON-NLS-1$
			writer.write( " id=\"" + inputTextId + "\"" ); //$NON-NLS-1$//$NON-NLS-2$
			if ( !isSelected )
			{
				writer
						.write( " value=\"" + ParameterAccessor.htmlEncode( this.displayTextString ) + "\"" ); //$NON-NLS-1$ //$NON-NLS-2$
			}
			writer.write( " onchange=\"handleTextParam" + encParamId + "( )\"" ); //$NON-NLS-1$ //$NON-NLS-2$
			writer.write( " >\n" ); //$NON-NLS-1$

			// initialize controls
			writer
					.write( "<script language=\"JavaScript\">updateParam" + encParamId + "(" + ( isNullValue || isSelected ) + ");</script>\n" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		// display text hidden object
		writer.write( "<input type=\"hidden\" " ); //$NON-NLS-1$
		writer.write( " id=\"" + displayTextId + "\" " ); //$NON-NLS-1$//$NON-NLS-2$
		if ( !isNullValue )
		{
			writer.write( " name=\"" + displayTextName + "\" " ); //$NON-NLS-1$ //$NON-NLS-2$
			writer
					.write( " value=\"" + ParameterAccessor.htmlEncode( this.displayTextString ) + "\" " ); //$NON-NLS-1$ //$NON-NLS-2$ 

		}
		writer.write( " >\n" ); //$NON-NLS-1$

		// parameter value hidden object
		writer.write( "<input type=\"hidden\" " ); //$NON-NLS-1$
		writer.write( " id=\"" + valueId + "\" " ); //$NON-NLS-1$//$NON-NLS-2$
		if ( !isNullValue )
		{
			writer.write( " name=\"" + encParamName + "\" " ); //$NON-NLS-1$ //$NON-NLS-2$
			writer
					.write( " value=\"" + ParameterAccessor.htmlEncode( this.valueString ) + "\" " ); //$NON-NLS-1$ //$NON-NLS-2$
		}
		writer.write( " >\n" ); //$NON-NLS-1$

		// Null Value hidden object
		if ( !paramDef.isRequired( ) )
		{
			writer.write( "<input type=\"hidden\" value=\"" //$NON-NLS-1$
					+ encParamName + "\" id=\"" + nullValueId + "\"" ); //$NON-NLS-1$ //$NON-NLS-2$
			if ( isNullValue )
				writer.write( " name=\"" //$NON-NLS-1$
						+ ParameterAccessor.PARAM_ISNULL + "\"" ); //$NON-NLS-1$
			writer.write( " >\n" ); //$NON-NLS-1$
		}

		if ( !isSelected && paramDef.mustMatch( ) )
		{
			writer.write( "\n<script language=\"JavaScript\">\n" ); //$NON-NLS-1$
			writer.write( "var selectCtl = document.getElementById(\"" //$NON-NLS-1$
					+ encParamId + "\");\n" ); //$NON-NLS-1$
			writer.write( "if( selectCtl.selectedIndex >= 0 )\n" ); //$NON-NLS-1$
			writer.write( "{\n" ); //$NON-NLS-1$
			if ( defaultValueText != null )
			{
				writer.write( "  selectCtl.value = \"" + defaultValueText //$NON-NLS-1$
						+ "\";\n" ); //$NON-NLS-1$
			}
			writer
					.write( "  handleParam" + encParamId + "( selectCtl.options[selectCtl.selectedIndex] );\n" ); //$NON-NLS-1$ //$NON-NLS-2$				
			writer.write( "}\n" ); //$NON-NLS-1$
			writer.write( "</script>\n" ); //$NON-NLS-1$
		}
	}

	/**
	 * Handle Cascading List Box type parameter
	 * 
	 * @throws Exception
	 */
	protected void __handleCascadingListBox( ) throws Exception
	{
		String encParamId = ParameterAccessor.htmlEncode( param.getId( ) );
		String encParamName = ParameterAccessor.htmlEncode( param.getName( ) );
		String inputTextId = encParamId + "_input"; //$NON-NLS-1$

		JspWriter writer = pageContext.getOut( );

		writer.write( "\n<script language=\"JavaScript\">\n" ); //$NON-NLS-1$
		writer
				.write( "var param = new ParameterDefinition(\"" + encParamId + "\",\"" + encParamName + "\");\n" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		writer.write( "param.setRequired(" + paramDef.isRequired( ) + ");\n" ); //$NON-NLS-1$ //$NON-NLS-2$
		writer.write( this.groupObjName + ".addParameter( param );\n" ); //$NON-NLS-1$
		writer.write( "</script>\n" ); //$NON-NLS-1$

		ParameterGroupDefinition group = (ParameterGroupDefinition) paramDef
				.getGroup( );
		int index = group.getParameters( ).indexOf( paramDef );

		// if it is the last cascading parameter, return
		if ( index == group.getParameterCount( ) - 1 )
			return;

		String casObj = "cas" + encParamId; //$NON-NLS-1$
		String namesObj = "names_" + encParamId; //$NON-NLS-1$
		writer.write( "\n<script language=\"JavaScript\">\n" ); //$NON-NLS-1$
		writer
				.write( "var " + namesObj + " = new Array( " + ( index + 2 ) + " );\n" ); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		for ( int i = 0; i < index + 2; i++ )
		{
			ParameterDefinition param = (ParameterDefinition) group
					.getParameters( ).get( i );
			writer.write( namesObj + "[" + i + "] = \"" //$NON-NLS-1$ //$NON-NLS-2$
					+ ParameterAccessor.htmlEncode( param.getName( ) )
					+ "\";\n" ); //$NON-NLS-1$
		}
		writer
				.write( "var " + casObj + " = new CascadingParameter( \"" + this.viewer.getId( ) + "\", param, " + namesObj + ", " + this.groupObjName + " );\n" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		writer
				.write( "var selectCtl = document.getElementById(\"" + encParamId + "\");\n" ); //$NON-NLS-1$//$NON-NLS-2$
		writer.write( "selectCtl.onchange = function( ) { \n" ); //$NON-NLS-1$
		writer.write( "var selectCtl = document.getElementById(\"" + encParamId //$NON-NLS-1$
				+ "\");\n" ); //$NON-NLS-1$
		writer.write( "handleParam" + encParamId //$NON-NLS-1$
				+ "( selectCtl.options[selectCtl.selectedIndex] );\n" ); //$NON-NLS-1$
		writer.write( "progressBar.setHandler(" + casObj + ");\n" ); //$NON-NLS-1$ //$NON-NLS-2$
		writer.write( casObj + ".process( ); };\n" ); //$NON-NLS-1$
		writer
				.write( "var inputCtl = document.getElementById(\"" + inputTextId + "\");\n" ); //$NON-NLS-1$//$NON-NLS-2$
		writer.write( "if( inputCtl )\n" ); //$NON-NLS-1$
		writer.write( "{\n" ); //$NON-NLS-1$
		writer.write( "inputCtl.onchange = function( ) { \n" ); //$NON-NLS-1$
		writer.write( "handleTextParam" + encParamId + "( );\n" ); //$NON-NLS-1$ //$NON-NLS-2$
		writer.write( "progressBar.setHandler(" + casObj + ");\n" ); //$NON-NLS-1$ //$NON-NLS-2$
		writer.write( casObj + ".process( ); };\n" ); //$NON-NLS-1$
		writer.write( "}\n" ); //$NON-NLS-1$
		writer.write( "</script>\n" ); //$NON-NLS-1$
	}

	/**
	 * Get parameter selection list from cascading parameter
	 * 
	 * @return
	 * @throws ReportServiceException
	 */
	private Collection getParameterSelectionListForCascadingGroup( )
			throws ReportServiceException
	{

		ParameterGroupDefinition group = (ParameterGroupDefinition) paramDef
				.getGroup( );
		int index = group.getParameters( ).indexOf( paramDef );
		Object[] groupKeys = new Object[index];
		for ( int i = 0; i < index; i++ )
		{
			ParameterDefinition def = (ParameterDefinition) group
					.getParameters( ).get( i );
			String parameterName = def.getName( );
			groupKeys[i] = requesterTag.getParameters( ).get( parameterName );
		}
		return BirtReportServiceFactory.getReportService( )
				.getSelectionListForCascadingGroup(
						viewer.getReportDesignHandle( ), group.getName( ),
						groupKeys, options );
	}

	/**
	 * Handle output Radio Button type parameter
	 * 
	 * @throws Exception
	 */
	protected void __handleRadioButton( ) throws Exception
	{
		Collection selectionList = BirtReportServiceFactory.getReportService( )
				.getParameterSelectionList( viewer.getReportDesignHandle( ),
						this.options, param.getName( ) );
		if ( selectionList == null || selectionList.size( ) <= 0 )
			return;

		JspWriter writer = pageContext.getOut( );

		String encParamId = ParameterAccessor.htmlEncode( param.getId( ) );
		String encParamName = ParameterAccessor.htmlEncode( param.getName( ) );

		String displayTextId = encParamId + "_displayText"; //$NON-NLS-1$
		String displayTextName = ParameterAccessor.PREFIX_DISPLAY_TEXT
				+ encParamName;

		String nullValueId = encParamId + "_null"; //$NON-NLS-1$
		String radioNullValueId = encParamId + "_radio_null"; //$NON-NLS-1$

		String radioName = encParamId + "_radio"; //$NON-NLS-1$
		String valueId = encParamId + "_value"; //$NON-NLS-1$
		boolean isChecked = false;

		// onclick script
		writer.write( "\n<script language=\"JavaScript\">\n" ); //$NON-NLS-1$
		writer.write( "function handleParam" + encParamId + "( e )\n" ); //$NON-NLS-1$ //$NON-NLS-2$
		writer.write( "{\n" ); //$NON-NLS-1$
		writer.write( "var obj;\n" ); //$NON-NLS-1$
		writer.write( "if( window.event )\n" ); //$NON-NLS-1$
		writer.write( "{\n" ); //$NON-NLS-1$
		writer.write( "  obj = window.event.srcElement;\n" ); //$NON-NLS-1$
		writer.write( "}\n" ); //$NON-NLS-1$
		writer.write( "else\n" ); //$NON-NLS-1$
		writer.write( "{\n" ); //$NON-NLS-1$		
		writer.write( "  if( e ) obj = e.target;\n" ); //$NON-NLS-1$
		writer.write( "}\n" ); //$NON-NLS-1$
		writer.write( "if( !obj ) return;\n" ); //$NON-NLS-1$

		writer.write( "var valCtl = document.getElementById(\"" + valueId //$NON-NLS-1$
				+ "\");\n" ); //$NON-NLS-1$
		writer.write( "var displayCtl = document.getElementById(\"" //$NON-NLS-1$
				+ displayTextId + "\");\n" ); //$NON-NLS-1$
		writer
				.write( "var nullCtl = document.getElementById(\"" + nullValueId + "\");\n" ); //$NON-NLS-1$//$NON-NLS-2$
		writer.write( "if( obj.id == \"" + radioNullValueId + "\")\n" ); //$NON-NLS-1$//$NON-NLS-2$
		writer.write( "{\n" ); //$NON-NLS-1$
		writer
				.write( "  if( nullCtl ) nullCtl.name=\"" + ParameterAccessor.PARAM_ISNULL //$NON-NLS-1$
						+ "\";\n" ); //$NON-NLS-1$
		writer.write( "  valCtl.name = '';\n" ); //$NON-NLS-1$
		writer.write( "  valCtl.value = '';\n" ); //$NON-NLS-1$
		writer.write( "  displayCtl.value = '';\n" ); //$NON-NLS-1$
		writer.write( "  displayCtl.name = '';\n" ); //$NON-NLS-1$
		writer.write( "}\n" ); //$NON-NLS-1$
		writer.write( "else\n" ); //$NON-NLS-1$
		writer.write( "{\n" ); //$NON-NLS-1$
		writer.write( "  if( nullCtl ) nullCtl.name='';\n" ); //$NON-NLS-1$
		writer.write( "  valCtl.name = \"" + encParamName + "\";\n" ); //$NON-NLS-1$//$NON-NLS-2$
		writer.write( "  valCtl.value = obj.value;\n" ); //$NON-NLS-1$
		writer
				.write( "  var labelCtl = document.getElementById( obj.id + \"_label\");\n" ); //$NON-NLS-1$
		writer.write( "  displayCtl.value = labelCtl.innerHTML;\n" ); //$NON-NLS-1$
		writer.write( "  displayCtl.name = \"" + displayTextName + "\";\n" ); //$NON-NLS-1$ //$NON-NLS-2$
		writer.write( "}\n" ); //$NON-NLS-1$

		writer.write( "}\n" ); //$NON-NLS-1$
		writer.write( "</script>\n" ); //$NON-NLS-1$

		String onClick = "handleParam" + encParamId + "( event )"; //$NON-NLS-1$ //$NON-NLS-2$

		int index = 0;
		for ( Iterator iter = selectionList.iterator( ); iter.hasNext( ); )
		{
			ParameterSelectionChoice selectionItem = (ParameterSelectionChoice) iter
					.next( );

			Object value = selectionItem.getValue( );
			try
			{
				// try convert value to parameter definition data type
				value = DataUtil.convert( value, paramDef.getDataType( ) );
			}
			catch ( Exception e )
			{
				value = null;
			}

			// Convert parameter value using standard format
			String displayValue = DataUtil.getDisplayValue( value );
			if ( displayValue == null )
				continue;

			// If label is null or blank, then use the format parameter
			// value for display
			String label = selectionItem.getLabel( );
			if ( label == null || label.length( ) <= 0 )
				label = ParameterValidationUtil.getDisplayValue( null,
						this.pattern, value, this.locale );

			label = label != null ? ParameterAccessor.htmlEncode( label ) : ""; //$NON-NLS-1$
			String ctlId = encParamId + "_" //$NON-NLS-1$
					+ index;

			writer.write( "<input type=\"radio\" " ); //$NON-NLS-1$
			writer.write( " name=\"" + radioName + "\"" ); //$NON-NLS-1$//$NON-NLS-2$
			writer.write( " id=\"" + ctlId + "\"" ); //$NON-NLS-1$//$NON-NLS-2$
			__handleGeneralDefinition( );
			writer
					.write( " value=\"" + ParameterAccessor.htmlEncode( displayValue ) + "\"" ); //$NON-NLS-1$ //$NON-NLS-2$
			writer.write( " onclick=\"" + onClick + "\"" ); //$NON-NLS-1$ //$NON-NLS-2$
			if ( displayValue.equalsIgnoreCase( DataUtil.getDisplayValue( param
					.getValue( ) ) ) )
			{
				isChecked = true;
				writer.write( " checked" ); //$NON-NLS-1$
				if ( param.getDisplayText( ) == null )
				{
					this.displayTextString = label;
				}
				else
				{
					label = param.getDisplayText( );
				}
			}
			writer.write( " >" ); //$NON-NLS-1$
			writer.write( "<label id=\"" + ( ctlId + "_label" ) + "\"" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			writer.write( " title=\"" + label + "\"" ); //$NON-NLS-1$ //$NON-NLS-2$
			writer.write( " for=\"" + ctlId + "\">" ); //$NON-NLS-1$//$NON-NLS-2$
			writer.write( label );
			writer.write( "</label>" ); //$NON-NLS-1$
			writer.write( "</input>\n" ); //$NON-NLS-1$

			index++;
		}

		// allow Null value
		if ( !paramDef.isRequired( ) )
		{
			boolean isNullValue = param.getValue( ) == null;

			// Null Value hidden object
			writer.write( "<input type=\"hidden\" value=\"" //$NON-NLS-1$
					+ encParamName + "\" id=\"" + nullValueId + "\"" ); //$NON-NLS-1$ //$NON-NLS-2$
			if ( isNullValue )
				writer.write( " name=\"" //$NON-NLS-1$
						+ ParameterAccessor.PARAM_ISNULL + "\"" ); //$NON-NLS-1$
			writer.write( " >\n" ); //$NON-NLS-1$

			writer
					.write( "<input type=\"radio\" id=\"" + radioNullValueId + "\" " ); //$NON-NLS-1$//$NON-NLS-2$
			writer.write( " name=\"" + radioName + "\"" ); //$NON-NLS-1$//$NON-NLS-2$
			writer.write( " onclick=\"" + onClick + "\"" ); //$NON-NLS-1$ //$NON-NLS-2$
			if ( isNullValue )
				writer.write( " checked " ); //$NON-NLS-1$
			writer.write( " >\n" ); //$NON-NLS-1$
			writer
					.write( "<label id=\"" + ( radioNullValueId + "_label" ) + "\"" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			writer.write( " title=\"" + ITagConstants.NULL_VALUE + "\"" ); //$NON-NLS-1$ //$NON-NLS-2$
			writer.write( " for=\"" + radioNullValueId + "\">" ); //$NON-NLS-1$//$NON-NLS-2$
			writer.write( ITagConstants.NULL_VALUE );
			writer.write( "</label>" ); //$NON-NLS-1$
			writer.write( "</input>" ); //$NON-NLS-1$
		}

		// display text hidden object
		writer.write( "<input type=\"hidden\" " ); //$NON-NLS-1$
		writer.write( " id=\"" + displayTextId + "\" " ); //$NON-NLS-1$//$NON-NLS-2$
		if ( isChecked )
		{
			writer.write( " name=\"" + displayTextName + "\" " ); //$NON-NLS-1$ //$NON-NLS-2$
			writer
					.write( " value=\"" + ParameterAccessor.htmlEncode( this.displayTextString ) + "\" " ); //$NON-NLS-1$ //$NON-NLS-2$ 

		}
		writer.write( " >\n" ); //$NON-NLS-1$

		// parameter value hidden object
		writer.write( "<input type=\"hidden\" " ); //$NON-NLS-1$
		writer.write( " id=\"" + valueId + "\" " ); //$NON-NLS-1$//$NON-NLS-2$
		if ( isChecked )
		{
			writer.write( " name=\"" + encParamName + "\" " ); //$NON-NLS-1$ //$NON-NLS-2$
			writer
					.write( " value=\"" + ParameterAccessor.htmlEncode( this.valueString ) + "\" " ); //$NON-NLS-1$ //$NON-NLS-2$
		}
		writer.write( " >\n" ); //$NON-NLS-1$
	}

	/**
	 * Handle output Check Box type parameter
	 * 
	 * @throws Exception
	 */
	protected void __handleCheckBox( ) throws Exception
	{
		JspWriter writer = pageContext.getOut( );

		String encParamId = ParameterAccessor.htmlEncode( param.getId( ) );
		String encParamName = ParameterAccessor.htmlEncode( param.getName( ) );

		Boolean bl = (Boolean) param.getValue( );
		boolean value = bl != null ? bl.booleanValue( ) : false;

		// parameter hidden value control
		String valueId = encParamId + "_value"; //$NON-NLS-1$
		writer.write( "<input type=\"hidden\" " ); //$NON-NLS-1$
		writer.write( " id=\"" + valueId + "\" " ); //$NON-NLS-1$//$NON-NLS-2$
		writer.write( " name=\"" + encParamName + "\" " ); //$NON-NLS-1$ //$NON-NLS-2$
		writer
				.write( " value=\"" + ParameterAccessor.htmlEncode( this.valueString ) + "\" " ); //$NON-NLS-1$ //$NON-NLS-2$ 
		writer.write( " >\n" ); //$NON-NLS-1$

		String valCtl = "document.getElementById('" + valueId + "')"; //$NON-NLS-1$ //$NON-NLS-2$
		String inputCtl = "document.getElementById('" + encParamId + "')"; //$NON-NLS-1$ //$NON-NLS-2$
		String onClick = "var value = 'false';if( " + inputCtl + ".checked ) value='true';" + valCtl + ".value = value;"; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$

		writer.write( "<input type=\"checkbox\" " ); //$NON-NLS-1$
		if ( param.getId( ) != null )
			writer.write( " id=\"" + encParamId + "\"" ); //$NON-NLS-1$//$NON-NLS-2$
		__handleGeneralDefinition( );
		writer.write( " onclick=\"" + onClick + "\"" ); //$NON-NLS-1$ //$NON-NLS-2$
		if ( value )
			writer.write( " checked " ); //$NON-NLS-1$

		writer.write( " >" ); //$NON-NLS-1$
	}

	/**
	 * Handle Exception
	 * 
	 * @param e
	 * @throws JspException
	 */
	protected void __handleException( Exception e ) throws JspException
	{
		JspWriter writer = pageContext.getOut( );
		try
		{
			writer.write( "<font color='red'>" ); //$NON-NLS-1$
			writer.write( e.getMessage( ) );
			writer.write( "</font>" ); //$NON-NLS-1$
		}
		catch ( IOException err )
		{
			throw new JspException( err );
		}
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId( String id )
	{
		param.setId( id );
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName( String name )
	{
		param.setName( name );
	}

	/**
	 * @param pattern
	 *            the pattern to set
	 */
	public void setPattern( String pattern )
	{
		param.setPattern( pattern );
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue( Object value )
	{
		param.setValue( value );
	}

	/**
	 * @param displayText
	 *            the displayText to set
	 */
	public void setDisplayText( String displayText )
	{
		param.setDisplayText( displayText );
	}

	/**
	 * @param isLocale
	 *            the isLocale to set
	 */
	public void setIsLocale( String isLocale )
	{
		param.setIsLocale( isLocale );
	}

	/**
	 * @param title
	 *            the title to set
	 */
	public void setTitle( String title )
	{
		param.setTitle( title );
	}

	/**
	 * @param cssClass
	 *            the cssClass to set
	 */
	public void setCssClass( String cssClass )
	{
		param.setCssClass( cssClass );
	}

	/**
	 * @param style
	 *            the style to set
	 */
	public void setStyle( String style )
	{
		param.setStyle( style );
	}
}
