package io.github.mklueh.affected.configuration;

import org.gradle.api.Project;

import java.util.Collections;
import java.util.Set;

/**
 * Created by Marian at 26.05.2022
 */
public class ConfigurationValidator {
    /**
     * Runs validation on the configuration.
     */
    public static void validate(AffectedExtension configuration, Project rootProject) {
        String taskToRun = configuration.getTarget().getOrNull();

        if (taskToRun == null && PropertiesExtractor.getTargetTaskParameter(rootProject).isEmpty()) {
            throw new IllegalArgumentException("changedProjectsTask: target task is required");
        } else if (taskToRun != null && taskToRun.startsWith(":")) {
            throw new IllegalArgumentException("changedProjectsTask: target task should not start with :");
        }

        Set<String> projectsAlwaysRun = configuration.getAlwaysRunProjects().getOrElse(Collections.emptySet());

        for (String project : projectsAlwaysRun) {
            if (!project.startsWith(":")) {
                throw new IllegalArgumentException(String.format("changedProjectsTask: alwaysRunProject %s must start with :", project));
            }
        }

        configuration.getAffectsAllRegex().getOrElse(Collections.emptySet()); //Gradle will throw if the type does not match
        configuration.getIgnoredRegex().getOrElse(Collections.emptySet()); //Gradle will throw if the type does not match
    }
}
