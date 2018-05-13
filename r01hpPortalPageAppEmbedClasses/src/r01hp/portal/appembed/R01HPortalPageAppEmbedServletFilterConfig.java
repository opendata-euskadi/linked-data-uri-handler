package r01hp.portal.appembed;

import java.util.Collection;
import java.util.regex.Pattern;

import javax.servlet.FilterConfig;

import com.google.appengine.repackaged.com.google.common.collect.Lists;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterators;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.config.ContainsConfigData;
import r01f.debug.Debuggable;
import r01f.guids.CommonOIDs.Environment;
import r01f.locale.Language;
import r01f.types.Path;
import r01f.types.TimeLapse;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;
import r01f.util.types.locale.Languages;
import r01f.xmlproperties.XMLPropertiesForAppComponent;
import r01hp.portal.common.R01HPortalOIDs.R01HPortalID;
import r01hp.portal.common.R01HPortalOIDs.R01HPortalPageID;

@Slf4j
@Accessors(prefix="_")
public class R01HPortalPageAppEmbedServletFilterConfig
  implements ContainsConfigData,
  			 Debuggable {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTANTS
/////////////////////////////////////////////////////////////////////////////////////////
    private static final Environment LOC = Environment.LOCAL;
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
    /**
     * The environment
     */
    @Getter private final Environment _environment;
	/**
	 * Is the request debug globally enabled?
	 * (if it's NOT globally enabled, it doesn't mind that the request includes the reqLog parameter; no log will be created)
	 */
	@Getter private final boolean _requestDebuggingGloballyEnabled;
	/**
	 * The folder where all the request & response log files will be stored
	 */
	@Getter private final Path _requestDebugFolderPath;
	/**
	 * A list of regular expressions to be matched against the url path
	 * for resources that will NOT be embedded into a portal page
	 */
	@Getter private final Collection<Pattern> _notPortalPageEmbeddedResources;
	/**
	 * The context defaults
	 */
	@Getter private final R01HPortalPageAppEmbedContextDefaults _contextDefaults;
	/**
	 * portal page manager config
	 */
	@Getter private final R01HPortalContainerPagesManagerConfig _appContainerPortalPageManagerConfig;
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public R01HPortalPageAppEmbedServletFilterConfig(final Environment env,
													 final boolean requestDebuggingGloballyEnabled,final Path requestDebugFolderPath,
													 final Collection<Pattern> notPortalPageEmbeddedResources,
													 final R01HPortalPageAppEmbedContextDefaults contextDefaults,
													 final R01HPortalContainerPagesManagerConfig appContainerPortalPageManagerConfig) {
		_environment = env;
		_requestDebuggingGloballyEnabled = requestDebuggingGloballyEnabled;
		_requestDebugFolderPath = requestDebugFolderPath;
		_notPortalPageEmbeddedResources = notPortalPageEmbeddedResources;
		_contextDefaults = contextDefaults;
		_appContainerPortalPageManagerConfig = appContainerPortalPageManagerConfig;
	}
	public R01HPortalPageAppEmbedServletFilterConfig(final XMLPropertiesForAppComponent props) {
		if (props == null) {
			_environment = Environment.LOCAL;
			_requestDebuggingGloballyEnabled = false;
			_requestDebugFolderPath = Path.from("/datos/r01hp/log");
			_notPortalPageEmbeddedResources = null;
			_contextDefaults = R01HPortalPageAppEmbedContextDefaults.from(_environment,
																		 null);
			_appContainerPortalPageManagerConfig = new R01HPortalContainerPagesManagerConfig(10,100,
																							 TimeLapse.createFor("200s"));
		}
		else {
	    	_environment = props.propertyAt("portalpageappembedfilter/@environment")
								   .asEnvironment("LOC");
			_requestDebuggingGloballyEnabled = props.propertyAt("portalpageappembedfilter/requestDebug/@enabled")
											 		.asBoolean(false);
			_requestDebugFolderPath = props.propertyAt("portalpageappembedfilter/requestDebug/logFilesFolderPath")
										   .asPath("/datos/r01hp/log");
			_notPortalPageEmbeddedResources = _regExpsToPatternCol(props.propertyAt("portalpageappembedfilter/notEmbeddedResources")
																		.asListOfStrings());
			_contextDefaults = R01HPortalPageAppEmbedContextDefaults.from(_environment,
																		  props);
			_appContainerPortalPageManagerConfig = new R01HPortalContainerPagesManagerConfig(props);
		}
		String msg = "\n\n\n" +
					 "****************************************************************************************************" +  "\n" +
					 "Portal page app embed filter request debug can be enabled at "                                        +  "\n" +
					 "r01hp.portalpageappembedfilter.properties.xml file: portalpageappembedfilter/requestDebug/@enabled"   +  "\n" +
					 "\tcurrent value=" + _requestDebuggingGloballyEnabled                                                  +  "\n" +
					 "\tif the request contains the r01hpReqDebug=[token] param, all the request and response"              +  "\n" +
					 "\twill be logged to a file at " + _requestDebugFolderPath.asAbsoluteString()  + "/[token].log"        +  "\n" +
					 "\t(see r01hp.portal.appembedR01HPortalPageAppEmbedServletFilterLogger.java"                           +  "\n" +
					 "BEWARE!!!!! DO NOT ACTIVATE THIS OPTION AT A PROD ENVIRONMENT"                                        +  "\n" +
					 "****************************************************************************************************" +  "\n" +
					 "\n\n\n";
		log.warn("[R01HP: DEBUG]: {}",msg);
	}
	/**
	 * Creates NEW config object whose properties are overriden with the ones at web.xml file
	 * @param config
	 * @return
	 */
	public R01HPortalPageAppEmbedServletFilterConfig cloneOverridenWith(final FilterConfig config) {
		// set properties form web.xml  > just clone the config with the new params
		R01HPortalPageAppEmbedServletFilterConfig outProps = this;	// by default is this object

		// [1] - The resources that will not be embedded into portal pages
		String notEmbeddedResourcesStr = config.getInitParameter("r01hp.appembed.notEmbeddedResources");
		if (Strings.isNOTNullOrEmpty(notEmbeddedResourcesStr)) {
			Collection<String> regExps = Lists.newArrayList(Splitter.on(";")
																	.split(notEmbeddedResourcesStr));
			Collection<Pattern> newNotEmbeddedResources = _regExpsToPatternCol(regExps);
			if (_notPortalPageEmbeddedResources != null
			 && CollectionUtils.hasData(newNotEmbeddedResources)) newNotEmbeddedResources = Lists.newArrayList(Iterators.concat(_notPortalPageEmbeddedResources.iterator(),
																														  			  newNotEmbeddedResources.iterator()));
			outProps = new R01HPortalPageAppEmbedServletFilterConfig(outProps.getEnvironment(),
																	 outProps.isRequestDebuggingGloballyEnabled(),
																	 outProps.getRequestDebugFolderPath(),
																	 newNotEmbeddedResources,
																	 outProps.getContextDefaults(),
																	 outProps.getAppContainerPortalPageManagerConfig());
		}

		// [2] - The default container page file to be used if the requested one is not found
		String defaultContainerPageFileIfRequestedNotFoundStr = config.getInitParameter("r01hp.appembed.defaultContainerPageFileIfRequestedNotFound");
		if (Strings.isNOTNullOrEmpty(defaultContainerPageFileIfRequestedNotFoundStr)) {
			log.warn("Default container page file to be used if the requested one is not found overriden al web.xml (servlet filter init params): {}",
					 defaultContainerPageFileIfRequestedNotFoundStr);
			Path defaultContainerPageFileIfRequestedNotFound = Path.from(defaultContainerPageFileIfRequestedNotFoundStr);
			R01HPortalContainerPagesManagerConfig currentContainerPagesMgrCfg = outProps.getAppContainerPortalPageManagerConfig();
			R01HPortalContainerPagesManagerConfig newContainerPagesMgrCfg = new R01HPortalContainerPagesManagerConfig(currentContainerPagesMgrCfg.getInitialCapacity(),currentContainerPagesMgrCfg.getMaxSize(),
																													  currentContainerPagesMgrCfg.getAppContainterPageModifiedCheckInterval(),
																													  defaultContainerPageFileIfRequestedNotFound);
			outProps = new R01HPortalPageAppEmbedServletFilterConfig(outProps.getEnvironment(),
																	 outProps.isRequestDebuggingGloballyEnabled(),
																	 outProps.getRequestDebugFolderPath(),
																	 outProps.getNotPortalPageEmbeddedResources(),
																	 outProps.getContextDefaults(),
																	 newContainerPagesMgrCfg);
		}

		// [3] - The location where to look after container page files
		String appContainerPageFilesRootPathStr = config.getInitParameter("r01hp.appembed.appContainerPageFilesRootPath");
		String appContainerPageFilesRelPathStr = config.getInitParameter("r01hp.appembed.appContainerPageFilesRelPath");
		if (Strings.isNOTNullOrEmpty(appContainerPageFilesRootPathStr) && Strings.isNOTNullOrEmpty(appContainerPageFilesRelPathStr)) {
			log.warn("Location where to look after container page files overriden al web.xml (servlet filter init params): appContainerPageFilesRootPath={}, appContainerPageFilesRelPath={}",
					 appContainerPageFilesRootPathStr,appContainerPageFilesRelPathStr);

			Path appContainerFilesRootPath = Strings.isNOTNullOrEmpty(appContainerPageFilesRootPathStr) ? Path.from(appContainerPageFilesRootPathStr)
																										: outProps.getContextDefaults().getAppContainerPageFilesRootPath();
			Path appContainerFilesRelPath = Strings.isNOTNullOrEmpty(appContainerPageFilesRelPathStr) ? Path.from(appContainerPageFilesRelPathStr)
																										: outProps.getContextDefaults().getAppContainerPageFilesRelPath();
			R01HPortalPageAppEmbedContextDefaults currCtxDefaults = outProps.getContextDefaults();
			R01HPortalPageAppEmbedContextDefaults newCtxDefaults = new R01HPortalPageAppEmbedContextDefaults(appContainerFilesRootPath,appContainerFilesRelPath,
																										     currCtxDefaults.getDefaultPortalId(),currCtxDefaults.getDefaultAppContainerPageId(),currCtxDefaults.getDefaultLanguage(),
																										     currCtxDefaults.getPortalCookieName());
			outProps = new R01HPortalPageAppEmbedServletFilterConfig(outProps.getEnvironment(),
																     outProps.isRequestDebuggingGloballyEnabled(),
																     outProps.getRequestDebugFolderPath(),
																     outProps.getNotPortalPageEmbeddedResources(),
																     newCtxDefaults,
																     outProps.getAppContainerPortalPageManagerConfig());
		}

		// [4] - The default portal/page/lang to be used if none can be guess from the request
		String defPortalStr = config.getInitParameter("r01hp.appembed.defaultPortal");
		String defPageStr = config.getInitParameter("r01hp.appembed.defaultPage");
		String defLangStr = config.getInitParameter("r01hp.appembed.defaultLang");
		if (Strings.isNOTNullOrEmpty(defPortalStr) && Strings.isNOTNullOrEmpty(defPageStr)) {
			log.warn("Default portal / page / lang to be used if none can be guess from the request overriden al web.xml (servlet filter init params): defaultPortal={}, defaultPage={}, defaultLang={}",
					 defPortalStr,defPageStr,defLangStr);
			R01HPortalID portal = Strings.isNOTNullOrEmpty(defPortalStr) ? R01HPortalID.forId(defPortalStr)
																		 : outProps.getContextDefaults().getDefaultPortalId();
			R01HPortalPageID page = Strings.isNOTNullOrEmpty(defPageStr) ? R01HPortalPageID.forId(defPageStr)
																		 : outProps.getContextDefaults().getDefaultAppContainerPageId();
			Language lang = Strings.isNOTNullOrEmpty(defLangStr) ? Languages.fromLanguageCode(defLangStr)
															     : outProps.getContextDefaults().getDefaultLanguage() != null ? outProps.getContextDefaults().getDefaultLanguage()
																	  													  	  : Language.DEFAULT;
			R01HPortalPageAppEmbedContextDefaults currCtxDefaults = outProps.getContextDefaults();
			R01HPortalPageAppEmbedContextDefaults newCtxDefaults = new R01HPortalPageAppEmbedContextDefaults(currCtxDefaults.getAppContainerPageFilesRootPath(),currCtxDefaults.getAppContainerPageFilesRelPath(),
																										     portal,page,lang,
																										     currCtxDefaults.getPortalCookieName());
			outProps = new R01HPortalPageAppEmbedServletFilterConfig(outProps.getEnvironment(),
																     outProps.isRequestDebuggingGloballyEnabled(),
																     outProps.getRequestDebugFolderPath(),
																     outProps.getNotPortalPageEmbeddedResources(),
																     newCtxDefaults,
																     outProps.getAppContainerPortalPageManagerConfig());
		}
		return outProps;
	}
	public R01HPortalPageAppEmbedServletFilterConfig withNotPortalEmbeddedUrlPatterns(final String... patterns) {
		return this.withNotPortalEmbeddedUrlPatterns(FluentIterable.from(patterns)
																   .transform(new Function<String,Pattern>() {
																						@Override
																						public Pattern apply(final String pattern) {
																							return Pattern.compile(pattern);
																						}
																   			  })
																   .toList());
	}
	public R01HPortalPageAppEmbedServletFilterConfig withNotPortalEmbeddedUrlPatterns(final Pattern... patterns) {
		return this.withNotPortalEmbeddedUrlPatterns(Lists.newArrayList(patterns));
	}
	public R01HPortalPageAppEmbedServletFilterConfig withNotPortalEmbeddedUrlPatterns(final Collection<Pattern> patterns) {
		Collection<Pattern> newNotEmbeddedResources = patterns;
		if (_notPortalPageEmbeddedResources != null
		 && CollectionUtils.hasData(patterns)) newNotEmbeddedResources = Lists.newArrayList(Iterators.concat(_notPortalPageEmbeddedResources.iterator(),
																											 newNotEmbeddedResources.iterator()));
		return new R01HPortalPageAppEmbedServletFilterConfig(this.getEnvironment(),
															 this.isRequestDebuggingGloballyEnabled(),
															 this.getRequestDebugFolderPath(),
															 newNotEmbeddedResources,
															 this.getContextDefaults(),
															 this.getAppContainerPortalPageManagerConfig());
	}
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
    public boolean isLocalEnv() {
    	return _environment != null && _environment.is(LOC);
    }
/////////////////////////////////////////////////////////////////////////////////////////
//  DEBUG
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public CharSequence debugInfo() {
		StringBuilder sb = new StringBuilder();
		sb.append("Request debug enabled: ").append(_requestDebuggingGloballyEnabled);
		if (_requestDebuggingGloballyEnabled) {
			sb.append("(file path=").append(_requestDebugFolderPath).append(")");
		}
		if (CollectionUtils.hasData(_notPortalPageEmbeddedResources)) {
			sb.append("\nThe following url patterns will NOT be embedded into a portal page:\n\t-> ")
			  .append(CollectionUtils.toStringSeparatedWith(_notPortalPageEmbeddedResources,"\n\t-> "));
		}
		if (_contextDefaults != null) {
			sb.append("\nContext defaults:\n").append(_contextDefaults.debugInfo());
		}
		if (_appContainerPortalPageManagerConfig != null) {
			sb.append("\nPortal page manager:\n").append(_appContainerPortalPageManagerConfig.debugInfo());
		}
		return sb;
	}
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
/////////////////////////////////////////////////////////////////////////////////////////
//	PRIVATE
/////////////////////////////////////////////////////////////////////////////////////////
	private static Collection<Pattern> _regExpsToPatternCol(final Collection<String> regExps) {
		return CollectionUtils.hasData(regExps)
					? FluentIterable.from(regExps)
							// Filter empty strings
							.filter(new Predicate<String>() {
								@Override
								public boolean apply(final String regExp) {
									return Strings.isNOTNullOrEmpty(regExp);
								}
							})
							// Transform to Pattern
							.transform(new Function<String,Pattern>() {
												@Override
												public Pattern apply(final String regExp) {
													Pattern outPattern = null;
													try {
														outPattern = Pattern.compile(regExp);
													} catch(Throwable th) {
														log.error("Error in pattern {}: {}",
																  regExp,th.getMessage(),
																  th);
													}
													return outPattern;
												}
									   })
							// Filter nulls
							.filter(new Predicate<Pattern>() {
												@Override
												public boolean apply(final Pattern pattern) {
													return pattern != null;
												}
									})
							.toList()
					: null;
	}
}
