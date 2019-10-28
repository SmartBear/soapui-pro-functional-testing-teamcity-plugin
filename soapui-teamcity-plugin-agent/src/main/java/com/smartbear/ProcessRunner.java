package com.smartbear;

import jetbrains.buildServer.agent.BuildProgressLogger;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class ProcessRunner {
    public final static String READYAPI_REPORT_DIRECTORY = "ReadyAPI_report";
    private static final String TESTRUNNER_VERSION_DETERMINANT = "ready-api-ui-";
    private static final int TESTRUNNER_VERSION_FOR_ANALYTICS_FIRST_NUMBER = 2;
    private static final int TESTRUNNER_VERSION_FOR_ANALYTICS_SECOND_NUMBER = 4;
    private static final String TESTRUNNER_NAME = "testrunner";
    private static final String COMPOSITE_PROJECT_SETTINGS_FILE_PATH = "settings.xml";
    private static final String SOAPUI_PRO_FUNCTIONAL_TESTING_PLUGIN_INFO = "/soapuiTeamCityPlugin.properties";
    private static final String DEFAULT_PLUGIN_VERSION = "1.0";
    private static final String LAST_ELEMENT_TO_READ = "con:soapui-project";
    private static final String ATTRIBUTE_TO_CHECK = "updated";
    private static final String TERMINATION_STRING = "Please enter absolute path of the license file";
    private static final String SH = ".sh";
    private static final String BAT = ".bat";
    private static final String JAR = ".jar";
    private boolean isSoapUIProProject = false;
    private BuildProgressLogger logger;

    Process run(final SoapUITestBuildProcess buildProcess, final BuildProgressLogger logger, final ParametersContainer params)
            throws IOException {
        this.logger = logger;
        List<String> processParameterList = new ArrayList<>();
        String testrunnerFilePath = buildTestRunnerPath(params.getPathToTestrunner());
        if (isNotBlank(testrunnerFilePath) && new File(testrunnerFilePath).exists()) {
            processParameterList.add(testrunnerFilePath);
        } else {
            logger.error("Failed to load testrunner file [" + testrunnerFilePath + "]");
            return null;
        }

        String reportDirectoryPath = params.getWorkspace() + File.separator + READYAPI_REPORT_DIRECTORY;
        setReportDirectory(reportDirectoryPath);
        processParameterList.addAll(Arrays.asList("-f", reportDirectoryPath));

        processParameterList.add("-r");
        processParameterList.add("-j");
        processParameterList.add("-J");

        if (isNotBlank(params.getTestSuite())) {
            processParameterList.addAll(Arrays.asList("-s", params.getTestSuite()));
        }
        if (isNotBlank(params.getTestCase())) {
            processParameterList.addAll(Arrays.asList("-c", params.getTestCase()));
        }
        if (isNotBlank(params.getProjectPassword())) {
            processParameterList.addAll(Arrays.asList("-x", params.getProjectPassword()));
        }
        if (isNotBlank(params.getEnvironment())) {
            processParameterList.addAll(Arrays.asList("-E", params.getEnvironment()));
        }

        processParameterList.addAll(Arrays.asList("-F", "HTML"));
        if (isNotBlank(params.getTestCase())) {
            processParameterList.addAll(Arrays.asList("-R", "Test Case Report"));
        } else if (isNotBlank(params.getTestSuite())) {
            processParameterList.addAll(Arrays.asList("-R", "TestSuite Report"));
        } else {
            processParameterList.addAll(Arrays.asList("-R", "Project Report"));
        }

        String projectFilePath = params.getPathToProjectFile();
        if (isNotBlank(projectFilePath) && new File(projectFilePath).exists()) {
            try {
                checkIfSoapUIProProject(projectFilePath);
            } catch (Exception e) {
                logger.exception(e);
                return null;
            }
            if (!isSoapUIProProject) {
                logger.error("The project is not a SoapUI Pro project! Exiting.");
                return null;
            }
            processParameterList.add(projectFilePath);
        } else {
            logger.error("Failed to load the project file [" + projectFilePath + "]");
            return null;
        }

        if (shouldSendAnalytics(testrunnerFilePath)) {
            Properties properties = new Properties();
            properties.load(ProcessRunner.class.getResourceAsStream(SOAPUI_PRO_FUNCTIONAL_TESTING_PLUGIN_INFO));
            String version = properties.getProperty("version", DEFAULT_PLUGIN_VERSION);
            processParameterList.addAll(Arrays.asList("-q", "TeamCity" + "-" + version));
        }

        ProcessBuilder pb = new ProcessBuilder(processParameterList);
        pb.redirectErrorStream(true);
        logger.message("Starting SoapUI Pro functional test.");
        final Process process = pb.start();

        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        new Thread(new Runnable() {
            public void run() {
                String s;
                try {
                    while ((s = bufferedReader.readLine()) != null) {
                        logger.message(s);
                        if (s.contains(TERMINATION_STRING)) {
                            logger.error("No license was found! Exiting.");
                            process.destroy();
                            buildProcess.interrupt();
                            return;
                        }
                    }
                } catch (IOException e) {
                    logger.exception(e);
                }
            }
        }).start();

        return process;
    }

    private String buildTestRunnerPath(String pathToTestrunnerFile) {
        if (!isNotBlank(pathToTestrunnerFile)) {
            return "";
        }
        if (new File(pathToTestrunnerFile).isFile()) {
            return pathToTestrunnerFile;
        }
        if (System.getProperty("os.name").contains("Windows")) {
            return pathToTestrunnerFile + File.separator + TESTRUNNER_NAME + BAT;
        } else {
            return pathToTestrunnerFile + File.separator + TESTRUNNER_NAME + SH;
        }
    }

    private void setReportDirectory(String reportDirectoryPath) {
        File reportDirectoryFile = new File(reportDirectoryPath);
        if (!reportDirectoryFile.exists()) {
            reportDirectoryFile.mkdir();
        } else {
            try {
                FileUtils.cleanDirectory(reportDirectoryFile);
            } catch (IOException e) {
                logger.exception(e);
            }
        }
    }

    private boolean shouldSendAnalytics(String testrunnerFile) throws IOException {
        String testrunnerFileToString = FileUtils.readFileToString(new File(testrunnerFile));
        if (testrunnerFileToString.contains(TESTRUNNER_VERSION_DETERMINANT)) {
            int[] versionNumbers = getVersionNumbers(testrunnerFileToString);
            if (versionNumbers != null) {
                int firstVersionNumber = versionNumbers[0];
                if (firstVersionNumber > TESTRUNNER_VERSION_FOR_ANALYTICS_FIRST_NUMBER) {
                    return true;
                } else if (firstVersionNumber == TESTRUNNER_VERSION_FOR_ANALYTICS_FIRST_NUMBER) {
                    int secondVersionIndex = versionNumbers[1];
                    return secondVersionIndex >= TESTRUNNER_VERSION_FOR_ANALYTICS_SECOND_NUMBER;
                }
            }
        }
        return false;
    }

    @Nullable
    private int[] getVersionNumbers(String testRunnerFileContent) {
        try {
            int[] versionNumbers = new int[2];
            int startIndex = testRunnerFileContent.indexOf(TESTRUNNER_VERSION_DETERMINANT) + TESTRUNNER_VERSION_DETERMINANT.length();
            int endIndex = testRunnerFileContent.indexOf(JAR, startIndex);
            String version = testRunnerFileContent.substring(startIndex, endIndex);
            String[] versionStringArray = version.split("\\.");
            for (int i = 0; i < versionNumbers.length; i++) {
                versionNumbers[i] = Integer.parseInt(versionStringArray[i]);
            }
            return versionNumbers;
        } catch (Exception e) {
            logger.exception(e);
            return null;
        }
    }

    private void checkIfSoapUIProProject(String projectFilePath) throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        //if composite project, check settings.xml file
        if (new File(projectFilePath).isDirectory()) {
            projectFilePath = projectFilePath + File.separator + COMPOSITE_PROJECT_SETTINGS_FILE_PATH;
            if (!new File(projectFilePath).exists()) {
                throw new IOException("Missing settings.xml file in the composite project! Can not check if the project " +
                        "is a SoapUI Pro project. Exiting.");
            }
        }
        try {
            saxParser.parse(projectFilePath, new ReadXmlUpToSpecificElementSaxParser(LAST_ELEMENT_TO_READ));
        } catch (MySAXTerminatorException exp) {
            //nothing to do, expected
        }
    }

    private class ReadXmlUpToSpecificElementSaxParser extends DefaultHandler {
        private final String lastElementToRead;

        ReadXmlUpToSpecificElementSaxParser(String lastElementToRead) {
            this.lastElementToRead = lastElementToRead;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws MySAXTerminatorException {
            if (lastElementToRead.equals(qName)) {
                isSoapUIProProject = !attributes.getValue(ATTRIBUTE_TO_CHECK).isEmpty();
                throw new MySAXTerminatorException();
            }
        }
    }

    private class MySAXTerminatorException extends SAXException {
    }
}
