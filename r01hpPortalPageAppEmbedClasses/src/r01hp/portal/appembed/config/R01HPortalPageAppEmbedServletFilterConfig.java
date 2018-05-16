package r01hp.portal.appembed.config;

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
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;
import r01f.util.types.locale.Languages;
import r01f.xmlproperties.XMLPropertiesForAppComponent;
import r01hp.portal.appembed.R01HPortalPageAppEmbedContextDefaults;
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
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public R01HPortalPageAppEmbedServletFilterConfig() {
		this((XMLPropertiesForAppComponent)null);
	}
	public R01HPortalPageAppEmbedServletFilterConfig(final Environment env,
													 final boolean requestDebuggingGloballyEnabled,final Path requestDebugFolderPath,
													 final Collection<Pattern> notPortalPageEmbeddedResources,
													 final R01HPortalPageAppEmbedContextDefaults contextDefaults) {
		_environment = env;
		_requestDebuggingGloballyEnabled = requestDebuggingGloballyEnabled;
		_requestDebugFolderPath = requestDebugFolderPath;
		_notPortalPageEmbeddedResources = notPortalPageEmbeddedResources;
		_contextDefaults = contextDefaults;
	}
	public R01HPortalPageAppEmbedServletFilterConfig(final FilterConfig filterConfig) {
		// set properties form web.xml  > just clone the config with the new params
		_environment = null;
		_requestDebuggingGloballyEnabled = false;
		_requestDebugFolderPath = null;
		
		// [1] - The resources that will not be embedded into portal pages
		String notEmbeddedResourcesStr = filterConfig.getInitParameter("r01hp.appembed.notEmbeddedResources");
		if (Strings.isNOTNullOrEmpty(notEmbeddedResourcesStr)) {
			Collection<String> regExps = Lists.newArrayList(Splitter.on(";")
																	.split(notEmbeddedResourcesStr));
			_notPortalPageEmbeddedResources = _regExpsToPatternCol(regExps);
		} else {
			_notPortalPageEmbeddedResources = Lists.newArrayList();
		}

		// [2] - The default portal/page/lang to be used if none can be guess from the request
		String defPortalStr = filterConfig.getInitParameter("r01hp.appembed.defaultPortal");
		String defPageStr = filterConfig.getInitParameter("r01hp.appembed.defaultPage");
		String defLangStr = filterConfig.getInitParameter("r01hp.appembed.defaultLang");
		if (Strings.isNOTNullOrEmpty(defPortalStr) 
		 && Strings.isNOTNullOrEmpty(defPageStr)) {
			log.warn("Default portal / page / lang to be used if none can be guess from the request overriden al web.xml (servlet filter init params): defaultPortal={}, defaultPage={}, defaultLang={}",
					 defPortalStr,defPageStr,defLangStr);
			R01HPortalID portal = R01HPortalID.forId(defPortalStr);
			R01HPortalPageID page = R01HPortalPageID.forId(defPageStr);
			Language lang = Strings.isNOTNullOrEmpty(defLangStr) ? Languages.fromLanguageCode(defLangStr)
															     : Language.DEFAULT;
			_contextDefaults = new R01HPortalPageAppEmbedContextDefaults(portal,page,lang);
		} else {
			_contextDefaults = null;
		}
	}
	public R01HPortalPageAppEmbedServletFilterConfig(final XMLPropertiesForAppComponent props) {
		if (props == null) {
			_environment = Environment.forId("pro");
			_requestDebuggingGloballyEnabled = false;
			_requestDebugFolderPath = Path.from("/datos/r01hp/log");
			_notPortalPageEmbeddedResources = null;
			_contextDefaults = new R01HPortalPageAppEmbedContextDefaults(R01HPortalID.forId("web01"),R01HPortalPageID.forId("ejeduki"),Language.DEFAULT,
															 			 "r01hpPortalCookie");
		}
		else {
	    	_environment = props.propertyAt("portalpageappembedfilter/@environment")
								   .asEnvironment("loc");
			_requestDebuggingGloballyEnabled = props.propertyAt("portalpageappembedfilter/requestDebug/@enabled")
											 		.asBoolean(false);
			_requestDebugFolderPath = props.propertyAt("portalpageappembedfilter/requestDebug/logFilesFolderPath")
										   .asPath("/datos/r01hp/log");
			_notPortalPageEmbeddedResources = _regExpsToPatternCol(props.propertyAt("portalpageappembedfilter/notEmbeddedResources")
																		.asListOfStrings());
			_contextDefaults = new R01HPortalPageAppEmbedContextDefaults(
											// portal & page
											props.propertyAt("portalpageappembedfilter/portalServer/portalFiles/defaultPortal")
									  			 .asObjectFromString(R01HPortalID.class,"web01"),
									  		props.propertyAt("portalpageappembedfilter/portalServer/portalFiles/defaultPage")
									  		 	 .asObjectFromString(R01HPortalPageID.class,"ejeduki"),
									  		// language
									  	    props.propertyAt("portalpageappembedfilter/portalServer/portalFiles/defaultLang")
									  			 .asLanguageFromCode(Language.DEFAULT),
									  		// cookie name
									  		props.propertyAt("portalpageappembedfilter/portalServer/portalFiles/portalCookieName")
									  			 .asString("r01hpPortalCookie"));
		}
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
															 this.getContextDefaults());
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	public R01HPortalPageAppEmbedServletFilterConfig cloneOverriddenWith(final R01HPortalPageAppEmbedServletFilterConfig other) {
		// env
		Environment environment = other.getEnvironment();
		
		// debug
		boolean requestDebuggingGloballyEnabled = other.isRequestDebuggingGloballyEnabled();
		Path requestDebugFolderPath = other.getRequestDebugFolderPath();
		
		// not portal embedded resources
		Collection<Pattern> notPortalPageEmbeddedResources = null;
		if (CollectionUtils.hasData(this.getNotPortalPageEmbeddedResources()) 
		 && CollectionUtils.hasData(other.getNotPortalPageEmbeddedResources())) {
			notPortalPageEmbeddedResources = Lists.newArrayList(Iterators.concat(this.getNotPortalPageEmbeddedResources().iterator(),
																				 other.getNotPortalPageEmbeddedResources().iterator()));
		} else if (CollectionUtils.hasData(this.getNotPortalPageEmbeddedResources())) {
			notPortalPageEmbeddedResources = this.getNotPortalPageEmbeddedResources();
		} else if (CollectionUtils.hasData(other.getNotPortalPageEmbeddedResources())) {
			notPortalPageEmbeddedResources = other.getNotPortalPageEmbeddedResources();
		}
				
		// context defaults
		R01HPortalPageAppEmbedContextDefaults contextDefaults = null;
		if (this.getContextDefaults() != null 
		 && other.getContextDefaults() != null)  {
			contextDefaults = this.getContextDefaults()
								  .cloneOverriddenWith(other.getContextDefaults());
		} else if (this.getContextDefaults() != null) {
			contextDefaults = this.getContextDefaults();
		} else if (other.getContextDefaults() != null) {
			contextDefaults = other.getContextDefaults();
		}
		
		// return 
		return new R01HPortalPageAppEmbedServletFilterConfig(environment,
															 requestDebuggingGloballyEnabled,requestDebugFolderPath,
															 notPortalPageEmbeddedResources,
															 contextDefaults);
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
		return sb;
	}
/////////////////////////////////////////////////////////////
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
