package r01hp.portal.appembed;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import r01f.types.Path;

@NoArgsConstructor(access=AccessLevel.PRIVATE)
abstract class R01HPortalPageAppEmbedServletFilterHelp {
/////////////////////////////////////////////////////////////////////////////////////////
//	HELP
/////////////////////////////////////////////////////////////////////////////////////////
	public static String filterConfigHelp() {
        String msg = "*****************************************************************************************************************\n" +
                     "* The portal page app embedder filter can be configured by TWO means:                                           *\n" +
                     "*                                                                                                               *\n" +
                     "* [1] - Use a properties file located at {config_root_path}/r01hp/r01hp.portalpageappembedfilter.properties.xml *\n" +
                     "*       This file is like:                                                                                      *\n" +
                     "*       <portalpageappembedfilter environment ='local'>                                                         *\n" +
	                 "*           <!-- Resources NOT embedded into a portal page ============================================ -->     *\n" +
	                 "*           <!-- A list of regular expressions that will be matched agains the URL path of the resource -->     *\n" +
                     "*           <notEmbeddedResources>                                                                              *\n" +
		             "*               <urlPathRegExp>/not-embeded/.*</urlPathRegExp>                                                  *\n" +
		             "*               <urlPathRegExp>/also-not-embeded/.*</urlPathRegExp>                                             *\n" +
		             "*           </notEmbeddedResources>                                                                             *\n" +
		             "*                                                                                                               *\n" +
                     "*           <!-- Portal server configuration ====================================================           --> *\n" +
                     "*           <!--     Defines the location (filesystem path) of the container pages where                    --> *\n" +
                     "*           <!--     the app will be embedded, the default page to use and how these pages are cached       --> *\n" +
                     "*           <!--    Multiple environments  can be configured in the same file                               --> *\n" +
                     "*           <portalServer>                                                                                      *\n" +
                     "*               <cacheConfig>                                                                                   *\n" +
                     "*                   <initialCapacity>10</initialCapacity>                                                       *\n" +
                     "*                   <maxSize>100</maxSize>                                                                      *\n" +
                     "*                   <checkInterval>20s</checkInterval>                                                          *\n" +
                     "*               </cacheConfig>                                                                                  *\n" +
                     "*               <portalFiles>                                                                                   *\n" +
                     "*                   <root>d:/temp_dev/r01hp/</root>                                                             *\n" +
                     "*                   <pages>/html/pages/portal</pages>                                                           *\n" +
                     "*                   <defaultPortal>web01</defaultPortal>                                                        *\n" +
                     "*                   <defaultPage>eduki</defaultPage>                                                            *\n" +
                     "*                   <defaultLang>es</defaultLang>                                                               *\n" +
                     "*               </portalFiles>                                                                                  *\n" +
                     "*               <portalCookieName>r01hpPortalCookie</portalCookieName>                                          *\n" +
                     "*           </portalServer>                                                                                     *\n" +
                     "*       </portalpageappembedfilter>                                                                             *\n" +
                     "*                                                                                                               *\n" +
                     "*       <!-- Metrics see http://metrics.dropwizard.io/3.1.0/ ============================= -->                  *\n" +
                     "*       <metrics enabled='true'>                                                                                *\n" +
                     "*           <consoleReporter enabled='false' reportEvery='30s' />                                               *\n" +
                     "*                                                                                                               *\n" +
                     "*           <slf4jReporter enabled='true' reportEvery='30s'/>                                                   *\n" +
                     "*                                                                                                               *\n" +
                     "*           <!-- visualVM can be used to inspect metrics:                                                   --> *\n" +
                     "*           <!-- 1.- Install visualVM MBeans plugin: tools > plugins > Available plugins > [VisualVM MBeans]--> *\n" +
                     "*           <!-- 2.- Select [Tomcat] (or whatever) and go to the [MBeans] tab                               --> *\n" +
                     "*           <!-- 3.- Using the tree go to [Metrics]                                                         --> *\n" +
                     "*           <!-- 4.- double-clicking at any metric value a graph can be seen                                --> *\n" +
                     "*           <!-- <jmxReporter enabled='false'/>                                                             --> *\n" +
                     "*                                                                                                           --> *\n" +
                     "*           <!-- if metrics restservices are enabled info is available through admin servlet (restServices)     *\n" +
                     "*           <!-- METRICS:       http://localhost:8080/myWar/r01hpMetricsRestServicesServlet/metrics             *\n" +
                     "*           <!-- HEALTH-CHECK:  http://localhost:8080/myWar/r01hpMetricsRestServicesServlet/healthcheck         *\n" +
                     "*           <!-- THREADS:       http://localhost:8080/myWar/r01hpMetricsRestServicesServlet/threads             *\n" +
                     "*           <!-- PING:          http://localhost:8080/myWar/r01hpMetricsRestServicesServlet/ping                *\n" +
                     "*           <restServices>true</restServices>                                                                   *\n" +
                     "*       </metrics>                                                                                              *\n" +
                     "*                                                                                                               *\n" +
                     "*                                                                                                               *\n" +
                     "* [2] - Use the web.xml file                                                                                    *\n" +
                     "*       Configure the servlet filter like:                                                                      *\n" +
                     "*       <filter>                                                                                                *\n" +
                     "*           <filter-name>portalPageAppEmbedServletFilter</filter-name>                                          *\n" +
                     "*           <filter-class>r01hp.portal.appembed.R01HPortalPageAppEmbedServletFilter</filter-class>              *\n" +
                     "*                                                                                                               *\n" +
                     "*           <!--                                                                                                *\n" +
                     "*           **************************************************************************************************  *\n" +
                     "*           BEWARE! if these parameters are NOT set, the ones at r01hp.portalpageappembedfilter.properties.xml  *\n" +
                     "*                 are used but if neither the properties file is found, a default ones are used instead         *\n" +
                     "*                 These parameters ARE NOT MANDATORY (they can be omitted), BUT if they're present, these       *\n" +
                     "*                 values override the ones at the properties file (if present)                                  *\n" +
                     "*           Portal page app embed filter properties can be set by two means:                                    *\n" +
                     "*              1.- Provide an xml file with the properties                                                      *\n" +
                     "*                 2.- Provide individual property values                                                        *\n" +
                     "*              (if both are used, [1] has preference over [2])                                                  *\n" +
                     "*           **************************************************************************************************  *\n" +
                     "*           -->                                                                                                 *\n" +
                     "*                                                                                                               *\n" +
                     "*           <!-- [1]: properties file -->                                                                       *\n" +
                     "*           <init-param>                                                                                        *\n" +
                     "*             <param-name>r01hp.appembed.configFor</param-name>                                                 *\n" +
                     "*             <param-value>xxx.component_name</param-value>                                                     *\n" +
                     "*           </init-param>                                                                                       *\n" +
                     "*                                                                                                               *\n" +
                     "*           <!-- [2]: Individual property values (only if r01hp.appembed.configFor is NOT set) -->              *\n" +
                     "*                                                                                                               *\n" +
                     "*           <!-- A list of regular expressions (separated with ;) that will be matched against the URL path     *\n" +
                     "*                of the resource -->                                                                            *\n" +
                     "*           <init-param>                                                                                        *\n" +
                     "*               <param-name>r01hp.notEmbeddedResources</param-name>                                             *\n" +
                     "*                <param-value>/not-embedded/.*;/also-not-embedded/.*</param-value>                              *\n" +
                     "*           </init-param>                                                                                       *\n" +
                     "*                                                                                                               *\n" +
                     "*           <!-- true if codahale's metrics are enabled -->                                                     *\n" +
                     "*           <init-param>                                                                                        *\n" +
                     "*             <param-name>r01hp.appembed.metricsEnabled</param-name>                                            *\n" +
                     "*             <param-value>true</param-value>                                                                   *\n" +
                     "*           </init-param>                                                                                       *\n" +
                     "*                                                                                                               *\n" +
                     "*           <!-- the filesystem path where the container pages can be found -->                                 *\n" +
                     "*           <init-param>                                                                                        *\n" +
                     "*             <param-name>r01hp.appembed.appContainerPageFilesRootPath</param-name>                             *\n" +
                     "*             <param-value>d:/temp_dev/r01hp</param-value>                                                      *\n" +
                     "*           </init-param>                                                                                       *\n" +
                     "*           <init-param>                                                                                        *\n" +
                     "*             <param-name>r01hp.appembed.appContainerPageFilesRelPath</param-name>                              *\n" +
                     "*             <param-value>/html/pages/portal</param-value>                                                     *\n" +
                     "*           </init-param>                                                                                       *\n" +
                     "*                                                                                                               *\n" +
                     "*           <!-- the default portal/page/lang to be used if none can be guess from the request -->              *\n" +
                     "*           <init-param>                                                                                        *\n" +
                     "*             <param-name>r01hp.appembed.defaultPortal</param-name>                                             *\n" +
                     "*             <param-value>web01</param-value>                                                                  *\n" +
                     "*           </init-param>                                                                                       *\n" +
                     "*           <init-param>                                                                                        *\n" +
                     "*             <param-name>r01hp.appembed.defaultPage</param-name>                                               *\n" +
                     "*             <param-value>container2</param-value>                                                             *\n" +
                     "*           </init-param>                                                                                       *\n" +
                     "*           <init-param>                                                                                        *\n" +
                     "*             <param-name>r01hp.appembed.defaultLang</param-name>                                               *\n" +
                     "*             <param-value>es</param-value>                                                                     *\n" +
                     "*           </init-param>                                                                                       *\n" +
                     "*                                                                                                               *\n" +
                     "*           <!-- the container page file to be used it the requested one cannot be found                        *\n" +
                     "*                (beware! this file is loaded from the classpath) -->                                           *\n" +
                     "*           <init-param>                                                                                        *\n" +
                     "*             <param-name>r01hp.appembed.defaultContainerPageFileIfRequestedNotFound</param-name>               *\n" +
                     "*             <param-value>r01hp/portal/pages/r01hpDefaultAppContainerPortalPage.shtml</param-value>            *\n" +
                     "*           </init-param>                                                                                       *\n" +
                     "*       </filter>                                                                                               *\n" +
                     "*****************************************************************************************************************\n";
			return msg;
	
	}
	public static String filterDebugHelp(final boolean requestDebuggingGloballyEnabled,
										 final Path requestDebugFolderPath) {
		String msg = "\n\n\n" +
					 "****************************************************************************************************" +  "\n" +
					 "Portal page app embed filter request debug can be enabled at "                                        +  "\n" +
					 "r01hp.portalpageappembedfilter.properties.xml file: portalpageappembedfilter/requestDebug/@enabled"   +  "\n" +
					 "\tcurrent value=" + requestDebuggingGloballyEnabled                                                   +  "\n" +
					 "\tif the request contains the r01hpReqDebug=[token] param, all the request and response"              +  "\n" +
					 "\twill be logged to a file at " + requestDebugFolderPath.asAbsoluteString()  + "/[token].log"         +  "\n" +
					 "\t(see r01hp.portal.appembedR01HPortalPageAppEmbedServletFilterLogger.java"                           +  "\n" +
					 "BEWARE!!!!! DO NOT ACTIVATE THIS OPTION AT A PROD ENVIRONMENT"                                        +  "\n" +
					 "****************************************************************************************************" +  "\n" +
					 "\n\n\n";
		return msg;
	}
}
