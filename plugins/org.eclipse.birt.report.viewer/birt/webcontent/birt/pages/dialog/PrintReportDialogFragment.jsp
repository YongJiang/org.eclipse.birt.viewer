<%-----------------------------------------------------------------------------
	Copyright (c) 2004 Actuate Corporation and others.
	All rights reserved. This program and the accompanying materials 
	are made available under the terms of the Eclipse Public License v1.0
	which accompanies this distribution, and is available at
	http://www.eclipse.org/legal/epl-v10.html
	
	Contributors:
		Actuate Corporation - Initial implementation.
-----------------------------------------------------------------------------%>
<%@ page contentType="text/html; charset=utf-8"%>
<%@ page session="false" buffer="none"%>
<%@ page import="org.eclipse.birt.report.presentation.aggregation.IFragment,
				 org.eclipse.birt.report.IBirtConstants,
				 java.util.ArrayList,
				 java.util.Map,
				 org.eclipse.birt.report.utility.Printer,
				 org.eclipse.birt.report.utility.DataUtil,
				 org.eclipse.birt.report.utility.PrintUtility,
				 org.eclipse.birt.report.utility.ParameterAccessor,
				 org.eclipse.birt.report.resource.BirtResources"%>

<%-----------------------------------------------------------------------------
	Expected java beans
-----------------------------------------------------------------------------%>
<jsp:useBean id="fragment" type="org.eclipse.birt.report.presentation.aggregation.IFragment" scope="request" />

<SCRIPT LANGUAGE="javascript">var index = 0;</SCRIPT>
<%
	boolean enable = ParameterAccessor.isSupportedPrintOnServer;
	if( enable )
	{
		String[] supportedFormats = ParameterAccessor.supportedFormats;
		for( int i=0; i<supportedFormats.length; i++ )
		{
			if( IBirtConstants.POSTSCRIPT_RENDER_FORMAT.equalsIgnoreCase( supportedFormats[i] ) )
			{
				enable = true;
				break;
			}
		}
	}
	
	if( enable )
	{
		ArrayList printers = (ArrayList)PrintUtility.findPrinters();
		for( int i=0; i<printers.size( ); i++ )
		{
			Printer bean = (Printer)printers.get( i );
			String name = PrintUtility.handleSlash( bean.getName( ) );
			String status = DataUtil.trimString( BirtResources.getMessage( bean.getStatus( ) ) );
			String model = DataUtil.trimString( bean.getModel( ) );
			String info = DataUtil.trimString( bean.getInfo( ) );
			String copies = "" + bean.getCopies( );
			String mode = "" + bean.getMode( );
			String duplex = "" + bean.getDuplex( );
			String mediaSize = DataUtil.trimString( bean.getMediaSize( ) );
			Map map = bean.getMediaSizeNames( );
			Object[] mediaSizeNames = map.keySet( ).toArray( );
%>
			<SCRIPT LANGUAGE="javascript">
				var printer = new Printer( );
				printer.setName( "<%= name %>" );
				printer.setStatus( "<%= status %>" );
				printer.setModel( "<%= model %>" );
				printer.setInfo( "<%= info %>" );
				
				// Copies attribute
				<%
				if( bean.isCopiesSupported() )
				{
				%>
				printer.setCopiesSupported( true );
				printer.setCopies( "<%= copies %>" );
				<%
				}
				else
				{
				%>	
				printer.setCopiesSupported( false );
				<%
				}
				%>
				
				// Collate attribute
				<%
				if( bean.isCollateSupported() )
				{
				%>
				printer.setCollateSupported( true );
					<%
					if( bean.isCollate( ) )
					{
					%>
				printer.setCollate( true );
					<%
					}
					else
					{
					%>
				printer.setCollate( false );	
				<%
					}
				}
				else
				{
				%>	
				printer.setCopiesSupported( false );
				<%
				}
				%>
				
				// Mode attribute
				<%
				if( bean.isModeSupported( ) )
				{
				%>
				printer.setModeSupported( true );
				printer.setMode( "<%= mode %>" );
				<%
				}
				else
				{
				%>	
				printer.setModeSupported( false );
				<%
				}
				%>				

				// Duplex attribute
				<%
				if( bean.isDuplexSupported( ) )
				{
				%>
				printer.setDuplexSupported( true );
				printer.setDuplex( "<%= duplex %>" );
				<%
				}
				else
				{
				%>	
				printer.setDuplexSupported( false );
				<%
				}
				%>	
				
				// Media attribute
				<%
				if( bean.isMediaSupported( ) )
				{
				%>
				printer.setMediaSupported( true );
				printer.setMediaSize( "<%= mediaSize %>" );
					<%
					for( int j=0; j<mediaSizeNames.length; j++ )
					{
						String mediaSizeName = DataUtil.trimString( (String)mediaSizeNames[j] );
					%>
				printer.addMediaSizeName( "<%= mediaSizeName %>" );						
				<%
					}
				}
				else
				{
				%>	
				printer.setMediaSupported( false );
				<%
				}
				%>	
				
				if( !printers[index] )								
					printers[index] = {};
					
				printers[index].name = printer.getName( );
				printers[index].value = printer;
				
				index++;
				
			</SCRIPT>
<%		
		}
	}	
