package io.github.mklueh.affected.configuration;

import org.gradle.api.Project;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;


/**
 * Created by Marian at 23.12.2022
 */
class ArgumentsExtractorTest {

    @Test
    void extractParameterValueNull() {
        var project = Mockito.mock(Project.class);
        Mockito.doReturn(project).when(project).getRootProject();
        Mockito.doReturn(null).when(project).findProperty(any(String.class));
        assertTrue(ArgumentsExtractor.extractParameterValue(project, Arguments.EXECUTION_MODE).isEmpty());
    }
}
