package io.github.crimix.changedprojectstask.utils;

import org.gradle.api.Project;

import static io.github.crimix.changedprojectstask.configuration.Properties.ENABLE;

/**
 * Created by Marian at 26.05.2022
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
        return project.getRootProject().hasProperty(ENABLE);
    }
}
