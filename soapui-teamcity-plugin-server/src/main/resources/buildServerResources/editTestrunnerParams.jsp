<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>

<props:viewWorkingDirectory/>


<tr>
    <th>
        <label for="testrunnerPath">Path to testrunner:
            <span class="mandatoryAsterix" title="Mandatory field">*</span>
        </label>
    </th>
    <td>
        <props:textProperty name="testrunnerPath" className="longField" expandable="true"/>
        <span class="smallNote">Specifies the fully-qualified name of the runner file (testrunner.bat or testrunner.sh).</span>
    </td>
</tr>
<tr>
    <th>
        <label for="soapuiProjectPath">Path to ReadyAPI Project:
            <span class="mandatoryAsterix" title="Mandatory field">*</span>
        </label>
    </th>
    <td>
        <props:textProperty name="soapuiProjectPath" className="longField" expandable="true"/>
        <span class="smallNote">Specifies the fully-qualified name of the project file.</span>
    </td>
</tr>
<tr>
    <th>
        <label for="testsuite">Test Suite:</label>
    </th>
    <td>
        <props:textProperty name="testsuite" className="longField" expandable="true"/>
        <span class="smallNote">Specifies the test suite to run.</span>
    </td>
</tr>
<tr>
    <th>
        <label for="testcase">Test Case:</label>
    </th>
    <td>
        <props:textProperty name="testcase" className="longField" expandable="true"/>
        <span class="smallNote">Specifies the test case to run.</span>
    </td>
</tr>
<tr>
    <th>
        <label for="projectPassword">Project Password:</label>
    </th>
    <td>
        <props:passwordProperty name="projectPassword" className="longField"/>
        <span class="smallNote">Specifies the encryption password.</span>
    </td>
</tr>
<tr>
    <th>
        <label for="environment">Environment:</label>
    </th>
    <td>
        <props:textProperty name="environment" className="longField" expandable="true"/>
        <span class="smallNote">Specifies the environment configuration for the test run.</span>
    </td>
</tr>