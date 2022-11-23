package io.github.mklueh.affected.configuration;

/**
 * The two different modes that the plugin can be used
 */
public enum AffectedMode {

    /**
     * Only execute task on those modules that are directly affected
     */
    ONLY_DIRECTLY,

    /**
     * Execute task on all modules in the dependency tree that are affected
     */
    INCLUDE_DEPENDENTS
}
