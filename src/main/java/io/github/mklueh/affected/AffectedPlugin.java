package io.github.mklueh.affected;

import io.github.mklueh.affected.configuration.AffectedConfiguration;
import io.github.mklueh.affected.configuration.ArgumentsExtractor;
import io.github.mklueh.affected.utils.Extension;
import lombok.experimental.ExtensionMethod;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

/**
 *
 */
@SuppressWarnings("ALL")
@ExtensionMethod(ArgumentsExtractor.class)
public class AffectedPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {

        if (!Extension.isRootProject(project)) {
            throw new IllegalArgumentException(String.format("Must be applied to root project %s, but was found on %s instead.",
                    project.getRootProject(), project.getName()));
        }

        //top-level extension
        AffectedConfiguration configuration = project.getExtensions()
                .create("affected", AffectedConfiguration.class);

        Task task = project.getTasks().register("affected").get();

        if (false && Extension.hasBothRunCommands(project)) {
            throw new IllegalArgumentException("You may either use run or runCommandLine, not both");
        }

        if (Extension.isAffectedPluginEnabled(project)) {
            AffectedTaskRunner.configureAndRun(project, task, configuration);
        } else System.out.println("affected plugin: disabled");

    }
}
