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
 * All shared utility functions should be put here.
 */

BirtUtility = function( ) { };

BirtUtility.prototype =
{
	/**
	 * @returns true if left button was pressed
	 */
	isLeftButton : function( event )
	{
		return Event.isLeftClick( event ); //prototype library function	
	},
	
	/**
	 * Recursively removes all element child nodes
	 * @param element DOM element
	 */
	removeChildren : function( element )
	{
		while( element.childNodes.length > 0 )
		{
			if( element.firstChild.childNodes.length > 0 )
			{
				this.removeChildren( element.firstChild );
			}
			element.removeChild( element.firstChild );
		}
	},
	
	/**
	 * @returns viewport height minus horizontal scroll bar if present.
	 */
	clientHeight : function( )
	{
		return document.body.clientHeight;
	},
	
	/**
	 * @returns viewport width minus vertical scroll bar if present.
	 */
	clientWidth : function( )
	{
		return document.body.clientWidth;
	},

	haveTagName : function( domTree, tag )
	{
		return ( ( domTree.getElementsByTagName( tag ).length == 0 ) ? false : true );
	},

	/**
	 * It returns the data value of a single DOM leaf node
	 */
	getDataReponseLeaf : function( leaf )
	{
		return( leaf[0].firstChild.data );
	},
	
	/**
	 * It is constructor function for creating object based on the leaf node of SOAP response DOM tree 
	 * The idea is from the Element function of WRScrollLayout.js
	 * @param it - leaf node of SOAP response DOM tree
	 * @returns object that consists of content of the input "item"
	 */	
	extractResponseLeaf : function( item, newObject )
	{
		var children = item.childNodes;
		var size = children.length;
		for( var i = 0; i < size; i++ )
		{	
			if( children[i].nodeType == "1" )
			{
				var childi = children[i];
				var nName = children[i].nodeName;
				var nData = null;
				if( children[i].firstChild && children[i].firstChild.data )
				{
					nData = children[i].firstChild.data;					
				}
				newObject[ nName ] = nData;			
			}	
		}		
	},

	/**
	 * It is recursive function to find all text contents under a DOM node.
	 * @param node - root node of the DOM tree. We traverse recursively from here.
	 * @param resultArray - The result are stored in this array during tree traversal.
	 * @returns void
	 */	
	findAllTextchild : function( node, resultArray )
	{
		if ( node.nodeType == 3 )
		{
			// Text node
			resultArray[ resultArray.length ] = node.data;
		}
		else
		{
			// Recursively traverse the tree
			var kids = node.childNodes;
			for ( var i = 0; i < node.childNodes.length; i++ )
			{
				this.findAllTextchild( kids[i], resultArray );
			}
		}
	},
	
	setTextContent : function( node, textString )
	{

		if ( node.textContent != undefined )
		{
			// For example, Firefox.
			node.textContent = textString;
		}
		else if ( node.innerText != undefined )
		{
			// For example, IE.
			node.innerText = textString;
		}
	},
	
	/**
	 * Fire a simulated mouse event on an html element
	 * @param element html element to fire event on
	 * @param eventName - name of mouse event such as 'click' or 'mousedown'
	 * @param ctrlKey - optional,if present, event will be fired with control key pressed	
	 */
	fireMouseEvent : function( element, eventName, ctrlKey )
	{
		var evt;
		var ctrl = ctrlKey || false;
		if( element.fireEvent )
		{
			evt = document.createEventObject( );
			evt.ctrlKey = ctrl;
			evt.button = 1;
			element.fireEvent( "on" + eventName, evt );
		}
		else if( element.dispatchEvent )
		{
			evt = document.createEvent( "MouseEvents" );
			evt.initMouseEvent( eventName, false, 
				true, undefined, undefined,
				0, 0, 0, 0,
				ctrl,
				false,
				false, false, 1,
				undefined );
			element.dispatchEvent( evt );
		}
		else
		{
			return null;
		}
		return evt;
	},

	/**
	@returns boolean true if checked, false otherwise
	*/
	setCheckBoxChecked : function( checkbox, checked )
	{
		// Both IE and Firefox have defaultChecked property. But they use it differently.
		// On Firefox, the GUI box is checked when checkbox.checked is true.
		// It is not true for IE. On IE, we also need to set checkbox.defaultChecked.
		// I guess when the checkbox is FIRST shown, IE uses checkbox.defaultChecked to determine the GUI.
		checkbox.defaultChecked = checked;
		checkbox.checked = checked;
		return checked;
	},

	/**
	 * Get a parameter specified in the URL. For example, if a URL is
	 * http://localhost:8080/iportal/wr?__report=/16.webrptdesign&iPortalID=YPTDAGCNPOYSOS
	 * getURLParameter(iPortalID) will return &iPortalID=YPTDAGCNPOYSOS
	 */
	getURLParameter: function ( url, parameterName )
	{
		var paramString = "";
		var paramStartIndex = url.indexOf( parameterName );
		if ( paramStartIndex >= 0 )
		{
			var paramEndIndex = url.indexOf( "&", paramStartIndex );
			if ( paramEndIndex >= 0 )
			{
				paramString = "&" + url.substring( paramStartIndex, paramEndIndex );
			}
			else
			{
				paramString = "&" + url.substring( paramStartIndex ); // get the substring till the end.					
			}
		}
		return paramString;
	},
	
	/**
	 * Modify the URL to change to the new report identified by newReportPath. The original locale will be kept.
	 * @param newReportPath - the path of the report to be opened, e.g., /iportal/bizRD/report1.webrptdesign
	 */
	changeReport: function ( newReportPath )
	{
		var url = document.location.href;
		var parameterIndex = url.indexOf( "?" );
	
		if ( parameterIndex >= 0 )
		{
			localeString = this.getURLParameter( url, "__locale" );
			iPortalIDString = this.getURLParameter( url, "iPortalID" );
			url = url.substring( 0, parameterIndex ) + "?__report=" + newReportPath + localeString + iPortalIDString;
			window.location = url;
		}	
	},

	/**
	 * Insert a string into the cursor position of a textarea.<b> 
	 * textarea: DOM element of textarea to be inserted.
	 * string: string to be inserted into the textarea.
	 */	
	insertStringCursorPosTextarea: function( textarea, string )
	{
		if ( textarea.selectionStart != undefined )
		{
			// Firefox
			var startPos = textarea.selectionStart;
			var endPos = textarea.selectionEnd;
			textarea.value = textarea.value.substring( 0, startPos )+ string + textarea.value.substring( endPos, textarea.value.length );			
		}
		else
		{
			// IE
			textarea.focus();
			var range = document.selection.createRange();
			range.text = string;
		}
	},

	/**
	 * Insert a string into the specified position of a textarea.<b> 
	 * textarea: DOM element of textarea to be inserted.
	 * pos: For Firefox, it is an integer. For IE, it is an TextRange object.
	 * string: string to be inserted into the textarea.
	 */	
	insertStringTextarea: function( textarea, pos, string )
	{
		if ( textarea.selectionStart != undefined )
		{
			// Firefox
			
			if ( pos == undefined || pos == null )
				pos = 0;
				
			textarea.value = textarea.value.substring( 0, pos )+ string + textarea.value.substring( pos, textarea.value.length );		
		}
		else
		{
			// IE
			
			textarea.focus( );
			
			if (pos == undefined || pos == null )
				pos = document.selection.createRange();
			
			pos.text = string;	
		}
	},
	
	// get the cursor position of the textarea
	getCursorPosTextarea: function( textarea )
	{
		if ( textarea.selectionStart != undefined )
		{
			// Firefox
			return( textarea.selectionEnd );		
		}
		else
		{
			// IE
			textarea.focus( );
			return( document.selection.createRange( ) );
		}
	},	
	
	// IE and Firefox behave so differently to insert a string into cursor position of text area.
	// So, we create this preferred method. This method subsequently call other methods of string insertion based on browser type.
	preferredInsertStringTextarea: function( textarea, pos, string )
	{
		if ( textarea.selectionStart != undefined )
		{
			// Firefox
			this.insertStringCursorPosTextarea( textarea, string );		
		}
		else
		{
			// IE
			this.insertStringTextarea( textarea, pos, string );			
		}		
	},
	
	// id: tableId
	getSelectedTableColumns: function( id )
	{
 		var handlerObject = ReportComponentIdRegistry.getObjectForId(id);
 		var tableInstance = handlerObject.selectionManager.getTableInstanceById(id);
 		var table = $( id );
 		var iid = table.iid;
 		
 		//check that there is at least one column selected		
 		var selectedColumns = tableInstance.getRLOrderedSelectedColumns();
 		if( !selectedColumns )
 		{
 			// #IV TODO integrate with IV error handling
 			throw new WRError("WRReportTable", "Must have one or more columns to apply format");
 		}
 		
 		return selectedColumns;
 	},
 	
	// trim left blanks
	ltrim: function ( str )
	{
		return str.replace( /^\s*/, '');
	},
	
	// trim right blanks
	rtrim: function ( str )
	{
		return str.replace( /\s*$/, ''); 
	},

	// trim left and right blanks
	trim: function ( str )
	{
		return this.rtrim( this.ltrim( str ) );
	},
	
	// set button if disabled
	setButtonsDisabled: function ( target, flag )
	{
		if ( !target )
			return;
			
		var oTarget = document.getElementById( target );
		var oIEC;
		
		if ( oTarget )
			oIEC = oTarget.getElementsByTagName( "INPUT" );
			
		if ( oIEC )
		{
			for( var i = 0; i < oIEC.length; i++ )
			{
				oIEC[i].disabled = flag;
			}		
		}
	},
		
	noComma : "" //just to avoid javascript syntax errors
}

var birtUtility = new BirtUtility( );

/**
 *	Extend prototype's Event.
 *	TODO: probably need a prototype extension.
 */
Event.prototype = Object.extend( Event,
{
	/**
	 *	Extension to prototype 'Event' since Event.stop(event) isn't
	 *	stopping in ie
	 */
	stop: function( event )
	{
		event.cancelBubble = true;
		
		if ( event.preventDefault )
		{ 
			event.preventDefault( );
			event.stopPropagation( );
	    }
	    else
	    {
			event.returnValue = false;
	    }
	},
	
	/**
	 *	Stops click from propigating without using .bindAsEventListener
	 */
	colClickStop: function( e )
	{
 		if (!e) var e = $("Document").contentWindow.event;
		debug( e.type);
		Event.stop( e );
	}
});