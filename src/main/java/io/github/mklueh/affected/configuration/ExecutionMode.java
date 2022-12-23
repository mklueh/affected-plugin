package io.github.mklueh.affected.configuration;

/**
 * Created by Marian at 23.12.2022
 */
public enum ExecutionMode {

    /**
     * The task of the affected project will be triggered directly
     */
    DIRECT_EXECUTION,


    /**
     * The task of the affected project will be triggered via command line
     */
    COMMAND_LINE_EXECUTION

}
