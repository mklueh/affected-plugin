package io.github.crimix.changedprojectstask.configuration;

import io.github.crimix.changedprojectstask.providers.git.GitUtil;
import org.gradle.api.Project;

import java.util.*;
import java.util.stream.Collectors;

import static io.github.crimix.changedprojectstask.configuration.Properties.*;

/**
 * Class the contains the Lombok extension methods
 */
public class PropertiesExtractor {

    /**
     * Returns whether the project is the root project.
     *
     * @return true if the project is the root project
     */
    public static boolean isRootProject(Project project) {
        return project.equals(project.getRootProject());
    }

    /**
     * Gets the name of the project's directory
     *
     * @return the name of the project's directory
     */
    public static String getProjectDirName(Project project) {
        return project.getProjectDir().getName();
    }

    /**
     * Returns whether the plugin's task is allowed to run and configure.
     *
     * @return true if the plugin's task is allowed to run and configure
     */
    public static boolean hasBeenEnabled(Project project) {
        return project.getRootProject().hasProperty(ENABLE);
    }

    /**
     * Gets the task to run from the command line arguments if given
     *
     * @return task to run CLI argument
     */
    public static Optional<String> getTargetTaskParameter(Project project) {
        return GitUtil.extractParameterValue(project, TARGET_TASK);
    }

    /**
     * Gets the projects to run from the command line arguments if given
     *
     * @return collection of project names
     */
    public static Collection<String> getEnabledProjectsParameter(Project project) {
        return Arrays.stream(GitUtil.extractParameterValue(project, ENABLED_FOR_MODULES)
                        .orElse("").split(","))
                .map(String::trim).collect(Collectors.toList());
    }

    /**
     * Gets the plugin's configured mode
     *
     * @return the mode the plugin is configured to use
     */
    public static AffectedMode getPluginMode(Configuration configuration) {
        return AffectedMode.valueOf(configuration.getChangedProjectsMode().getOrElse(AffectedMode.INCLUDE_DEPENDENTS.name()));
    }

}
