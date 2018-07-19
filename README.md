# SoapUI Pro Functional Testing Plugin

### About

A SmartBear plugin used to run SoapUI Pro tests on TeamCity agents. 

### Requirements

* The project you want to run must be saved in ReadyAPI version 2.1.0 or later.
* The Teamcity agent where you want to run your test must have ReadyAPI installed with an activated SoapUI Pro license.

### Configuration

The build step has the following settings:  
	
* **Path to testrunner** - Specifies the fully-qualified path to the runner file (*testrunner.bat* or *testrunner.sh*). By default, you can find it in the *&lt;ReadyAPI installation&gt;/bin* directory.
* **Path to SoapUI Pro project** -  Specifies the fully-qualified path to the SoapUI Pro project you want to run.
* **Test Suite** - Specifies the test suite to run. To run all the test suites of your project, leave the field blank.
* **Test Case** - Specifies the test case to run. If you leave the field blank, the runner will execute all the test cases of the specified test suite, or, if you have not specified a test suite, all the test cases of your project.
* **Project Password** - Specifies the encryption password, if you encrypted the entire project or some of its custom properties.
* **Environment** - Specifies the environment configuration for the test run.
