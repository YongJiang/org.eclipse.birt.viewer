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

package org.eclipse.birt.integration.wtp.ui.internal.util;

import java.util.Map;

import org.eclipse.birt.integration.wtp.ui.internal.dialogs.FolderSelectionGroup;
import org.eclipse.birt.integration.wtp.ui.internal.resource.BirtWTPMessages;
import org.eclipse.birt.integration.wtp.ui.internal.wizards.BirtWizardUtil;
import org.eclipse.birt.integration.wtp.ui.internal.wizards.IBirtWizardConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Create UI Components
 * 
 */
public class UIUtil implements IBirtWizardConstants
{

	/**
	 * Dialog Properties Map
	 */
	private Map properties;

	/**
	 * Max value of Max Rows setting
	 */
	private final static int MAX_MAX_ROWS = Integer.MAX_VALUE;

	public UIUtil( Map properties )
	{
		this.properties = properties;
	}

	/**
	 * Create "BIRT_RESOURCE_PATH" configuration group
	 * 
	 * @param composite
	 */
	public Text createResourceFolderGroup( Composite parent )
	{
		Text txtResourceFolder = null;

		// get default value of resource folder setting
		String defaultFolder = BirtWizardUtil.getDefaultResourceFolder( );

		// create folder selection group
		FolderSelectionGroup group = new FolderSelectionGroup( );
		group.setLabelText( BirtWTPMessages.BIRTConfiguration_resource_label );
		group
				.setButtonText( BirtWTPMessages.BIRTConfiguration_folder_button_text );
		group
				.setDialogTitle( BirtWTPMessages.BIRTConfiguration_resource_dialog_title );
		group
				.setDialogMessage( BirtWTPMessages.BIRTConfiguration_resource_dialog_message );
		group.setDialogFilterPath( defaultFolder );
		group.setTextValue( defaultFolder );

		group.create( parent );
		txtResourceFolder = group.getText( );

		// add modify listener
		txtResourceFolder.addModifyListener( new ModifyListener( ) {

			public void modifyText( ModifyEvent e )
			{
				WebArtifactUtil.setContextParamValue( properties,
						BIRT_RESOURCE_FOLDER_SETTING, ( (Text) e.getSource( ) )
								.getText( ) );
			}
		} );

		return txtResourceFolder;
	}

	/**
	 * Create "BIRT_VIEWER_DOCUMENT_FOLDER" configuration group
	 * 
	 * @param composite
	 */
	public Text createDocumentFolderGroup( Composite parent )
	{
		Text txtDocumentFolder = null;

		// create folder selection group
		FolderSelectionGroup group = new FolderSelectionGroup( );
		group.setLabelText( BirtWTPMessages.BIRTConfiguration_document_label );
		group
				.setButtonText( BirtWTPMessages.BIRTConfiguration_folder_button_text );
		group
				.setDialogTitle( BirtWTPMessages.BIRTConfiguration_document_dialog_title );
		group
				.setDialogMessage( BirtWTPMessages.BIRTConfiguration_document_dialog_message );

		// set default value
		group.setTextValue( DataUtil.getString(
				WebArtifactUtil.getContextParamValue( properties,
						BIRT_DOCUMENT_FOLDER_SETTING ), false ) );

		group.create( parent );
		txtDocumentFolder = group.getText( );

		// add modify listener
		txtDocumentFolder.addModifyListener( new ModifyListener( ) {

			public void modifyText( ModifyEvent e )
			{
				WebArtifactUtil.setContextParamValue( properties,
						BIRT_DOCUMENT_FOLDER_SETTING, ( (Text) e.getSource( ) )
								.getText( ) );
			}
		} );

		return txtDocumentFolder;
	}

	/**
	 * Create "BIRT_VIEWER_IMAGE_DIR" configuration group
	 * 
	 * @param composite
	 */
	public Text createImageFolderGroup( Composite parent )
	{
		Text txtImageFolder = null;

		// create folder selection group
		FolderSelectionGroup group = new FolderSelectionGroup( );
		group.setLabelText( BirtWTPMessages.BIRTConfiguration_image_label );
		group
				.setButtonText( BirtWTPMessages.BIRTConfiguration_folder_button_text );
		group
				.setDialogTitle( BirtWTPMessages.BIRTConfiguration_image_dialog_title );
		group
				.setDialogMessage( BirtWTPMessages.BIRTConfiguration_image_dialog_message );

		// set default value
		group.setTextValue( DataUtil.getString( WebArtifactUtil
				.getContextParamValue( properties, BIRT_IMAGE_FOLDER_SETTING ),
				false ) );

		group.create( parent );
		txtImageFolder = group.getText( );

		// add modify listener
		txtImageFolder.addModifyListener( new ModifyListener( ) {

			public void modifyText( ModifyEvent e )
			{
				WebArtifactUtil.setContextParamValue( properties,
						BIRT_IMAGE_FOLDER_SETTING, ( (Text) e.getSource( ) )
								.getText( ) );
			}
		} );

		return txtImageFolder;
	}

