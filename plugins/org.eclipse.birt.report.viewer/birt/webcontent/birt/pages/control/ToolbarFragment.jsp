<%-----------------------------------------------------------------------------
	Copyright (c) 2004 Actuate Corporation and others.
	All rights reserved. This program and the accompanying materials 
	are made available under the terms of the Eclipse Public License v1.0
	which accompanies this distribution, and is available at
	http://www.eclipse.org/legal/epl-v10.html
	
	Contributors:
		Actuate Corporation - Initial implementation.
-----------------------------------------------------------------------------%>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ page session="false" buffer="none" %>
<%@ page import="org.eclipse.birt.report.presentation.aggregation.IFragment,
				 org.eclipse.birt.report.resource.BirtResources,
				 org.eclipse.birt.report.utility.ParameterAccessor,
				 org.eclipse.birt.report.servlet.ViewerServlet" %>

<%-----------------------------------------------------------------------------
	Expected java beans
-----------------------------------------------------------------------------%>
<jsp:useBean id="fragment" type="org.eclipse.birt.report.presentation.aggregation.IFragment" scope="request" />

<%
	String pdfUrl = request.getContextPath( ) + "/run?"
		+ ParameterAccessor.getEncodedQueryString( request, ParameterAccessor.PARAM_FORMAT,	ParameterAccessor.PARAM_FORMAT_PDF );
	String ivUrl = request.getContextPath( ) + "/iv?"
		+ ParameterAccessor.getEncodedQueryString( request, null, null );
%>

<%-----------------------------------------------------------------------------
	Toolbar fragment
-----------------------------------------------------------------------------%>
<TR HEIGHT="20px">
	<TD COLSPAN='2'>
		<DIV ID="toolbar">
			<TABLE CELLSPACING="1px" CELLPADDING="1px" WIDTH="100%" CLASS="birtviewer_toolbar">
				<TR><TD></TD></TR>
				<TR>
					<TD WIDTH="6px"/>
					<TD WIDTH="15px">
					   <IMG NAME='toc' SRC="birt/images/Toc.gif"
					   		TITLE="<%= BirtResources.getString( "birt.viewer.toolbar.toc" )%>" CLASS="birtviewer_clickable">
					</TD>
					<TD WIDTH="6px"/>
					<TD WIDTH="15px">
					   <IMG NAME='parameter' SRC="birt/images/ChangeParameter.gif"
					   		TITLE="<%= BirtResources.getString( "birt.viewer.toolbar.parameter" )%>" CLASS="birtviewer_clickable">
					</TD>
					<TD WIDTH="6px"/>
					<TD WIDTH="15px">
					   <IMG NAME='export' SRC="birt/images/ExportData.gif"
					   		TITLE="<%= BirtResources.getString( "birt.viewer.toolbar.export" )%>" CLASS="birtviewer_clickable">
					</TD>
					<TD ALIGN='right'>
					<%
					if ( !ViewerServlet.isOpenSource( ) )
					{
					%>
						<A HREF="<%= ivUrl %>" style="color:#CCCCCC">
							<B><%= BirtResources.getString( "birt.viewer.toolbar.enableiv" )%></B>
						</A>
					<%
					}
					%>
					</TD>
				</TR>
			</TABLE>
		</DIV>
	</TD>
</TR>
