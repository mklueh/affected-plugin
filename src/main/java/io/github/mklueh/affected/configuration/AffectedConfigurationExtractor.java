package io.github.mklueh.affected.configuration;

/**
 * Created by Marian at 23.12.2022
 * <p>
 * Configuration parameters extraction
 */
public class AffectedConfigurationExtractor {
    /**
     * Gets the plugin's configured mode
     *
     * @return the mode the plugin is configured to use
     */
    public static AffectedMode getAffectedMode(AffectedConfiguration configuration) {
        return configuration.getAffectedMode().getOrElse(AffectedMode.INCLUDE_DEPENDENTS);
    }

    /**
     * Returns if the task to runs should be invoked using the commandline instead of using the task onlyIf approach.
     *
     * @return true if the task should be invoked using the commandline
     */
    public static ExecutionMode getExecutionMode(AffectedConfiguration configuration) {
        return configuration.getExecutionMode().getOrElse(ExecutionMode.DIRECT_EXECUTION);
    }

}