	/**
	 * Create "BIRT_VIEWER_SCRIPTLIB_DIR" configuration group
	 * 
	 * @param composite
	 */
	public Text createScriptLibFolderGroup( Composite parent )
	{
		Text txtScriptlibFolder = null;

		// create folder selection group
		FolderSelectionGroup group = new FolderSelectionGroup( );
		group.setLabelText( BirtWTPMessages.BIRTConfiguration_scriptlib_label );
		group
				.setButtonText( BirtWTPMessages.BIRTConfiguration_folder_button_text );
		group
				.setDialogTitle( BirtWTPMessages.BIRTConfiguration_scriptlib_dialog_title );
		group
				.setDialogMessage( BirtWTPMessages.BIRTConfiguration_scriptlib_dialog_message );

		// set default value
		group.setTextValue( DataUtil.getString( WebArtifactUtil
				.getContextParamValue( properties,
						BIRT_SCRIPTLIB_FOLDER_SETTING ), false ) );

		group.create( parent );
		txtScriptlibFolder = group.getText( );

		// add modify listener
		txtScriptlibFolder.addModifyListener( new ModifyListener( ) {

			public void modifyText( ModifyEvent e )
			{
				WebArtifactUtil.setContextParamValue( properties,
						BIRT_SCRIPTLIB_FOLDER_SETTING, ( (Text) e.getSource( ) )
								.getText( ) );
			}
		} );

		return txtScriptlibFolder;
	}

	/**
	 * Create "BIRT_VIEWER_LOG_DIR" configuration group
	 * 
	 * @param composite
	 */
	public Text createLogFolderGroup( Composite parent )
	{
		Text txtLogFolder = null;

		// create folder selection group
		FolderSelectionGroup group = new FolderSelectionGroup( );
		group.setLabelText( BirtWTPMessages.BIRTConfiguration_log_label );
		group
				.setButtonText( BirtWTPMessages.BIRTConfiguration_folder_button_text );
		group
				.setDialogTitle( BirtWTPMessages.BIRTConfiguration_log_dialog_title );
		group
				.setDialogMessage( BirtWTPMessages.BIRTConfiguration_log_dialog_message );

		// set default value
		group.setTextValue( DataUtil.getString( WebArtifactUtil
				.getContextParamValue( properties, BIRT_LOG_FOLDER_SETTING ),
				false ) );

		group.create( parent );
		txtLogFolder = group.getText( );

		// add modify listener
		txtLogFolder.addModifyListener( new ModifyListener( ) {

			public void modifyText( ModifyEvent e )
			{
				WebArtifactUtil.setContextParamValue( properties,
						BIRT_LOG_FOLDER_SETTING, ( (Text) e.getSource( ) )
								.getText( ) );
			}
		} );

		return txtLogFolder;
	}

	/**
	 * Create "DOCUMENT_FOLDER_ACCESS_ONLY" configuration group
	 * 
	 * @param parent
	 */
	public Button createAccessOnlyGroup( Composite parent )
	{
		// checkbox for "DOCUMENT_FOLDER_ACCESS_ONLY" setting
		Button btAccessOnly = new Button( parent, SWT.CHECK );

		// set default value
		boolean defaultValue = DataUtil.getBoolean( WebArtifactUtil
				.getContextParamValue( properties,
						BIRT_DOCUMENT_ACCESSONLY_SETTING ) );

		btAccessOnly.setSelection( defaultValue );
		btAccessOnly
				.setText( BirtWTPMessages.BIRTConfiguration_document_access_message );
		btAccessOnly.setLayoutData( new GridData( GridData.END ) );
		btAccessOnly.addSelectionListener( new SelectionAdapter( ) {

			public void widgetSelected( SelectionEvent e )
			{
				WebArtifactUtil.setContextParamValue( properties,
						BIRT_DOCUMENT_ACCESSONLY_SETTING, new String(
								BLANK_STRING
										+ ( (Button) e.getSource( ) )
												.getSelection( ) ) );
			}
		} );

		return btAccessOnly;
	}

