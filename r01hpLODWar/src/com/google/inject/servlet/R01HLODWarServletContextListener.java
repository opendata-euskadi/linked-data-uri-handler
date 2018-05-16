package com.google.inject.servlet;

import java.lang.ref.WeakReference;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.epimorphics.lda.restlets.RouterRestlet;
import com.epimorphics.lda.routing.Container;
import com.google.inject.Guice;
import com.google.inject.Injector;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import r01f.types.Path;
import r01f.util.types.Strings;
import r01hp.bootstrap.lod.R01HLODWarBootstrapGuiceModule;
import r01hp.lod.config.R01HLODURIHandlerConfig;

/**
 * A bit hack was needed to use GUICE with LDA since TWO servlet context listeners were needed:
 * 		- The servlet context listener that LDA uses: com.epimorphics.lda.restlets.RouterRestlet.Init
 *  	- The servlet context listener that bootstraps the guice injector: com.google.inject.servlet.GuiceServletContextListener
 * ... since there cannot be two servlet context listeners and multiple inheritance is NOT possible:
 * 		- the code of com.google.inject.servlet.GuiceServletContextListener was REPLICATED here
 * 		  (this requires that the java package was com.google.inject.servlet because some types are only module-visible)
 * 
 * 		- this type extends com.epimorphics.lda.restlets.RouterRestlet.Init and contextInitialized() / contextDestroyed()
 * 		  of this super type is called after guice initialization
 */
