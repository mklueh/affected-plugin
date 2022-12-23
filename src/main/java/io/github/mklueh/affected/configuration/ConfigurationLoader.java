package io.github.mklueh.affected.configuration;

import org.gradle.api.Project;

/**
 * Created by Marian at 23.12.2022
 * <p>
 * This configuration loader combines arguments with configuration
 * parameters and uses CLI args with a higher priority, configuration parameters as fallback
 */
public class ConfigurationLoader {

    public static ExecutionMode getExecutionMode(AffectedConfiguration affectedConfiguration, Project project) {
        var executionModeByArgument = ArgumentsExtractor.extractParameterValue(project, Arguments.EXECUTION_MODE);
        var executionModeByConfiguration = AffectedConfigurationExtractor.getExecutionMode(affectedConfiguration);
        return executionModeByArgument.map(ExecutionMode::valueOf).orElse(executionModeByConfiguration);
    }

    public static AffectedMode getAffectedMode(AffectedConfiguration affectedConfiguration, Project project) {
        var affectedModeByArgument = ArgumentsExtractor.extractParameterValue(project, Arguments.AFFECTED_MODE);
        var affectedModeByConfiguration = AffectedConfigurationExtractor.getAffectedMode(affectedConfiguration);
        return affectedModeByArgument.map(AffectedMode::valueOf).orElse(affectedModeByConfiguration);
    }

    public static boolean dryRun(AffectedConfiguration affectedConfiguration, Project affected) {
        return false;
    }
}
