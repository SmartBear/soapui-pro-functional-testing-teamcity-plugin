package com.smartbear;

import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.RunType;
import jetbrains.buildServer.serverSide.RunTypeRegistry;
import jetbrains.buildServer.util.PropertiesUtil;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import static com.smartbear.TestRunnerParams.SOAPUI_PROJECT_PATH;
import static com.smartbear.TestRunnerParams.TEST_RUNNER_PATH;

public class SoapUITestRunnerType extends RunType {
    private PluginDescriptor descriptor;

    public SoapUITestRunnerType(@NotNull final PluginDescriptor descriptor, @NotNull final RunTypeRegistry registry) {
        registry.registerRunType(this);
        this.descriptor = descriptor;
    }

    @Override
    public String getType() {
        return "SoapUI Runner";
    }

    @Override
    public String getDisplayName() {
        return "ReadyAPI Test: Run Functional Test";
    }

    @Override
    public String getDescription() {
        return "Runs ReadyAPI Tests";
    }

    @Override
    public PropertiesProcessor getRunnerPropertiesProcessor() {
        return new PropertiesProcessor() {
            @Override
            public Collection<InvalidProperty> process(final Map<String, String> properties) {
                Collection<InvalidProperty> invalid = new LinkedList<InvalidProperty>();
                if (PropertiesUtil.isEmptyOrNull(properties.get(TEST_RUNNER_PATH))) {
                    invalid.add(new InvalidProperty(TEST_RUNNER_PATH, "Please, set path to testrunner"));
                }
                if (PropertiesUtil.isEmptyOrNull(properties.get(SOAPUI_PROJECT_PATH))) {
                    invalid.add(new InvalidProperty(SOAPUI_PROJECT_PATH, "Please, set path to the SoapUI Pro project"));
                }
                return invalid;
            }
        };
    }

    @Override
    public String getEditRunnerParamsJspFilePath() {
        return descriptor.getPluginResourcesPath("editTestrunnerParams.jsp");
    }

    @Override
    public String getViewRunnerParamsJspFilePath() {
        return descriptor.getPluginResourcesPath("viewTestrunnerParams.jsp");
    }

    @Override
    public Map<String, String> getDefaultRunnerProperties() {
        return null;
    }
}
