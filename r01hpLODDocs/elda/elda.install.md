Doc: http://epimorphics.github.io/elda/current/reference.html

# Build ELDA
====================================================================================================================
(see: http://epimorphics.github.io/elda/current/reference.html#building-elda)


## PRE-REQUISITE 1: Install MAVEN
1.- Download MAVEN from https://maven.apache.org/  
2.- Extract to /java/apache-maven/  
3.- Create a mvn-env.cmd (or mvn-env.sh) at /java/env/

```
	@REM ======================================================================
	@REM == SET GENERAL ENV
	@REM ======================================================================
	call d:/java/env/pcienv_jdk8.cmd

	@REM == ENVIRONMENT =======================================================
	set MVN_HOME=D:/java/apache-maven
	set MVN_BIN=%MVN_HOME%/bin/

	set PATH=%PATH%;%MVN_BIN%

	set JVM_ARGS="-Duser.language=en -Duser.region=EN"
```

4.- Check  

		> mvn -v

5.- Configure the local repository at /java/apache-maven/conf/settings.xml
    (see https://maven.apache.org/configure.html and https://maven.apache.org/settings.html)

    set: <localRepository>{dev_home}/maven_libs</localRepository>
    (dev_home is `d:\eclipse` in windows or `/develop/ecipse` in linux)

## [1] - Clone ELDA Git repo

Create a folder where the cloned repo will be stored

    ie: /{dev_home}/projects_lod/

If git client is available:

	/{dev_home}/projects_lod/git/elda/> git clone https://github.com/epimorphics/elda.git

otherwise use [eclipse] to import a GIT remote project
(the git repository can be cloned although no eclipse project can be imported)

another option is to use [github desktop] to clone the remote repo

## [2] - MAVEN BUILD

**BEWARE!! JDK**
If jdk is other than jdk 1.8  
> 1.- change the jdk ref at mvn-env.cmd  
> 2.- Edit D:/eclipse/projects_platea_lod/git/elda/elda/pom.xml   
> 3.- Change property: <jdk.version>1.7</jdk.version>  


**Install**

	/{dev_home}/projects_lod/git/elda/elda> mvn clean install

BEWARE
If any TEST fails, try:

	mvn clean install -DskipTests   /     mvn clean install -Dmaven.test.skip=true
	or
	mvn install -DskipTests         /     mvn install -Dmaven.test.skip=true

(see http://maven.apache.org/plugins-archives/maven-surefire-plugin-2.12.4/examples/skipping-test.html)

All the dependencies should be downloaded to `/{dev_home}/eclipse/maven_libs`  
... the generated WAR for elda-common should be at: `    /{dev_home}/projects_lod/git/elda/elda/elda-common/target`  
... and the generated WAR for elda-assets should be at: `/{dev_home}/projects_lod/git/elda/elda/elda-assets/target`

BEWARE!
Sometimes when running an SPARQL query the following exception arises:

	Problem running query for SparqlSource{http://xxx0/blazegraph/sparql; unauthenticated}: Problems with HTTP response (was it an HTTP server?)

The reason is the jena-arq jar version (see /r01hpLODWar/WebContent/WEB-INF/lib/ 
The version downloaded by maven is jena-arq-2.10.2; BUT this version fails... download manually jena-arq-2.11.2 version
and replace the other version



## [3] - ECLIPSE PROJECT: r01hpLODWar

a) Copy the content of `/{dev_home}/projects_lod/git/elda/elda/elda-common/target/elda-common` to `[r01hpLODWar]\WebContent` folder

b) Compare `[elda-common]` libraries at `WebContent\WEB-INF\lib` with the project ones (resolved by ivy) and try to delete all the duplicate libraries from `WebContent\WEB-INF\lib`  
(another alternative is to include all the [elda]-required libraries in the ivy.xml file and completely delete all `WebContent\WEB-INF\lib content)`

[elda] url is mapped at `/doc/*` (see `r01hp.lod.bootstrap.R01HLODWarBootstrapGuiceModule`)



## [4] - ELDA Config

Elda uses a ttl (turtle) file as a config file  
The config file can be set in TWO diferent ways:

[1] - From the WEB.XML file (see `com.epimorphics.lda.routing.ServletUtils#specNamesFromInitParam()`)  
The web.xml file contains a CONTEXT PARAM like:  
```xml
	<context-param>
        <param-name>com.epimorphics.api.initialSpecFile</param-name>
        <param-value>${r01hpConfigPath}/elda/r01hp.elda.euskadi_es.config.ttl</param-value>
	</context-param>
```
PROBLEM with this alternative:

    The web.xml init parameter named=`com.epimorphics.api.initialSpecFile` that sets where the ELDA config file resides MUST be an absolute path of a .ttl file  
    ... BUT this path depends on the environment
    - Tomcat allows init-param in web.xml file to use environment vars (set with -Dvar=value when starting the JVM)

	<context-param>
		<param-name>com.epimorphics.api.initialSpecFile</param-name>
		<param-value>${r01hpConfigPath}/elda/r01hp.elda.euskadi_es.config.ttl</param-value>
	</context-param>

	- Weblogic DOES NOT interpolates environment vars in web.xml init-params

... so JVM env params cannot be used consistently between Tomcat & Weblogic

[2] - From a JVM's environment variable (see `com.epimorphics.lda.routing.ServletUtils#specNamesFromSystemProperties()`)
Just set a JVM environment variable:

		-Delda.spec={absolute path of the .ttl file}


BEWARE!!! WEBLOGIC: avoid NPE when calling servletContext.getRealPath(..) in .war deployments
=============================================================================================
[WLS console]: domain > [web applications] > [Archived Real Path Enabled]
NOTE:
it can also be done editing the domain's config.xml:
```xml
<web-app-container>
	<show-archived-real-path-enabled>true</show-archived-real-path-enabled>
</web-app-container>
```

(see WEB-INF/weblogic.xml)
