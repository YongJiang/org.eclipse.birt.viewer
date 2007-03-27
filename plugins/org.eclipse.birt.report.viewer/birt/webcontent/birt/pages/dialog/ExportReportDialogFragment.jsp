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
				 org.eclipse.birt.report.utility.ParameterAccessor,
				 org.eclipse.birt.report.resource.BirtResources"%>

<%-----------------------------------------------------------------------------
	Expected java beans
-----------------------------------------------------------------------------%>
<jsp:useBean id="fragment" type="org.eclipse.birt.report.presentation.aggregation.IFragment" scope="request" />

<%
	String[] supportedFormats = ParameterAccessor.supportedFormats;
%>
<%-----------------------------------------------------------------------------
	Export report dialog fragment
-----------------------------------------------------------------------------%>
<TABLE CELLSPACING="2" CELLPADDING="2" CLASS="birtviewer_dialog_body">
	<TR HEIGHT="5px"><TD></TD></TR>
	<TR>
		<TD>
		<%=BirtResources.getMessage( "birt.viewer.dialog.export.format" )%>
		<SELECT	ID="exportFormat" NAME="format" CLASS="birtviewer_exportreport_dialog_select">
			<%
				for ( int i = 0; i < supportedFormats.length; i++ )
				{
					if ( !ParameterAccessor.PARAM_FORMAT_HTML.equalsIgnoreCase( supportedFormats[i] ) )
					{
			%>
						<OPTION VALUE="<%= supportedFormats[i] %>"><%=supportedFormats[i]%></OPTION>
			<%
					}
				}
			%>
		</SELECT>
		</TD>
	</TR>
	<TR HEIGHT="5px"><TD></TD></TR>
	<TR>
		<TD>
			<DIV ID="exportPageSetting">
				<INPUT TYPE="radio" ID="exportPageAll" NAME="exportPages" CHECKED/><%=BirtResources.getMessage( "birt.viewer.dialog.page.all" )%>
				&nbsp;&nbsp;<INPUT TYPE="radio" ID="exportPageCurrent" NAME="exportPages"/><%=BirtResources.getMessage( "birt.viewer.dialog.page.current" )%>
				&nbsp;&nbsp;<INPUT TYPE="radio" ID="exportPageRange" NAME="exportPages"/><%=BirtResources.getMessage( "birt.viewer.dialog.page.range" )%>
				&nbsp;&nbsp;<INPUT TYPE="text" CLASS="birtviewer_exportreport_dialog_input" ID="exportPageRange_input" DISABLED="true"/>
			</DIV>
		</TD>
	</TR>
	<TR>
		<TD>&nbsp;&nbsp;<%=BirtResources.getMessage( "birt.viewer.dialog.page.range.description" )%></TD>
	</TR>
	<TR HEIGHT="5px"><TD><HR/></TD></TR>
	<TR>
		<TD>
			<DIV ID="exportFitSetting">
				<INPUT TYPE="radio" ID="exportFitToActual" NAME="exportFit" CHECKED/><%=BirtResources.getMessage( "birt.viewer.dialog.export.pdf.fittoactual" )%>
				&nbsp;&nbsp;<INPUT TYPE="radio" ID="exportFitToWidth" NAME="exportFit"/><%=BirtResources.getMessage( "birt.viewer.dialog.export.pdf.fittowidth" )%>
				&nbsp;&nbsp;<INPUT TYPE="radio" ID="exportFitToWhole" NAME="exportFit"/><%=BirtResources.getMessage( "birt.viewer.dialog.export.pdf.fittowhole" )%>
			</DIV>			
		</TD>
	</TR>
	<TR HEIGHT="5px"><TD></TD></TR>
</TABLE>
