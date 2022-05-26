package io.github.crimix.changedprojectstask.providers.git;

import io.github.crimix.changedprojectstask.providers.git.GitDiffMode;
import org.gradle.api.Project;

import java.io.File;
import java.util.Optional;

import static io.github.crimix.changedprojectstask.configuration.Properties.*;

/**
 * Created by Marian at 26.05.2022
 */
public class GitUtil {
    /**
     * Extracts the value of a given CLI parameter
     */
    public static Optional<String> extractParameterValue(Project project, String parameter) {
        return Optional.of(project)
                .map(Project::getRootProject)
                .map(p -> p.findProperty(parameter))
                .map(String.class::cast);
    }

    /**
     * Gets the configured previous commit id
     *
     * @return either an optional with the previous commit id or an empty optional if it has not been configured
     */
    public static Optional<String> getPreviousCommitId(Project project) {
        return extractParameterValue(project, PREVIOUS_COMMIT);
    }

    /**
     * Gets the configured commit id
     *
     * @return either an optional with the commit id or an empty optional if it has not been configured
     */
    public static Optional<String> getCommitId(Project project) {
        return extractParameterValue(project, CURRENT_COMMIT);
    }

    /**
     * Gets the configured git commit compare mode if specified.
     * Defaults to {@link GitDiffMode#COMMIT} if none specified.
     *
     * @return the configured git compare mode or {@link GitDiffMode#COMMIT}
     */
    public static GitDiffMode getCommitCompareMode(Project project) {
        return Optional.of(project)
                .map(Project::getRootProject)
                .map(p -> p.findProperty(COMMIT_MODE))
                .map(String.class::cast)
                .map(GitDiffMode::getMode)
                .orElse(GitDiffMode.COMMIT);
    }

    /**
     * Finds the git root for the project.
     *
     * @return a file that represents the git root of the project.
     */
    public static File getGitRootDir(Project project) {
        File currentDir = project.getRootProject().getProjectDir();

        //Keep going until we either hit a .git dir or the root of the file system on either Windows or Linux
        while (currentDir != null && !currentDir.getPath().equals("/")) {
            if (new File(String.format("%s/.git", currentDir.getPath())).exists()) {
                return currentDir;
            }
            currentDir = currentDir.getParentFile();
        }

        return null;
    }
}
