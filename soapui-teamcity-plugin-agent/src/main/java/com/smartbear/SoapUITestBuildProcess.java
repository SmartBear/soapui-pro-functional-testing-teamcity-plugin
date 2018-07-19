package com.smartbear;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.agent.BuildRunnerContext;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

import static com.smartbear.TestRunnerParams.ENVIRONMENT;
import static com.smartbear.TestRunnerParams.PROJECT_PASSWORD;
import static com.smartbear.TestRunnerParams.SOAPUI_PROJECT_PATH;
import static com.smartbear.TestRunnerParams.TESTCASE_NAME;
import static com.smartbear.TestRunnerParams.TESTSUITE_NAME;
import static com.smartbear.TestRunnerParams.TEST_RUNNER_PATH;

public class SoapUITestBuildProcess implements BuildProcess, Callable<BuildFinishedStatus> {
    private Future<BuildFinishedStatus> buildStatus;
    private final AgentRunningBuild agent;
    private final BuildRunnerContext context;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final BuildProgressLogger logger;

    public SoapUITestBuildProcess(final AgentRunningBuild agent, final BuildRunnerContext context) {
        this.agent = agent;
        this.context = context;
        this.logger = agent.getBuildLogger();
    }

    @Override
    public void start() throws RunBuildException {
        try {
            buildStatus = executor.submit(this);
            logger.message("SoapUI testrunner started");
        } catch (final RejectedExecutionException e) {
            logger.error("SoapUI testrunner failed to start");
            throw new RunBuildException(e);
        }
    }

    @Override
    public boolean isInterrupted() {
        return buildStatus.isCancelled() && isFinished();
    }

    @Override
    public boolean isFinished() {
        return buildStatus.isDone();
    }

    @Override
    public void interrupt() {
        logger.message("Interrupting testrunner");
        buildStatus.cancel(true);
    }

    @NotNull
    @Override
    public BuildFinishedStatus waitFor() throws RunBuildException {
        try {
            final BuildFinishedStatus status = buildStatus.get();
            logger.message("Build process was finished");
            return status;
        } catch (final InterruptedException e) {
            throw new RunBuildException(e);
        } catch (final ExecutionException e) {
            throw new RunBuildException(e);
        } catch (final CancellationException e) {
            logger.message("Build process was interrupted: ");
            return BuildFinishedStatus.INTERRUPTED;
        } finally {
            executor.shutdown();
        }
    }

    @Override
    public BuildFinishedStatus call() throws Exception {
        Map<String, String> buildParams = context.getRunnerParameters();

        Process process = null;
        try {
            process = new ProcessRunner()
                    .run(this, logger, new ParametersContainer.Builder()
                            .withPathToTestrunner(buildParams.get(TEST_RUNNER_PATH))
                            .withPathToProjectFile(buildParams.get(SOAPUI_PROJECT_PATH))
                            .withTestSuite(buildParams.get(TESTSUITE_NAME))
                            .withTestCase(buildParams.get(TESTCASE_NAME))
                            .withProjectPassword(buildParams.get(PROJECT_PASSWORD))
                            .withEnvironment(buildParams.get(ENVIRONMENT))
                            .withWorkspace(context.getWorkingDirectory())
                            .build());
            if (process == null) {
                throw new RunBuildException("Could not start SoapUI Pro functional testing.");
            }
        } catch (Exception e) {
            logger.exception(e);
            if (process != null) {
                process.destroy();
            }
            throw new RunBuildException("Could not start SoapUI Pro functional testing.");

        } finally {
            if (process != null) {
                try {
                    process.waitFor();
                } catch (Exception e) {
                    logger.error("Could not start SoapUI Pro functional testing:");
                    logger.exception(e);
                    return BuildFinishedStatus.FINISHED_FAILED;
                }
            }
        }

        return BuildFinishedStatus.FINISHED_SUCCESS;
    }
}