@NoArgsConstructor
@Slf4j
public class R01HLODWarServletContextListener
	 extends RouterRestlet.Init					// BEWARE!!!
  implements ServletContextListener {
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////	
  static final String INJECTOR_NAME = Injector.class.getName();

/////////////////////////////////////////////////////////////////////////////////////////
//  ServletContextListenerBase
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void contextInitialized(final ServletContextEvent servletContextEvent) {
		log.warn("\n" +
				 "*****************************************************************************\n" +
				 "BOOTSTRAPPING LinkedData WAR\n" +
				 "*****************************************************************************\n");
	    final ServletContext servletContext = servletContextEvent.getServletContext();
	
		// BEWARE!!! avoid NPE when calling servletContext.getRealPath(..) in .war or .ear deployments
		//			 - check com.epimorphics.lda.restlets.RouterRestlet$Init (line 128) -
		// (see https://stackoverflow.com/questions/12160639/what-does-servletcontext-getrealpath-mean-and-when-should-i-use-it)
		// 
		// 
		// [WLS Config]:
		//	 Config in [WLS console]: domain > [web applications] > [Archived Real Path Enabled]
		//	 NOTE:
		//	 it can also be done editing the domain's config.xml:
	    // 		<web-app-container>
	    //     		<show-archived-real-path-enabled>true</show-archived-real-path-enabled>
	    //  		</web-app-container>
	    //  
	    // [WEB-INF/weblogic.xml config]
		// <wls:container-descriptor>
	    //     <wls:show-archived-real-path-enabled>true</wls:show-archived-real-path-enabled>
	    // </wls:container-descriptor>
		// <wls:virtual-directory-mapping>
	  	// 		<wls:local-path>r01hpLODWar.war</wls:local-path>
	  	// 		<wls:url-pattern>*</wls:url-pattern>
		// </wls:virtual-directory-mapping>
		String realServletContextPath = servletContext.getRealPath("/");
		if (Strings.isNullOrEmpty(realServletContextPath)) log.error("\n\n\n\n\n\n{}\n\n\n\n\n\n",_composekWeblogicServerConfigMsg());
	    
	    
	    
	    // Set the Servletcontext early for those people who are using this class.
	    // NOTE: This use of the servletContext is deprecated.
	    GuiceFilter.servletContext = new WeakReference<ServletContext>(servletContext);
	
	    Injector injector = _createInjector();
	    injector.getInstance(InternalServletModule.BackwardsCompatibleServletContextProvider.class)
	        									  .set(servletContext);
	    servletContext.setAttribute(INJECTOR_NAME,
	    							injector);
	    
	    // call lda context initialized (see RouterRestlet.Init)
	    // BEWARE: Elda initialization	
	    // Elda uses a ttl (turtle) file as a config file
	    // The config file can be set in TWO diferent ways:
	    // 		[1] - Config at the WEB.XML file (see com.epimorphics.lda.routing.ServletUtils#specNamesFromInitParam())
	    //			  		The web.xml file contains a CONTEXT PARAM like:
	    //						<!-- ELDA CONFIG LOCATION -->
	    //						<context-param>
		//							<param-name>com.epimorphics.api.initialSpecFile</param-name>
		//							<param-value>${r01hpConfigPath}/elda/r01hp.elda.euskadi_es.config.ttl</param-value>
	    //						</context-param>
	    //			  		PROBLEM with this alternative:
	    //						The web.xml init parameter named=com.epimorphics.api.initialSpecFile that sets where the ELDA config file 
	    //						be an absolute path of a .ttl file
	    //				    	... BUT this path depends on the environment
	    //							- Tomcat allows init-param in web.xml file to use environment vars (set with -Dvar=value when starting the JVM)
		//								<context-param>
		//									<param-name>com.epimorphics.api.initialSpecFile</param-name>
		//									<param-value>${r01hpConfigPath}/elda/r01hp.elda.euskadi_es.config.ttl</param-value>
	    //								</context-param>
	    //							- Weblogic DOES NOT interpolates environment vars in web.xml init-params 
	    //						... so JVM env params cannot be used consistently between Tomcat & Weblogic
	    //
	    //		[2] - Config at a JVM's environment variable (see com.epimorphics.lda.routing.ServletUtils#specNamesFromSystemProperties())
	    //					Just set a JVM environment variable:
	    //						-Delda.spec={absolute path of the .ttl file}
	    // 
	    // BEWARE WINDOWS!!!
	    // File com.epimorphics.lda.restlets.RouterRestletSupport.java had to be modified and recompiled (see elda.install.read.me)
	    // since it did NOT supported WINDOWS config load
	    // 		String fullPath = specName.startsWith("/") ? specName : baseFilePath + specName;
	    // was dirty-patched to:
	    //	    String fullPath = specName.startsWith("/") 
		//	    			   || specName.startsWith("d:/") || specName.startsWith("D:/")
		//	    			   || specName.startsWith("c:/") || specName.startsWith("C:/") 
		//	    						? specName 
		//	    						: baseFilePath + specName;
	   
	    String servletContextInitParamValForEldaConfig = servletContext.getInitParameter(Container.INITIAL_SPECS_PARAM_NAME);
	    String servletContextInitParamValForEldaConfigPathPrefix = servletContext.getInitParameter(Container.INITIAL_SPECS_PREFIX_PATH_NAME);
	    String jvmEnvVarValForEldaConfig = System.getProperty(Container.ELDA_SPEC_SYSTEM_PROPERTY_NAME);
	    if (Strings.isNullOrEmpty(servletContextInitParamValForEldaConfig)
	     && Strings.isNullOrEmpty(jvmEnvVarValForEldaConfig)) {
	    	log.error("ELDA CONFIG: Cannot find ELDA .ttl config file path\n" + 
	    			  "Could NOT find either a web.xml init param called {} " +
	    			  "nor a JVM environment param called {}\n" + 
	    			  "{}",
	    			  Container.INITIAL_SPECS_PARAM_NAME,Container.ELDA_SPEC_SYSTEM_PROPERTY_NAME,
	    			  _eldaConfigFilePathMsg());
	    	
	    	R01HLODURIHandlerConfig config = injector.getInstance(R01HLODURIHandlerConfig.class);
	    	Path eldaTTLFilePath = config.getEldaConfig().getRootConfigPath()
	    			 								     .joinedWith("/elda/r01hp.elda.euskadi.config.ttl");
	    	log.warn("The jvm env var {}={} will be set by default",
	    			 Container.ELDA_SPEC_SYSTEM_PROPERTY_NAME,eldaTTLFilePath.asAbsoluteString());
	    	System.setProperty(Container.ELDA_SPEC_SYSTEM_PROPERTY_NAME,eldaTTLFilePath.asAbsoluteString());
	    } 
	    else if (Strings.isNOTNullOrEmpty(servletContextInitParamValForEldaConfig)
	    	    && Strings.isNullOrEmpty(jvmEnvVarValForEldaConfig)) {
	    	log.warn("ELDA CONFIG: using web.xml init param {}={}",
	    			 Container.INITIAL_SPECS_PARAM_NAME,servletContextInitParamValForEldaConfig);
	    } 
	    else if (Strings.isNullOrEmpty(servletContextInitParamValForEldaConfig)
	    		&& Strings.isNOTNullOrEmpty(jvmEnvVarValForEldaConfig)) {
	    	log.warn("ELDA CONFIG: using jvm env var {}={}",
	    			 Container.ELDA_SPEC_SYSTEM_PROPERTY_NAME,jvmEnvVarValForEldaConfig);	    	
	    } 
	    else {
	    	log.warn("ELDA CONFIG: Both web.xml init param {}={} and JVM env var {}={} are set: using the JVM env var",
	    			Container.INITIAL_SPECS_PARAM_NAME,servletContextInitParamValForEldaConfig,
	    			Container.ELDA_SPEC_SYSTEM_PROPERTY_NAME,jvmEnvVarValForEldaConfig);
	    }
	        	
    	super.contextInitialized(servletContextEvent);
	    	
	}
	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
		ServletContext servletContext = servletContextEvent.getServletContext();
		servletContext.removeAttribute(INJECTOR_NAME);
		
		// call lda context destroyed (see RouterRestlet.Init)
		super.contextDestroyed(servletContextEvent);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	private Injector _createInjector() {
		log.warn("warming up guice injector");
		return Guice.createInjector(new R01HLODWarBootstrapGuiceModule());
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	MESAGES
/////////////////////////////////////////////////////////////////////////////////////////
	private static String _eldaConfigFilePathMsg() {
        String msg = "********************************************************************************************************************************************\n" +
                     "* The config file can be set in TWO diferent ways:                                                                                         *\n" +
                     "* [1] - Config at the WEB.XML file (see com.epimorphics.lda.routing.ServletUtils#specNamesFromInitParam())                                      *\n" +
                     "*                   The web.xml file contains a CONTEXT PARAM like:                                                                        *\n" +
                     "*                     <!-- ELDA CONFIG LOCATION -->                                                                                        *\n" +
                     "*                     <context-param>                                                                                                      *\n" +
                     "*                         <param-name>com.epimorphics.api.initialSpecFile</param-name>                                                     *\n" +
                     "*                         <param-value>${r01hpConfigPath}/elda/r01hp.elda.euskadi_es.config.ttl</param-value>                              *\n" +
                     "*                     </context-param>                                                                                                     *\n" +
                     "*                   PROBLEM with this alternative:                                                                                         *\n" +
                     "*                     The web.xml init parameter named=com.epimorphics.api.initialSpecFile that sets where the ELDA config file            *\n" +
                     "*                     be an absolute path of a .ttl file                                                                                   *\n" +
                     "*                     ... BUT this path depends on the environment                                                                         *\n" +
                     "*                         - Tomcat allows init-param in web.xml file to use environment vars (set with -Dvar=value when starting the JVM)  *\n" +
                     "*                             <context-param>                                                                                              *\n" +
                     "*                                 <param-name>com.epimorphics.api.initialSpecFile</param-name>                                             *\n" +
                     "*                                 <param-value>${r01hpConfigPath}/elda/r01hp.elda.euskadi_es.config.ttl</param-value>                      *\n" +
                     "*                             </context-param>                                                                                             *\n" +
                     "*                         - Weblogic DOES NOT interpolates environment vars in web.xml init-params                                         *\n" +
                     "*                     ... so JVM env params cannot be used consistently between Tomcat & Weblogic                                          *\n" +
                     "*                                                                                                                                          *\n" +
                     "* [2] - Config at a JVM's environment variable (see com.epimorphics.lda.routing.ServletUtils#specNamesFromSystemProperties())                   *\n" +
                     "*                 Just set a JVM environment variable:                                                                                     *\n" +
                     "*                     -Delda.spec={absolute path of the .ttl file}                                                                         *\n" +
                     "*                                                                                                                                          *\n" +
                     "* BEWARE!! If neither [1] nor [2] is used, the bootstrap routine will set -Delda.spec={absolute path of the .ttl file} automatically using *\n" +
                     "*          the r01hp.lod.properties.xml file                                                                                               *\n" +  
                     "********************************************************************************************************************************************\n";
		return msg;
	}
	private static String _composekWeblogicServerConfigMsg() {
		String msg = "***************************************************************************************************************************" +
					 "* BEWARE!!! avoid NPE when calling servletContext.getRealPath(..) in .war deployments                                     *"  +
					 "* (see https://stackoverflow.com/questions/12160639/what-does-servletcontext-getrealpath-mean-and-when-should-i-use-it)   *"  +
					 "*                                                                                                                         *"  +
					 "* [WLS Config]:                                                                                                           *"  +
					 "*	 Config in [WLS console]: domain > [web applications] > [Archived Real Path Enabled]                                    *"  +
					 "*	 NOTE:                                                                                                                  *"  +
					 "*	 it can also be done editing the domain's config.xml:                                                                   *"  +
				     "* 		<web-app-container>                                                                                             *"  +
				     "*     		<show-archived-real-path-enabled>true</show-archived-real-path-enabled>                                     *"  +
				     "*  		</web-app-container>                                                                                            *"  +
				     "*                                                                                                                         *"  +
				     "* [WEB-INF/weblogic.xml config]                                                                                           *"  +
					 "* <wls:container-descriptor>                                                                                              *"  +
				     "*     <wls:show-archived-real-path-enabled>true</wls:show-archived-real-path-enabled>                                     *"  +
				     "* </wls:container-descriptor>                                                                                             *"  +
					 "* <wls:virtual-directory-mapping>                                                                                         *"  +
				  	 "* 	<wls:local-path>r01hpLODWar.war</wls:local-path>                                                                    *"  +
				  	 "* 	<wls:url-pattern>*</wls:url-pattern>                                                                                *"  +
					 "* </wls:virtual-directory-mapping>                                                                                        *"  +
				  	 "***************************************************************************************************************************";
		return msg;
	}	
}
