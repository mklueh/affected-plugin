package io.github.mklueh.affected;

import io.github.mklueh.affected.configuration.AffectedExtension;
import io.github.mklueh.affected.configuration.PropertiesExtractor;
import io.github.mklueh.affected.utils.Extension;
import lombok.experimental.ExtensionMethod;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

/**
 *
 */
@SuppressWarnings("ALL")
@ExtensionMethod(PropertiesExtractor.class)
public class AffectedPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {

        if (!Extension.isRootProject(project)) {
            throw new IllegalArgumentException(String.format("Must be applied to root project %s, but was found on %s instead.",
                    project.getRootProject(), project.getName()));
        }

        //top-level extension
        AffectedExtension configuration = project.getExtensions()
                .create("affected", AffectedExtension.class);

        Task task = project.getTasks().register("affected").get();

        if (Extension.isAffectedPluginEnabled(project)) {
            AffectedTaskRunner.configureAndRun(project, task, configuration);
        } else System.out.println("affected plugin: disabled");

    }
}
