Setup [OpenDataEuskadi] [LinkedOpenData] eclipse development environment
========================================================================

## [1]: Setup eclipse
Follow the guide at https://github.com/opendata-euskadi/java-utils/blob/master/base/r01fbDocs/eclipse/install/eclipse_install.read.me.md
(/r01fb/r01fbDocs/eclipse/install/eclipse_install.read.me.md)

## [2]: Create the project workspace
1.  create a dir `{dev_home}/workspaces/r01r01hp_linked_data`

2.  copy the _template_ workspace contents into the previously created

        cp {dev_home}/workspaces/master_[instance]/* {dev_home}/workspaces/r01r01hp_linked_data/

3. Launch eclipse from `{dev_home}/instances/[instance]`

## [3]: Clone [java-utils] (R01FB) projects
1. Create a folder for the [R01FB] base projects at `{dev_home}/projects_r01fb/`

2. Using [eclipse]'s GIT client import [R01FB] projects

    [file] > [import] > [Git] > [projects from git] > [clone uri]
    - URI: https://github.com/opendata-euskadi/java-utils/
    - Host: github.com
    - Repository path: /opendata-euskadi/java-utils/

    Destination: `{dev_home}/projects_r01fb/`

3. Import [R01FB] working sets (requires [AnyEdit tools] plugin)

    [file] > [import] > [working sets]
    select `{dev_home}/projects_r01fb/base/r01fbDocs/eclipse/working_sets/r01fb.workingsets.working_sets`

4. Resolve Ivys and ensure all [R01FB] projects compile

## [4]: Clone [linked-data-url-handler] (R01HP) projects_r01fb
1. Create a folder for the [R01FB] base projects at `{dev_home}/projects_linked_data/`

2. Using [eclipse]'s GIT client import [R01HP] projects

    [file] > [import] > [Git] > [projects from git] > [clone uri]
    - URI: https://github.com/opendata-euskadi/linked-data-url-handler
    - Host: github.com
    - Repository path: /opendata-euskadi/linked-data-url-handler/

    Destination: `{dev_home}/projects_linked_data/`


## [5]: Local properties
1. Create a **new** local [eclipse] project named `[r01PLATEALocalConfig]`  
(it's advisable to create a [source folder] named [config] and delete the default [source folder] named [src])

2.  Right click at [config] folder and select [new > folder]

3. Click on [Advanced >] options and select `link to alternate location (Linked Folder)`

4. Browse and select `{dev_home}/projects_r01fb/base/r01fbConfig/loc_win` (or loc_linux)

Repeat [3] and [4] for `{dev_home}/projects_linked_data/r01hpConfig/loc_win`


## [6]: Create a new [Tomcat] server
1. Select [Window] > [show view] > [Other...] > [servers]

2. Add a new [Tomcat 9] server

3. Add the [r01hpLODWar] project
