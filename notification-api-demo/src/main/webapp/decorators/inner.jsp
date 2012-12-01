<%@ include file="/common/taglibs.jsp" %>

<html>
<head>
    <title><decorator:title/> | <fmt:message key="webapp.name"/></title>
    <!-- External CSS and JS files -->

    <link href="<c:url value="/styles/style.css"/>" rel="stylesheet" type="text/css"/>
    <script src="<c:url value="/scripts/jquery-1.3.2.min.js" />" type="text/javascript"></script>

    <decorator:head/>
</head>

<body>
<div id="top"></div>
<!-- Start main wrapper -->

<div id="wrapper">
    <%@ include file="/common/header.jsp" %>

    <div id="page_left">

        <div class="ui-state-default box_title_new">
            <div>
                	<span> 
                		<fmt:message key="test menu header"/>
                	</span>
            </div>
        </div>
        <div class="box_data" style="padding-top:5px;">
            <ul class="side_bar">
                <li><a href="<c:url value=''/>"><fmt:message key="test menu item"/></a></li>
            </ul>
        </div>

    </div>
    <div id="page_right">
        <decorator:body/>
    </div>


    <div class="clear"></div>
    <%@ include file="/common/footer.jsp" %>
</div>
<!--   End main wrapper -->
</body>

</html>