package io.github.mklueh.affected.providers.git;

import io.github.mklueh.affected.configuration.AffectedConfiguration;
import io.github.mklueh.affected.configuration.ArgumentsExtractor;
import lombok.experimental.ExtensionMethod;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.internal.impldep.org.jetbrains.annotations.VisibleForTesting;

import java.util.Optional;

import static io.github.mklueh.affected.configuration.Arguments.CURRENT_COMMIT;
import static io.github.mklueh.affected.configuration.Arguments.PREVIOUS_COMMIT;

/**
 * This class is responsible for creating the git diff command based on the users command line choices when running the task.
 */
@ExtensionMethod(ArgumentsExtractor.class)
public class GitCommandProvider {

    private final Logger logger;

    // The default if no commit ids have been specified
    private static final String HEAD = "HEAD";

    /**
     * Uncommitted changes + last commit:  git diff --name-only HEAD~
     */
    private static final String BASE_DIFF_COMMAND = "git diff --name-only";

    private final Project project;
    private final AffectedConfiguration configuration;

    public GitCommandProvider(Project project, AffectedConfiguration configuration) {
        this.project = project;
        this.logger = project.getLogger();
        this.configuration = configuration;
    }

    /**
     * Constructs the git diff command that should be used to find the changed files.
     *
     * @return the git diff command
     */
    public String getGitDiffCommand() {
        GitDiffMode mode = GitUtil.getCommitCompareMode(project);
        Optional<String> currentCommitId = GitUtil.getCommitId(project);
        Optional<String> previousCommitId = GitUtil.getPreviousCommitId(project);

        return evaluate(mode, currentCommitId, previousCommitId);
    }

    /**
     * Method created such that we can write test for it
     *
     * @param mode             the mode
     * @param currentCommitId  the current commit ref if present
     * @param previousCommitId the previous commit ref if present
     * @return the git diff command
     */
    @VisibleForTesting
    public String evaluate(GitDiffMode mode, Optional<String> currentCommitId, Optional<String> previousCommitId) {
        switch (mode) {
            case COMMIT:
                return getCommitDiff(currentCommitId, previousCommitId);
            case BRANCH:
                return getBranchDiff(currentCommitId, previousCommitId);
            case BRANCH_TWO_DOT:
                return getBranchTwoDotDiff(currentCommitId, previousCommitId);
            case BRANCH_THREE_DOT:
                return getBranchThreeDotDiff(currentCommitId, previousCommitId);
            default:
                throw new UnsupportedOperationException(String.format("GitCommitMode %s is not supported", mode.name()));
        }
    }

    private String getCommitDiff(Optional<String> currentCommitId, Optional<String> previousCommitId) {
        //If only currentCommitId has been specified then we assume that it is the diff of that specific commit
        if (currentCommitId.isPresent() && previousCommitId.isPresent()) {
            return String.format("%s %s~ %s", BASE_DIFF_COMMAND, previousCommitId.get(), currentCommitId.get());
        } else if (currentCommitId.isPresent()) {
            return String.format("%s %s~ %s", BASE_DIFF_COMMAND, currentCommitId.get(), currentCommitId.get());
        } else if (previousCommitId.isPresent()) {
            throw new IllegalStateException(String.format("[%s] When using %s then %s must also be specified", GitDiffMode.COMMIT.name(), PREVIOUS_COMMIT, CURRENT_COMMIT));
        } else {
            return String.format("%s %s~ %s", BASE_DIFF_COMMAND, HEAD, HEAD);
        }
    }

    private String getBranchDiff(Optional<String> currentCommitId, Optional<String> previousCommitId) {
        if (currentCommitId.isPresent() && previousCommitId.isPresent()) {
            return String.format("%s %s %s", BASE_DIFF_COMMAND, previousCommitId.get(), currentCommitId.get());
        } else if (previousCommitId.isPresent()) {
            return String.format("%s %s %s", BASE_DIFF_COMMAND, previousCommitId.get(), HEAD);
        } else {
            throw new IllegalStateException(String.format("[%s] %s must always be specified", GitDiffMode.BRANCH.name(), PREVIOUS_COMMIT));
        }
    }

    private String getBranchTwoDotDiff(Optional<String> currentCommitId, Optional<String> previousCommitId) {
        if (currentCommitId.isPresent() && previousCommitId.isPresent()) {
            return String.format("%s %s..%s", BASE_DIFF_COMMAND, previousCommitId.get(), currentCommitId.get());
        } else if (previousCommitId.isPresent()) {
            return String.format("%s %s..", BASE_DIFF_COMMAND, previousCommitId.get());
        } else {
            throw new IllegalStateException(String.format("[%s] %s must always be specified", GitDiffMode.BRANCH_TWO_DOT.name(), PREVIOUS_COMMIT));
        }
    }

    private String getBranchThreeDotDiff(Optional<String> currentCommitId, Optional<String> previousCommitId) {
        if (currentCommitId.isPresent() && previousCommitId.isPresent()) {
            return String.format("%s %s...%s", BASE_DIFF_COMMAND, previousCommitId.get(), currentCommitId.get());
        } else if (previousCommitId.isPresent()) {
            return String.format("%s %s...", BASE_DIFF_COMMAND, previousCommitId.get());
        } else {
            throw new IllegalStateException(String.format("[%s] %s must always be specified", GitDiffMode.BRANCH_THREE_DOT.name(), PREVIOUS_COMMIT));
        }
    }
}
