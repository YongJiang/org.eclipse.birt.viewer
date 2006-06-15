/******************************************************************************
 *	Copyright (c) 2004 Actuate Corporation and others.
 *	All rights reserved. This program and the accompanying materials 
 *	are made available under the terms of the Eclipse Public License v1.0
 *	which accompanies this distribution, and is available at
 *		http://www.eclipse.org/legal/epl-v10.html
 *	
 *	Contributors:
 *		Actuate Corporation - Initial implementation.
 *****************************************************************************/
 
/**
 *	BirtParameterDialog
 *	...
 */
BirtParameterDialog = Class.create( );

BirtParameterDialog.prototype = Object.extend( new AbstractParameterDialog( ),
{
	/**
	 *	Parameter dialog working state. Whether embedded inside
	 *	designer dialog.
	 */
	__mode : 'frameset',

	/**
	 *	Identify the parameter is null.
	 */
	__isnull : '__isnull',
	
	/**
	 *	Initialization routine required by "ProtoType" lib.
	 *	@return, void
	 */
	initialize : function( id, mode )
	{
		this.initializeBase( id );
		this.__mode = mode;
		
		if ( this.__mode == 'parameter' )
		{
			// Hide dialog title bar if embedded in designer.
			var paramDialogTitleBar = $( id + 'dialogTitleBar' );
			paramDialogTitleBar.style.display = 'none';			
		}
	},

	/**
	 *	Binding data to the dialog UI. Data includes zoom scaling factor.
	 *	@data, data DOM tree (schema TBD)
	 *	@return, void
	 */
	__bind : function( data )
	{
		if ( !data )
		{
			return;
		}
		
		var cascadeParamObj = data.getElementsByTagName( 'CascadeParameter' );
		var confirmObj = data.getElementsByTagName( 'Confirmation' );
		if ( cascadeParamObj.length > 0 )
		{
			this.__propogateCascadeParameter( data );
		}
		else if ( confirmObj.length > 0 )
		{
			this.__close( );
		}
	},

	/**
	 *	Collect parameters, include five cases appear in Birt1.0 Viewer.
	 *
	 *	@return, void
	 */
	collect_parameter : function( )
	{
		// Clear parameter array
		this.__parameter = new Array( );		
				
		var k = 0;
		//oTRC[i] is <tr></tr> section
		var oTRC = document.getElementById( "parameter_table" ).getElementsByTagName( "TR" );
		for( var i = 0; i < oTRC.length; i++ )
		{
			if( !this.__parameter[k] )
			{
				this.__parameter[k] = { };
			}
			
			//input element collection
			var oIEC = oTRC[i].getElementsByTagName( "input" );
			//select element collection
			var oSEC = oTRC[i].getElementsByTagName( "select" );
			//avoid group parameter
			var oTable = oTRC[i].getElementsByTagName( "table" );
			if( oTable.length > 0 || ( oSEC.length == 0 && oIEC.length == 0 ) || ( oIEC.length == 1 && oIEC[0].type == 'submit' ) )
			{
				continue;
			}
			
			if( oSEC.length == 1 && oIEC.length <= 1 )
			{
				// deal with "select" parameter
				if( oIEC.length == 1 )
				{
					this.__parameter[k].name = oIEC[0].name;
				}
				else
				{
					this.__parameter[k].name = oSEC[0].name;
				}
				this.__parameter[k].value = oSEC[0].options[oSEC[0].selectedIndex].value;
				k++;
			}
			
			if( oSEC.length == 0 && ( oIEC.length == 1 || oIEC.length == 2 ) )
			{
				var temp = {};
				var tempDef = null;
				if( oIEC.length == 1 )
				{
					temp = oIEC[0]
				}
				else if( oIEC[0].type == 'hidden' )
				{
					tempDef = oIEC[0]
					temp = oIEC[1];
				}
				else
				{
					continue;
				}
				
				if( temp.type == 'text' || temp.type == 'password' )
				{
					// deal with "text" parameter
					this.__parameter[k].name = temp.name;
					// if the parameter neither has a value nor a default value, error
					if( birtUtility.trim( temp.value ) == '' )
					{
						if( tempDef )
						{
							temp.focus( );
							alert( temp.name + ' cannot be blank' );
							return false;
						}
						else
						{
							this.__parameter[k].value = temp.value;
						}
					}
					else
					{
						this.__parameter[k].value = temp.value;
					}
					k++;
				}
				else if( temp.type == 'checkbox' )
				{
					// deal with checkbox
					this.__parameter[k].name = temp.value;
					temp.checked?this.__parameter[k].value = 'true':this.__parameter[k].value = 'false';  
					k++;
				}
				else
				{
					//handle more cases
				}
			}
			else if( oSEC.length <= 1 && oIEC.length > 2 )
			{
				for( var j = 0; j < oIEC.length; j++ )
				{
					// deal with radio
					if( oIEC[j].type == 'radio' && oIEC[j].checked )
					{
						if( oIEC[j+1] && ( oIEC[j+1].type == 'text' || oIEC[j+1].type == 'password' ) )
						{
							//Check if allow blank
							var temp = oIEC[j+2];
							if ( temp && temp.value == 'true' )
							{
								if ( birtUtility.trim( oIEC[j+1].value ) == '' )
								{
									oIEC[j+1].focus( );
									alert( temp.name + ' cannot be blank' );
									return false;
								}
							}
							
							// deal with radio box with textarea or password area
							if( oIEC[j+1].name && oIEC[j+1].value )
							{
								this.__parameter[k].name = oIEC[j+1].name
								this.__parameter[k].value = oIEC[j+1].value;
								k++;
							}
							else
							{
								this.__parameter[k].name = oIEC[j].value
								this.__parameter[k].value = oIEC[j+1].value;
								//oIEC[j+1].value = "";
								k++;	            
							}
						}
						else if( oSEC[0] )
						{
							// deal with "select" parameter							
							if ( oSEC[0].selectedIndex == -1 )
							{
								oSEC[0].focus( );
								alert( oIEC[j].value + " should have a value" );
								return false;
							}
							
							var temp = oSEC[0].options[oSEC[0].selectedIndex].text;
							if ( !temp )
							{
								oSEC[0].focus( );
								alert( oIEC[j].value + " should have a value" );
								return false;								
							}
							
							// Check if select 'Null Value' option								
							if( temp && temp != 'Null Value' )
							{	
								this.__parameter[k].name = oIEC[j].value;							
								this.__parameter[k].value = oSEC[0].options[oSEC[0].selectedIndex].value;
								k++;
							}
							else
							{
								this.__parameter[k].name = this.__isnull;
								this.__parameter[k].value = oIEC[j].value;
								k++;								
							}
						}
						else if( !oIEC[j+1] && !oIEC[j].name )
						{
							//deal with common radio with null value
							this.__parameter[k].name = this.__isnull;
							this.__parameter[k].value = oIEC[j-1].name;
							k++;
						}
						else
						{
							//deal with common radio
							this.__parameter[k].name = oIEC[j].name;
							this.__parameter[k].value = oIEC[j].value;
							k++;
						}
					}
				}
			}
		}
		return true;
	},
	
	/**
	 *	Handle clicking on okRun.
	 *
	 *	@event, incoming browser native event
	 *	@return, void
	 */
	__okPress : function( )
	{
		if( birtParameterDialog.collect_parameter( ) )
		{
			if ( this.__mode == 'parameter' )
			{
				birtEventDispatcher.broadcastEvent( birtEvent.__E_CACHE_PARAMETER );
			}
			else if ( this.__mode == 'run' )
			{
				this.__doSubmit( );
			}
			else
			{
				birtEventDispatcher.broadcastEvent( birtEvent.__E_CHANGE_PARAMETER );
				this.__l_hide( );
			}
		}
	},
	
	/**
	 *	Override cancel button click.
	 */
	__neh_cancel : function( )
	{
		if ( this.__mode == 'parameter' )
		{
			this.__cancel();
		}
		else
		{
			this.__l_hide( );
		}
	},

	/**
	 *	Handle submit form with current parameters.
	 *
	 *	@return, void
	 */
	__doSubmit : function( )
	{
		var action = window.location.href;
		
		var divObj = document.createElement( "DIV" );
		document.body.appendChild( divObj );
		divObj.style.display = "none";
		
		var formObj = document.createElement( "FORM" );
		divObj.appendChild( formObj );
		
		if ( this.__parameter != null )
		{
			for( var i = 0; i < this.__parameter.length; i++ )	
			{
				var param = document.createElement( "INPUT" );
				formObj.appendChild( param );
				param.TYPE = "HIDDEN";
				param.name = this.__parameter[i].name;
				param.value = this.__parameter[i].value;
				
				//replace the URL parameter			
				var reg = new RegExp( "&" + param.name + "[^&]*&*", "g" );
				action = action.replace( reg, "&" );
			}
		}

		formObj.action = action;
		formObj.method = "post";
				
		this.__l_hide( );
		formObj.submit( );		
	},

	/**
	 *	Caching parameters success, close window.
	 *
	 *	@return, void
	 */	
	__close : function( )
	{
		if ( BrowserUtility.__isIE( ) )
		{
			window.opener = null;
			window.close( );
		}
		else
		{
			window.status = "close";
		}
	},
	
	/**
	 *	Click 'Cancel', close window.
	 *
	 *	@return, void
	 */	
	__cancel : function( )
	{
		window.status = "cancel";
	},

	/**
	Called right before element is shown
	*/
	__preShow: function()
	{
		// disable the toolbar buttons
		birtUtility.setButtonsDisabled ( "toolbar", true );
		
		// disable the Navigation Bar buttons
		birtUtility.setButtonsDisabled ( "navigationBar", true );
	},
	
	/**
	Called before element is hidden
	*/
	__preHide: function()
	{
		// enable the toolbar buttons
		birtUtility.setButtonsDisabled ( "toolbar", false );
		
		// enable the Navigation Bar buttons
		birtUtility.setButtonsDisabled ( "navigationBar", false );		
	}	
}
);