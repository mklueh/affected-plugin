package io.github.mklueh.affected.configuration;

/**
 * Created by Marian at 23.12.2022
 */
public enum ExecutionMode {

    /**
     * The affected plugin will directly execute the task
     */
    DIRECT_EXECUTION,


    /**
     * The task to be executed will be
     * called as a separate process
     */
    COMMAND_LINE_EXECUTION

}