%>
<%-----------------------------------------------------------------------------
	Print report dialog fragment
-----------------------------------------------------------------------------%>
<TABLE CELLSPACING="2" CELLPADDING="2" CLASS="birtviewer_dialog_body">
	<TR HEIGHT="5px"><TD></TD></TR>
	<TR>
		<TD>
			<INPUT TYPE="checkbox" ID="print_onserver" <%if( !enable ) { %>DISABLED="true"<%}%>/>
			<%=BirtResources.getMessage( "birt.viewer.dialog.print.onserver" )%>
		</TD>
	</TR>
	<TR HEIGHT="5px"><TD></TD></TR>
	<TR>
		<TD>
			<TABLE WIDTH="100%" ID="printer_general">
				<TR>
					<TD WIDTH="80px"><%=BirtResources.getMessage( "birt.viewer.dialog.print.printer" )%></TD>
					<TD>						
						<SELECT ID="printer" CLASS="birtviewer_printreport_dialog_select"></SELECT>
					</TD>
				</TR>
				<TR>
					<TD><%=BirtResources.getMessage( "birt.viewer.dialog.print.status" )%></TD>
					<TD><LABEL ID="printer_status"></LABEL></TD>
				</TR>
				<TR>
					<TD><%=BirtResources.getMessage( "birt.viewer.dialog.print.model" )%></TD>
					<TD><LABEL ID="printer_model"></LABEL></TD>
				</TR>
				<TR>
					<TD><%=BirtResources.getMessage( "birt.viewer.dialog.print.description" )%></TD>
					<TD><LABEL ID="printer_description"></LABEL></TD>
				</TR>
			</TABLE>
		</TD>
	</TR>			
	<TR HEIGHT="5px"><TD><HR/></TD></TR>
	<TR>
		<TD><%=BirtResources.getMessage( "birt.viewer.dialog.print.settings" )%></TD>
	</TR>	
	<TR>
		<TD>
			<TABLE WIDTH="100%" ID="printer_config">
				<TR>
					<TD WIDTH="100px">
						<%=BirtResources.getMessage( "birt.viewer.dialog.print.settings.copies" )%>
					</TD>
					<TD>
						<INPUT TYPE="text" CLASS="birtviewer_printreport_dialog_input_short" ID="printer_copies"/>
						&nbsp;&nbsp;<%=BirtResources.getMessage( "birt.viewer.dialog.print.settings.collate" )%>&nbsp;&nbsp;<INPUT TYPE="checkbox" ID="printer_collate"/>						
					</TD>
				</TR>	
				<TR>
					<TD>
						<%=BirtResources.getMessage( "birt.viewer.dialog.print.settings.duplex" )%>
					</TD>
					<TD>						
						<INPUT TYPE="radio" ID="printer_duplexSimplex" NAME="printerDuplex"/><%=BirtResources.getMessage( "birt.viewer.dialog.print.settings.duplex.simplex" )%>
						&nbsp;&nbsp;<INPUT TYPE="radio" ID="printer_duplexHorz" NAME="printerDuplex"/><%=BirtResources.getMessage( "birt.viewer.dialog.print.settings.duplex.horizontal" )%>
						&nbsp;&nbsp;<INPUT TYPE="radio" ID="printer_duplexVert" NAME="printerDuplex"/><%=BirtResources.getMessage( "birt.viewer.dialog.print.settings.duplex.vertical" )%>			
					</TD>
				</TR>
				<TR>
					<TD>
						<%=BirtResources.getMessage( "birt.viewer.dialog.print.settings.mode" )%>
					</TD>
					<TD>						
						<INPUT TYPE="radio" ID="printer_modeBW" NAME="printerMode"/><%=BirtResources.getMessage( "birt.viewer.dialog.print.settings.mode.bw" )%>
						&nbsp;&nbsp;<INPUT TYPE="radio" ID="printer_modeColor" NAME="printerMode"/><%=BirtResources.getMessage( "birt.viewer.dialog.print.settings.mode.color" )%>			
					</TD>
				</TR>
				<TR>
					<TD>
						<%=BirtResources.getMessage( "birt.viewer.dialog.print.settings.pagesize" )%>
					</TD>				
					<TD>						
						<SELECT ID="printer_mediasize" CLASS="birtviewer_printreport_dialog_select"></SELECT>
					</TD>
				</TR>					
			</TABLE>
		</TD>
	</TR>	
	<TR HEIGHT="5px"><TD><HR/></TD></TR>
	<TR>
		<TD> 
			<DIV ID="printPageSetting">
				Print:
				&nbsp;&nbsp;<INPUT TYPE="radio" ID="printPageAll" NAME="printPages" CHECKED/><%=BirtResources.getMessage( "birt.viewer.dialog.page.all" )%>
				&nbsp;&nbsp;<INPUT TYPE="radio" ID="printPageCurrent" NAME="printPages"/><%=BirtResources.getMessage( "birt.viewer.dialog.page.current" )%>
				&nbsp;&nbsp;<INPUT TYPE="radio" ID="printPageRange" NAME="printPages"/><%=BirtResources.getMessage( "birt.viewer.dialog.page.range" )%>
				&nbsp;&nbsp;<INPUT TYPE="text" CLASS="birtviewer_printreport_dialog_input" ID="printPageRange_input"/>
			</DIV>						
		</TD>
	</TR>	
	<TR>
		<TD>&nbsp;&nbsp;<%=BirtResources.getMessage( "birt.viewer.dialog.page.range.description" )%></TD>
	</TR>	
	<TR HEIGHT="5px"><TD><HR/></TD></TR>
	<TR>
		<TD>
			<DIV ID="printFitSetting">
				<INPUT TYPE="radio" ID="printFitToActual" NAME="printFit" CHECKED/><%=BirtResources.getMessage( "birt.viewer.dialog.export.pdf.fittoactual" )%>
				&nbsp;&nbsp;<INPUT TYPE="radio" ID="printFitToWidth" NAME="printFit"/><%=BirtResources.getMessage( "birt.viewer.dialog.export.pdf.fittowidth" )%>
				&nbsp;&nbsp;<INPUT TYPE="radio" ID="printFitToWhole" NAME="printFit"/><%=BirtResources.getMessage( "birt.viewer.dialog.export.pdf.fittowhole" )%>
			</DIV>			
		</TD>
	</TR>
	<TR HEIGHT="5px"><TD></TD></TR>	
</TABLE>
