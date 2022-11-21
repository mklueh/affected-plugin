package io.github.crimix.changedprojectstask.providers;

import io.github.crimix.changedprojectstask.configuration.Configuration;
import io.github.crimix.changedprojectstask.providers.git.GitCommandProvider;
import io.github.crimix.changedprojectstask.utils.CollectingOutputStream;
import io.github.crimix.changedprojectstask.providers.git.GitUtil;
import io.github.crimix.changedprojectstask.utils.LogUtil;
import lombok.SneakyThrows;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ChangedFilesProvider {

    private final Logger logger;
    private final Project project;
    private final Configuration configuration;
    private final GitCommandProvider gitCommandProvider;
    private final List<File> changedFiles;
    private final boolean affectsAllProjects;

    public ChangedFilesProvider(Project project, Configuration configuration) {
        this.project = project;
        this.logger = project.getLogger();
        this.configuration = configuration;
        this.gitCommandProvider = new GitCommandProvider(project, configuration);
        List<String> gitFilteredChanges = findFilteredFileChanges();

        this.changedFiles = createAbsolutFilePaths(gitFilteredChanges);
        this.affectsAllProjects = affectsAllProjects(gitFilteredChanges);
    }

    @SneakyThrows
    private List<String> findFilteredFileChanges() {
        File gitRoot = GitUtil.getGitRootDir(project);

        if (gitRoot == null) {
            throw new IllegalStateException("The project does not have a git root");
        }

        CollectingOutputStream stdout = new CollectingOutputStream();
        CollectingOutputStream stderr = new CollectingOutputStream();
        //We use Apache Commons Exec because we do not want to re-invent the wheel as ProcessBuilder hangs if the output or error buffer is full


        DefaultExecutor exec = new DefaultExecutor();
        exec.setStreamHandler(new PumpStreamHandler(stdout, stderr));
        exec.setWorkingDirectory(gitRoot);
        String gitDiffCommand = gitCommandProvider.getGitDiffCommand();
        exec.execute(CommandLine.parse(gitDiffCommand));

        if (stderr.isNotEmpty()) {
            throw new IllegalStateException(String.format("Failed to run git diff because of \n%s", stdout));
        }

        if (stdout.isEmpty()) {
            throw new IllegalStateException("Git diff returned no results this must be a mistake");
        }

        //Create a single predicate from the ignored regexes such that we can use a simple filter
        Predicate<String> filter = createIgnoredFilter();


        //Filter and return the list
        return stdout.getLines().stream()
                .filter(Predicate.not(filter))
                .collect(Collectors.toList());
    }

    private Predicate<String> createIgnoredFilter() {
        return configuration.getIgnoredRegex()
                .getOrElse(Collections.emptySet())
                .stream()
                .map(Pattern::asMatchPredicate)
                .reduce(Predicate::or)
                .orElse(x -> false);
    }

    private boolean affectsAllProjects(List<String> gitFilteredChanges) {
        //Create a single predicate from the affects all projects regexes such that we can use a simple filter
        Predicate<String> filter = configuration.getAffectsAllRegex()
                .getOrElse(Collections.emptySet())
                .stream()
                .map(Pattern::asMatchPredicate)
                .reduce(Predicate::or)
                .orElse(x -> false);

        return gitFilteredChanges.stream()
                .anyMatch(filter);
    }

    private List<File> createAbsolutFilePaths(List<String> changedFiles) {
        File gitRoot = GitUtil.getGitRootDir(project);

        if (gitRoot == null) {
            throw new IllegalStateException("The project does not have a git root");
        }

        return changedFiles.stream()
                .map(s -> new File(gitRoot, s))
                .collect(Collectors.toList());
    }

    /**
     * Gets the filtered changed files
     *
     * @return the filtered changed files
     */
    public List<File> getChangedFiles() {
        return changedFiles;
    }

    /**
     * Returns whether all projects are affected by the changes specified by the plugin configuration
     *
     * @return true if all projects are affected
     */
    public boolean isAllProjectsAffected() {
        return affectsAllProjects;
    }

    /**
     * Prints debug information if it has been enabled
     */
    public void printDebug() {
        if (LogUtil.shouldLog(configuration)) {
            logger.lifecycle("Git diff command uses {}", gitCommandProvider.getGitDiffCommand());
            logger.lifecycle("All projects affected? {}", isAllProjectsAffected());
            logger.lifecycle("Changed files:");
            changedFiles.forEach(file -> logger.lifecycle(file.toString()));
            logger.lifecycle("");
        }
    }
}
