package io.github.crimix.changedprojectstask;

import io.github.crimix.changedprojectstask.configuration.AffectedMode;
import io.github.crimix.changedprojectstask.configuration.Configuration;
import io.github.crimix.changedprojectstask.configuration.PropertiesExtractor;
import io.github.crimix.changedprojectstask.providers.ChangedFilesProvider;
import io.github.crimix.changedprojectstask.providers.ProjectDependencyProvider;
import io.github.crimix.changedprojectstask.utils.Extension;
import io.github.crimix.changedprojectstask.utils.LogUtil;
import io.github.crimix.changedprojectstask.configuration.ConfigurationValidator;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.logging.Logger;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * TODO name AffectedTask makes no sense for this class and confuses
 */
public class AffectedTaskRunner {

    private final Logger logger;
    private final Project project;
    private final Task affectedTask;
    private final Configuration configuration;

    private boolean affectsAll = false;

    // may run if affected
    private Set<Project> allowedToRunProjects = new HashSet<>();

    //always run, affected or not
    private Set<Project> alwaysRunProjects = new HashSet<>();

    //never run, affected or not - but dependents still will
    private Set<Project> neverRunProjects = new HashSet<>();

    private Set<Project> affectedProjects = new HashSet<>();

    private AffectedTaskRunner(Project project, Task affectedTask, Configuration configuration) {
        this.project = project;
        this.logger = project.getLogger();
        this.configuration = configuration;
        this.affectedTask = affectedTask;
    }

    public static void configureAndRun(Project project, Task task, Configuration configuration) {
        AffectedTaskRunner affectedTaskRunner = new AffectedTaskRunner(project, task, configuration);

        affectedTaskRunner.configureTargetTasksForProjects();

        //pure evaluation that enabled or disables the previously configured target tasks
        project.getGradle().projectsEvaluated(g -> affectedTaskRunner.evaluateAffectedProjects());
    }

    /**
     * TODO determine everything that should run before looping through the projects and run the actual task.
     * Testability
     */
    private void configureTargetTasksForProjects() {
        if (allowedToRunProjects.isEmpty()) return;

        for (Project project : project.getAllprojects()) {
            project.afterEvaluate(p -> {
                Task targetTask = p.getTasks().findByPath(resolvePathToTargetTask(p));

                if (targetTask != null) {
                    //make targetTask run after changedProjectsTask
                    affectedTask.dependsOn(targetTask);

                    //conditionally enable / disable the specific project's task
                    targetTask.onlyIf(t -> {
                        boolean willRun = shouldModuleRun(p);

                        if (willRun)
                            logger.lifecycle("################# Task " +
                                    t.getName() + " will run for project " +
                                    p.getName() + " #################"
                            );

                        return willRun;
                    });
                }
            });
        }
    }

    /**
     * Determine for each project if it should be triggered or not
     */
    @SuppressWarnings("RedundantIfStatement")
    private boolean shouldModuleRun(Project p) {
        //preventing conditions
        if (neverRunProjects.contains(p)) return false;
        if (!allowedToRunProjects.contains(p)) return false;

        //allowing conditions
        if (affectsAll) return true;
        if (alwaysRunProjects.contains(p)) return true;
        if (affectedProjects.contains(p)) return true;

        return false;
    }

    /**
     * TODO naming - what do we do here?
     */
    private void evaluateAffectedProjects() {
        Project project = getRootProject();
        ConfigurationValidator.validate(configuration, project);

        if (!isAffectedPluginEnabled()) return;

        ChangedFilesProvider changedFilesProvider = new ChangedFilesProvider(project, configuration);
        changedFilesProvider.printDebug();

        if (!changedFilesProvider.hasFileChanges()) return;
        if (!changedFilesProvider.allProjectsAffected()) {
            affectsAll = true;
            return;
        }

        determineEligibleProjectsBasedOnProperties(project);


        ProjectDependencyProvider projectDependencyProvider = new ProjectDependencyProvider(project, configuration);
        projectDependencyProvider.printDebug();

        Set<Project> directlyAffectedProjects = findAffectedProjects(changedFilesProvider, projectDependencyProvider);

        if (LogUtil.shouldLog(configuration)) {
            logger.lifecycle("Directly affected projects: {}", directlyAffectedProjects);
        }

        Set<Project> dependentAffectedProjects = new HashSet<>();
        if (AffectedMode.INCLUDE_DEPENDENTS == PropertiesExtractor.getPluginMode(configuration)) {
            dependentAffectedProjects.addAll(projectDependencyProvider.getAffectedDependentProjects(directlyAffectedProjects));
            if (LogUtil.shouldLog(configuration)) {
                logger.lifecycle("Dependent affected Projects: {}", dependentAffectedProjects);
            }
        }

        affectedProjects = Stream.concat(directlyAffectedProjects.stream(), dependentAffectedProjects.stream()).collect(Collectors.toSet());
    }

    private Set<Project> findAffectedProjects(ChangedFilesProvider changedFilesProvider, ProjectDependencyProvider projectDependencyProvider) {
        return changedFilesProvider
                .getChangedFiles()
                .stream()
                .map(projectDependencyProvider::findProjectOfChangedFile)
                .filter(Objects::nonNull).collect(Collectors.toSet());
    }

    /**
     * Matches projects against the "alwaysRun" and "neverRun" projects and
     */
    private void determineEligibleProjectsBasedOnProperties(Project project) {
        //should run no matter what
        Set<String> alwaysRun = configuration.getAlwaysRunProjects().getOrElse(Collections.emptySet());
        Set<Project> allProjects = project.getAllprojects();
        alwaysRunProjects = allProjects.stream().filter(p -> alwaysRun.contains(p.getPath())).collect(Collectors.toSet());

        //should never run
        Set<String> notAllowedToRun = configuration.getNeverRunProjects().getOrElse(Collections.emptySet());
        neverRunProjects = allProjects.stream().filter(p -> notAllowedToRun.contains(p.getName())).collect(Collectors.toSet());


        //only those should be allowed to run if set
        Set<String> allowedToRun = PropertiesExtractor.getEnabledModulesParameter(project)
                .orElse(configuration.getProjects().getOrElse(Collections.emptySet()));
        allowedToRunProjects = allProjects.stream().filter(p -> allowedToRun.contains(p.getName())).collect(Collectors.toSet());

        if (LogUtil.shouldLog(configuration)) {
            logger.lifecycle("Projects allowed to run: {}", allowedToRunProjects);
            logger.lifecycle("Never run projects: {}", this.neverRunProjects);
            logger.lifecycle("Always run projects: {}", this.alwaysRunProjects);
        }
    }

    private Project getRootProject() {
        return project.getRootProject();
    }

    private boolean isAffectedPluginEnabled() {
        return Extension.isAffectedPluginEnabled(project);
    }


    private String resolvePathToTargetTask(Project project) {
        String targetTask = PropertiesExtractor.getTargetTaskParameter(project)
                .orElse(configuration.getTarget().getOrNull());

        if (LogUtil.shouldLog(configuration)) {
            logger.lifecycle("targetTask: {}", targetTask);
        }

        if (Extension.isRootProject(project)) {
            return String.format(":%s", targetTask);
        }

        return String.format("%s:%s", project.getPath(), targetTask);
    }

}
