# Affected Plugin
A Gradle plugin to run a user defined task on changed projects (modules) and their dependent projects (modules) based on git changes.
This is based on either the HEAD commit, a specific commit id or even a commit range.

>Note: this is a fork of the original Changed Projects Task Plugin with the intention to mimic the
API of NX used for frontend projects. See [https://nx.dev/using-nx/affected](https://nx.dev/using-nx/affected)


It has a slightly different API than the original. For example, this command would run the task **release** for the applications **applicationModuleA** and **applicationModuleB** in case they or their dependent modules have changed

```shell
 ./gradlew --continue affected -Paffected.run -Paffected.target=release \
  -Paffected.projects=applicationModuleA,applicationModuleB
```

## Installation
The plugin is not intended to be available as a Maven artifact.

```groovy
affected {
    target = "test" //One of the main use cases of the plugin
    alwaysRunProject = [
            ":some-other-project"
    ]
    affectsAllRegex = [
            ~'build.gradle$' //Changes to the root build.gradle affects all projects
    ]
    ignoredRegex = [
            ~'^.*([.]css|[.]html)$' //Ignore changes to front-end files
    ]
}
```

## Configuration
As seen above, there are a few different configuration options available

| **Option**            | **Explanation**                                                                                                                                                                                                                                                                                                                                                                              |
|-----------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `debugLogging`        | Is default false and can be left out.<br/>If true will print details during plugin configuration and execution.                                                                                                                                                                                                                                                                              |
| `taskToRun`           | A name of a task to run on changed projects.                                                                                                                                                                                                                                                                                                                                                 |
| `alwaysRunProject`    | A set of string for project paths starting with `:` that will be run always when there is a not ignored changed file.                                                                                                                                                                                                                                                                        |
| `neverRunProject`     | A set of string for project paths starting with `:` that will never be run, even it is changed or `affectsAllRegex` has been evaluated to true.                                                                                                                                                                                                                                              |
| `affectsAllRegex`     | A set of regexes that if any file matches will cause the `taskToRun` to be executed for all projects.                                                                                                                                                                                                                                                                                        |
| `ignoredRegex`        | A set of regexes for files that are ignored when evaluating if any project has changed.                                                                                                                                                                                                                                                                                                      |
| `changedProjectsMode` | A string that denotes which mode the plugin is running in, either `ONLY_DIRECTLY` or `INCLUDE_DEPENDENTS`.<br/><br/>`INCLUDE_DEPENDENTS` is the default and causes the `taskToRun` to be executed for project that are changed and projects that depends on those changed.<br/><br/>`ONLY_DIRECTLY` causes the `taskToRun` to only be executed for projects that are changed and only those. |

## Usage
To use the added `affected` task from this plugin you need to run it the `-Paffected.run` parameter, which enables the plugin to run.


### Other CLI arguments

- `--continue` Depending on usage, it might be a good idea to run it with `--continue` such that all dependent tasks are run, instead of fail-fast behaviour.

- `-Paffected.target` let's you configure the task to run on demand. 
  If provided, the taskToRun is not required in the config anymore. It will also have higher priority as the task provided in the configuration.

- `-Paffected.projects` let's you specify a restricted list of modules that will be checked for     changed, including their module dependencies

- `-Paffected.commit` is to configure which ref to use in the git diff.
  - If this is specified with `-Paffected.prevCommit` it creates a range to use in diff.   
  By calling the following `git diff --name-only prevCommit~ commit`.
  - If it is specified with `-Paffected.prevCommit`, it uses the following command instead  
  `git diff --name-only commit~ commit`
  

- `-Paffected.prevCommit` is to configure which previous ref to use in the git diff.   
This cannot be used without also using `-Paffected.commit`


- `-Paffected.compareMode` is used to change which mode it uses to compare.
The following modes are available
  - `commit` (Default, the `-Paffected.commit` and `-Paffected.prevCommit` options are the commit ids and makes use of `~`)
  - `branch` (`-Paffected.commit` and `-Paffected.prevCommit` are now branch names and will be used like the following `git diff --name-only prev curr`, where `curr` is `-Paffected.commit`)
  - `branchTwoDotted` (`-Paffected.commit` and `-Paffected.prevCommit` are branch names and will be used like the following `git diff --name-only prev..curr`)
  - `branchThreeDotted` (`-Paffected.commit` and `-Paffected.prevCommit` are branch names and will be used like the following `git diff --name-only prev..curr`)

If either `-Paffected.commit` and `-Paffected.prevCommit` is not specified when running the `affected` command,
then that option simply defaults to `HEAD` if it is allowed to by the logic, otherwise an error is thrown.

The following table illustrates the allowed and available options and how the resulting diff command looks

| **Mode**          | **Current** | **Previous** | **Git diff command**               |
|-------------------|-------------|--------------|------------------------------------|
| commit            |             |              | `git diff --name-only HEAD~ HEAD`  |
| commit            | curr        |              | `git diff --name-only curr~ curr`  |
| commit            | curr        | prev         | `git diff --name-only prev~ curr`  |
| branch            | curr        | prev         | `git diff --name-only prev curr`   |
| branch            |             | prev         | `git diff --name-only prev HEAD`   |
| branchTwoDotted   | curr        | prev         | `git diff --name-only prev..curr`  |
| branchTwoDotted   |             | prev         | `git diff --name-only prev..`      |
| branchThreeDotted | curr        | prev         | `git diff --name-only prev...curr` |
| branchThreeDotted |             | prev         | `git diff --name-only prev...`     |

## Example for evaluating the plugin
This is a basic example you can use to evaluate the plugin on your project, apply the following to your own root `build.gradle`.

```groovy
plugins {
    id 'io.github.crimix.changed-projects-task' version 'VERSION'
}

allprojects {
    task print {
        doLast {
            println ">> " + project.path
        }
    }
}

affectedTask {
    taskToRun = "print"
}
```
Then run the following Gradle command line   
`affected -Paffected.run`

This example will print the path of all the projects that is affected by some change and write `Task x:print SKIPPED` for those not affected.  
You can use this to test how the plugin works and also set up the configuration of the plugin using real-world changes in your project.  
This way to can skip running time-consuming task like test when you are just configuring the plugin.

## FAQ
### Dependent task execution stops on first failed
Normally Gradle uses a fail-fast approach except for the test task. This means that if a dependent task fails the build stops.
Depending on the use case it can be preferable, but if this plugin is used to skip unit tests, the wanted behavior will probably to execute all test tasks.

The way to get the wanted behaviour is to run the task as the following
```
--continue affected -Paffected.run
```

This caused Gradle to execute all tasks even if the fail and still report the build as failed when it is done.
This way it is possible to run all dependent tasks and get all unit test results to present to the user. 


## Why did I make this
I have for at least a month been looking for a plugin or way to do this in Gradle.
I have found a few interesting articles and plugins/code snippets, but none that worked out-of-the-box or suited my needs. 

Thus, I began to write it using plain Groovy and Gradle, but when I was nearly done I stopped and thought for a moment, 
because all the embedded filtering and logic could be removed and put into a configuration instead, such that others could use it. 
Leading to this plugin being created