	/**
	 * Create "BIRT_OVERWRITE_DOCUMENT" configuration group
	 * 
	 * @param parent
	 */
	public Button createOverwriteGroup( Composite parent )
	{
		// checkbox for "BIRT_OVERWRITE_DOCUMENT" setting
		Button btOverwrite = new Button( parent, SWT.CHECK );

		// set default value
		boolean defaultValue = DataUtil.getBoolean( WebArtifactUtil
				.getContextParamValue( properties,
						BIRT_OVERWRITE_DOCUMENT_SETTING ) );

		btOverwrite.setSelection( defaultValue );
		btOverwrite
				.setText( BirtWTPMessages.BIRTConfiguration_overwrite_message );
		btOverwrite.setLayoutData( new GridData( GridData.END ) );
		btOverwrite.addSelectionListener( new SelectionAdapter( ) {

			public void widgetSelected( SelectionEvent e )
			{
				WebArtifactUtil.setContextParamValue( properties,
						BIRT_OVERWRITE_DOCUMENT_SETTING, new String(
								BLANK_STRING
										+ ( (Button) e.getSource( ) )
												.getSelection( ) ) );
			}
		} );

		return btOverwrite;
	}

	/**
	 * Create "BIRT_VIEWER_MAX_ROWS" configuration group
	 * 
	 * @param parent
	 */
	public Text createMaxRowsGroup( Composite parent )
	{
		Text txtMaxRows = null;

		Composite composite = new Composite( parent, SWT.NULL );
		GridLayout layout = new GridLayout( );
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 2;
		composite.setLayout( layout );

		GridData data = new GridData( GridData.FILL_HORIZONTAL );
		composite.setLayoutData( data );

		// get font
		Font font = parent.getFont( );

		Label label = new Label( composite, SWT.LEFT );
		label.setFont( font );
		label.setText( BirtWTPMessages.BIRTConfiguration_maxrows_label );

		txtMaxRows = new Text( composite, SWT.BORDER );
		txtMaxRows.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
		txtMaxRows.setFont( font );
		txtMaxRows.setTextLimit( Integer.toString( MAX_MAX_ROWS ).length( ) );

		// set default value
		int defaultValue = DataUtil.getInt( WebArtifactUtil
				.getContextParamValue( properties, BIRT_MAX_ROWS_SETTING ) );
		if ( defaultValue > 0 )
			txtMaxRows.setText( "" + defaultValue ); //$NON-NLS-1$

		txtMaxRows.addVerifyListener( new VerifyListener( ) {

			public void verifyText( VerifyEvent e )
			{
				e.doit = e.text.matches( "[0-9]*" ); //$NON-NLS-1$
			}
		} );

		// add modify listener
		txtMaxRows.addModifyListener( new ModifyListener( ) {

			public void modifyText( ModifyEvent e )
			{
				WebArtifactUtil.setContextParamValue( properties,
						BIRT_MAX_ROWS_SETTING, DataUtil.getMaxRows( ( (Text) e
								.getSource( ) ).getText( ) ) );
			}
		} );

		return txtMaxRows;
	}

	/**
	 * Create "BIRT_VIEWER_LOG_LEVEL" configuration group
	 * 
	 * @param parent
	 */
	public Combo createLogLevelGroup( Composite parent )
	{
		Combo cbLogLevel = null;

		Composite composite = new Composite( parent, SWT.NULL );
		GridLayout layout = new GridLayout( );
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 2;
		composite.setLayout( layout );

		GridData data = new GridData( GridData.HORIZONTAL_ALIGN_BEGINNING );
		composite.setLayoutData( data );

		// get font
		Font font = parent.getFont( );

		Label label = new Label( composite, SWT.LEFT );
		label.setFont( font );
		label.setText( BirtWTPMessages.BIRTConfiguration_loglevel_label );

		cbLogLevel = new Combo( composite, SWT.H_SCROLL );
		cbLogLevel.setFont( font );
		cbLogLevel.setItems( BirtWizardUtil.getLogLevels( ) );

		// set default value
		cbLogLevel.setText( DataUtil.getString( WebArtifactUtil
				.getContextParamValue( properties, BIRT_LOG_LEVEL_SETTING ),
				false ) );

		// Add Selection Listener
		cbLogLevel.addSelectionListener( new SelectionListener( ) {

			public void widgetDefaultSelected( SelectionEvent e )
			{
			}

			public void widgetSelected( SelectionEvent e )
			{
				WebArtifactUtil.setContextParamValue( properties,
						BIRT_LOG_LEVEL_SETTING, ( (Combo) e.getSource( ) )
								.getText( ) );
			}
		} );

		return cbLogLevel;
	}

	/**
	 * Create Import Clear Setting configuration group
	 * 
	 * @param parent
	 */
	public Button createImportClearSetting( Composite parent )
	{
		// checkbox for Import Clear setting
		Button btClear = new Button( parent, SWT.CHECK );

		btClear.setSelection( true );
		btClear
				.setText( BirtWTPMessages.BIRTConfiguration_import_clear_message );
		btClear.setLayoutData( new GridData( GridData.END ) );

		return btClear;
	}

	/**
	 * @return the properties
	 */
	public Map getProperties( )
	{
		return properties;
	}
}
