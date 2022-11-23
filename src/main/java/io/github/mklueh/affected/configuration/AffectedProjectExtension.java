package io.github.mklueh.affected.configuration;

import org.gradle.api.provider.Property;

/**
 * Created by Marian at 20.11.2022
 *
 * The extension that allows configuring and controlling
 * individual projects within a monorepo
 */
public interface AffectedProjectExtension {

    /**
     * Determins , if this project is generally enabled and taken into consideration
     * by the affected plugin
     */
    Property<Boolean> getEnabled();

    /**
     * Determines, if the specified target task can run on this project
     */
    Property<Boolean> getIsRunnableProject();

    /**
     * Determines, if this project can affect dependents if changes are available
     */
    Property<Boolean> getIsAffectingAllowed();

}