package com.smartbear;

import java.io.File;

public class ParametersContainer {
    private String pathToTestrunner;
    private String pathToProjectFile;
    private String testSuite;
    private String testCase;
    private String projectPassword;
    private String environment;
    private File workspace;

    public String getPathToTestrunner() {
        return pathToTestrunner;
    }

    public String getPathToProjectFile() {
        return pathToProjectFile;
    }

    public String getTestSuite() {
        return testSuite;
    }

    public String getTestCase() {
        return testCase;
    }

    public String getProjectPassword() {
        return projectPassword;
    }

    public String getEnvironment() {
        return environment;
    }

    public File getWorkspace() {
        return workspace;
    }

    public static class Builder {
        ParametersContainer parametersContainer = new ParametersContainer();

        public ParametersContainer build() {
            return parametersContainer;
        }

        public Builder withPathToTestrunner(String pathToTestrunner) {
            parametersContainer.pathToTestrunner = pathToTestrunner;
            return this;
        }

        public Builder withPathToProjectFile(String pathToProjectFile) {
            parametersContainer.pathToProjectFile = pathToProjectFile;
            return this;
        }

        public Builder withTestSuite(String testSuite) {
            parametersContainer.testSuite = testSuite;
            return this;
        }

        public Builder withTestCase(String testCase) {
            parametersContainer.testCase = testCase;
            return this;
        }

        public Builder withProjectPassword(String projectPassword) {
            parametersContainer.projectPassword = projectPassword;
            return this;
        }

        public Builder withEnvironment(String environment) {
            parametersContainer.environment = environment;
            return this;
        }

        public Builder withWorkspace(File workspace) {
            parametersContainer.workspace = workspace;
            return this;
        }

    }
}
