package io.github.mklueh.affected.utils;

import org.gradle.api.Project;

import java.util.Optional;

import static io.github.mklueh.affected.configuration.Arguments.*;

/**
 * Created by Marian at 26.05.2022
 *
 * TODO will be used to enhance diverse classes via lombok
 */
public class Extension {
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
    public static boolean isAffectedPluginEnabled(Project project) {
        return project.getRootProject().hasProperty(ENABLE)
                || project.getRootProject().hasProperty(EXECUTION_MODE);
    }

    /**
     * Returns whether the plugin has been told to run using both task and commandline
     * @return true if the plugin has been told to run using both task and commandline
     */
    public static boolean hasBothRunCommands(Project project) {
        return project.getRootProject().hasProperty(ENABLE)
                && project.getRootProject().hasProperty(EXECUTION_MODE);
    }

    /**
     * Returns if the task to runs should be invoked using the commandline instead of using the task onlyIf approach.
     * @return true if the task should be invoked using the commandline
     */
    public static boolean shouldUseCommandLine(Project project) {
        return project.getRootProject().hasProperty(EXECUTION_MODE);
    }

    /**
     * Gets the commandline arguments specified for use when invoking the task to run using the commandline.
     * @return the commandline arguments as a string
     */
    public static String getCommandLineArgs(Project project) {
        return Optional.of(project)
                .map(Project::getRootProject)
                .map(p -> p.findProperty(COMMANDLINE_ARGS))
                .map(String.class::cast)
                .orElse("");
    }
}
