package io.github.crimix.changedprojectstask.providers;

import io.github.crimix.changedprojectstask.configuration.AffectedExtension;
import io.github.crimix.changedprojectstask.utils.Extension;
import io.github.crimix.changedprojectstask.utils.LogUtil;
import io.github.crimix.changedprojectstask.utils.Pair;
import io.github.crimix.changedprojectstask.utils.ProjectNode;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.logging.Logger;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ProjectDependencyProvider {

    private final Logger logger;
    private final Project project;
    private final AffectedExtension configuration;
    private final Map<Project, Set<Project>> projectDependentsMap;
    private final ProjectNode rootNode;

    public ProjectDependencyProvider(Project project, AffectedExtension configuration) {
        this.project = project;
        this.logger = project.getLogger();
        this.configuration = configuration;
        this.projectDependentsMap = initProjectDependents();
        this.rootNode = new ProjectNode(project.getRootProject());
    }

    private Map<Project, Set<Project>> initProjectDependents() {
        //We create a lookup map of projects and the projects that depends on that project once
        //This is to speed up the evaluating dependent changed projects
        //The key of the map is a project that is a direct dependency for the value set
        return project.getSubprojects().stream()
                .map(this::getProjectDependencies)
                .flatMap(Collection::stream)
                .collect(Collectors.groupingBy(Pair::getKey, Collectors.mapping(Pair::getValue, Collectors.toSet())));
    }

    /**
     *
     */
    private Set<Pair<Project, Project>> getProjectDependencies(Project subproject) {
        //We use a pair, because we want the project that is a dependency together with the project it is a dependency for
        return subproject.getConfigurations().stream()
                .map(org.gradle.api.artifacts.Configuration::getDependencies)
                .map(dependencySet -> dependencySet.withType(ProjectDependency.class))
                .flatMap(Set::stream)
                .map(ProjectDependency::getDependencyProject)
                .map(p -> new Pair<>(p, subproject))
                .collect(Collectors.toSet());
    }

    public Project findProjectOfChangedFile(File file) {
        String filePath = file.getPath();
        if (!filePath.contains(Extension.getProjectDirName(project.getRootProject()) + File.separator)) {
            return null; //We return null here as there is no need to try and step though the map
        }

        //We do the split such that we can start from the root of the project and thus when we get an empty optional back we know we are done
        filePath = filePath.split(Pattern.quote(Extension.getProjectDirName(project.getRootProject()) + File.separator), 2)[1];
        String[] paths = filePath.split(Pattern.quote(File.separator));
        ProjectNode currentNode = rootNode;

        for (String path : paths) {
            Optional<ProjectNode> potentialNext = currentNode.getProjectNodeFromPath(path);
            if (potentialNext.isPresent()) {
                currentNode = potentialNext.get();
            } else {
                break; //We have stepped though all the child projects as far we can, just break
            }
        }

        // We now have the project the file change belong to
        Project affectedProject = currentNode.getProject();

        logger.lifecycle("Changed file: " + file.getAbsolutePath() + " of affected project " + affectedProject.getPath());

        return affectedProject;
    }

    public Set<Project> getAffectedDependentProjects(Set<Project> directlyChangedProjects) {
        //We use this to have a way to break our of recursion when we have already seen that project once
        //This makes it possible to avoid infinite recursion and also speeds up the process
        Set<Project> alreadyVisitedProjects = new HashSet<>();

        return directlyChangedProjects.stream()
                .map(p -> getDependentProjects(p, alreadyVisitedProjects))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    private Set<Project> getDependentProjects(Project project, Set<Project> alreadyVisitedProjects) {
        //If we have already visited the project, we can just return empty as we have evaluated dependent projects in another call
        if (alreadyVisitedProjects.contains(project)) {
            return Collections.emptySet();
        }
        Set<Project> result = new HashSet<>(projectDependentsMap.getOrDefault(project, Collections.emptySet()));
        alreadyVisitedProjects.add(project);

        //Continue down the chain until no more new affected projects are found
        Set<Project> dependents = result.stream()
                .map(p -> getDependentProjects(p, alreadyVisitedProjects))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        result.addAll(dependents);
        return result;
    }

    public void printDebug() {
        if (LogUtil.shouldLog(configuration)) {

            logger.lifecycle("Printing project dependents map");
            projectDependentsMap.forEach((key, value) -> logger.lifecycle("Project: {} is a direct dependent for the following {}", key, value));
            logger.lifecycle("Printing project nodes");
            logger.lifecycle(rootNode.toString());
            logger.lifecycle("");
        }
    }
}
