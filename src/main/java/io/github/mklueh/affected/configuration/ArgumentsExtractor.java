package io.github.mklueh.affected.configuration;

import org.gradle.api.Project;

import java.util.*;
import java.util.stream.Collectors;

import static io.github.mklueh.affected.configuration.Arguments.*;

/**
 * Extractor for CLI arguments
 */
public class ArgumentsExtractor {

    /**
     * Gets the task to run from the command line arguments if given
     *
     * @return task to run CLI argument
     */
    public static Optional<String> getTargetTaskParameter(Project project) {
        return extractParameterValue(project, TARGET_TASK);
    }

    /**
     * Gets the projects to run from the command line arguments if given
     *
     * @return collection of project names
     */
    public static Optional<Set<String>> getEnabledModulesParameter(Project project) {
        Optional<String> value = extractParameterValue(project, ENABLED_FOR_MODULES);
        if (value.isEmpty()) return Optional.empty();
        return Optional.of(Arrays.stream(value.get().split(","))
                .map(String::trim).collect(Collectors.toSet()));
    }


    /**
     * Extracts the value of a given CLI parameter
     */
    public static Optional<String> extractParameterValue(Project project, String parameter) {
        return Optional.of(project)
                .map(Project::getRootProject)
                .map(p -> p.findProperty(parameter))
                .map(String.class::cast);
    }
}
