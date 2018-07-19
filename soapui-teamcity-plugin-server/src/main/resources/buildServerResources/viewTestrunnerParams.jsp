<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>

<props:viewWorkingDirectory />

<div class="parameter">
    Path to testrunner: <strong><props:displayValue name="testrunnerPath" emptyValue=""/></strong>
</div>
<div class="parameter">
    Path to SoapUI Pro Project: <props:displayValue name="soapuiProjectPath" emptyValue=""/>
</div>
<div class="parameter">
    Test Suite: <props:displayValue name="testsuite" emptyValue=""/>
</div>
<div class="parameter">
    Test Case: <props:displayValue name="testcase" emptyValue=""/>
</div>
<div class="parameter">
    Project Password: <props:displayValue name="projectPassword" emptyValue=""/>
</div>
<div class="parameter">
    Environment: <props:displayValue name="environment" emptyValue=""/>
</div>
