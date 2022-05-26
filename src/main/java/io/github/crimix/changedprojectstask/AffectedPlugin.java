package io.github.crimix.changedprojectstask;

import io.github.crimix.changedprojectstask.configuration.Configuration;
import io.github.crimix.changedprojectstask.configuration.PropertiesExtractor;
import io.github.crimix.changedprojectstask.task.AffectedTask;
import io.github.crimix.changedprojectstask.utils.Extension;
import lombok.experimental.ExtensionMethod;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

@ExtensionMethod(PropertiesExtractor.class)
public class AffectedPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        if (!Extension.isRootProject(project)) {
            throw new IllegalArgumentException(String.format("Must be applied to root project %s, but was found on %s instead.", project.getRootProject(), project.getName()));
        }

        Configuration configuration = project.getExtensions().create("affected", Configuration.class);

        Task task = project.getTasks().register("affected").get();

        if (Extension.hasBeenEnabled(project)) {
            AffectedTask.configureAndRun(project, task, configuration);
        }

    }
}
