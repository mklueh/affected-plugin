package io.github.mklueh.affected.utils;

import io.github.mklueh.affected.configuration.AffectedExtension;
import io.github.mklueh.affected.configuration.PropertiesExtractor;
import org.gradle.api.logging.Logger;

import java.util.Collections;

/**
 * Created by Marian at 26.05.2022
 */
public class LogUtil {
    /**
     * Prints the configuration.
     *
     * @param logger the logger to print the configuration to.
     */
    public static void print(AffectedExtension configuration, Logger logger) {
        if (shouldLog(configuration)) {
            logger.lifecycle("Printing configuration");
            logger.lifecycle("Task to run {}", configuration.getTarget().getOrNull());
            logger.lifecycle("Always run project {}", configuration.getAlwaysRunProjects().getOrElse(Collections.emptySet()));
            logger.lifecycle("Never run project {}", configuration.getNeverRunProjects().getOrElse(Collections.emptySet()));
            logger.lifecycle("Affects all regex {}", configuration.getAffectsAllRegex().getOrElse(Collections.emptySet()));
            logger.lifecycle("Ignored regex {}", configuration.getIgnoredRegex().getOrElse(Collections.emptySet()));
            logger.lifecycle("Mode {}", PropertiesExtractor.getPluginMode(configuration));
            logger.lifecycle("");
        }
    }

    /**
     * Returns whether the plugin should log debug information to the Gradle log
     *
     * @return true if the plugin should debug log
     */
    public static boolean shouldLog(AffectedExtension configuration) {
        return configuration.getDebugLogging().getOrElse(false);
    }
}
