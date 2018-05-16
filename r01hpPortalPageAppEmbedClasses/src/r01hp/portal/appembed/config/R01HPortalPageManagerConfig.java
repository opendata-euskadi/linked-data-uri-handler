package r01hp.portal.appembed.config;

import java.util.concurrent.TimeUnit;

import javax.servlet.FilterConfig;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.config.ContainsConfigData;
import r01f.debug.Debuggable;
import r01f.patterns.Memoized;
import r01f.types.Path;
import r01f.types.TimeLapse;
import r01f.util.types.Strings;
import r01f.xmlproperties.XMLPropertiesForAppComponent;

@Slf4j
@Accessors(prefix="_")
@RequiredArgsConstructor
public class R01HPortalPageManagerConfig
  implements ContainsConfigData,
  			 Debuggable {
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTANTS
/////////////////////////////////////////////////////////////////////////////////////////
    public static final Path DEFAULT_APP_CONTAINER_PAGE = Path.from("r01hp/portal/pages/r01hpDefaultAppContainerPortalPage.shtml");
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
    @Getter private final int _initialCapacity;
    @Getter private final int _maxSize;
    @Getter private final TimeLapse _appContainterPageModifiedCheckInterval;
    @Getter private final Path _lastResourceContainerPageFileIfRequestedNotFound;

    private final transient Memoized<Long> _appContainerPageModifiedCheckIntervalMilis = new Memoized<Long>() {
																								@Override
																								protected Long supply() {
																									return _appContainterPageModifiedCheckInterval.asMilis();
																								}
    																					};
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
    public R01HPortalPageManagerConfig() {
    	this(10,100,
    		 TimeLapse.createFor("20s"),
    		 DEFAULT_APP_CONTAINER_PAGE);
    }
    public R01HPortalPageManagerConfig(final int initialCapacity,final int maxSize,
    								   final TimeLapse checkInterval) {
    	this(initialCapacity,maxSize,
    		 checkInterval,
    		 DEFAULT_APP_CONTAINER_PAGE);
    }
    public R01HPortalPageManagerConfig(final int initialCapacity,final int maxSize,
    								   final long appContainterPageModifiedCheckInterval,final TimeUnit unit) {
    	this(initialCapacity,maxSize,
    		 TimeLapse.createFor(appContainterPageModifiedCheckInterval,unit),
    		 DEFAULT_APP_CONTAINER_PAGE);
    }
    public R01HPortalPageManagerConfig(final int initialCapacity,final int maxSize,
    								   final long appContainterPageModifiedCheckInterval,final TimeUnit unit,
    								   final Path defaultAppContainerPage) {
    	this(initialCapacity,maxSize,
    		 TimeLapse.createFor(appContainterPageModifiedCheckInterval,unit),
    		 defaultAppContainerPage);
    }
    public R01HPortalPageManagerConfig(final Path defaultAppContainerPage) {
    	this(10,100,
    		 TimeLapse.createFor("20s"),
    		 defaultAppContainerPage);
    }
    public R01HPortalPageManagerConfig(final FilterConfig filterConfig) {
		// [2] - The default container page file to be used if the requested one is not found
		String lastResourceContainerPageFileIfRequestedNotFoundStr = filterConfig.getInitParameter("r01hp.appembed.defaultContainerPageFileIfRequestedNotFound");
		
		if (Strings.isNOTNullOrEmpty(lastResourceContainerPageFileIfRequestedNotFoundStr)) {
			Path newLastResourceContainerPageFileIfRequestedNotFound = Path.from(lastResourceContainerPageFileIfRequestedNotFoundStr);
			
			log.warn("Default container page file to be used if the requested one is not found overriden al web.xml (servlet filter init params): {}",
				 	 newLastResourceContainerPageFileIfRequestedNotFound);
																		
			_lastResourceContainerPageFileIfRequestedNotFound = newLastResourceContainerPageFileIfRequestedNotFound;
		} else {
			_lastResourceContainerPageFileIfRequestedNotFound = null;
		}
		_initialCapacity = 10;
		_maxSize = 100;
		_appContainterPageModifiedCheckInterval = TimeLapse.createFor("20s");
    }
    public R01HPortalPageManagerConfig(final XMLPropertiesForAppComponent props) {
    	this(// cache initial capacity & maxsize
			 props.propertyAt("/portalpageappembedfilter/portalServer/cacheConfig/initialCapacity").asInteger(10),props.propertyAt("/portalpageappembedfilter/portalServer/cacheConfig/maxSize").asInteger(100),
			 // cache check interval
			 props.propertyAt("/portalpageappembedfilter/portalServer/cacheConfig/checkInterval").asTimeLapse("200s"),
			 // last resource page 															
			 props.propertyAt("/portalpageappembedfilter/portalServer/cacheConfig/defaultContainerPageFileIfRequestedNotFound").asPath(DEFAULT_APP_CONTAINER_PAGE));
    }
/////////////////////////////////////////////////////////////////////////////////////////
//  GETTERS
/////////////////////////////////////////////////////////////////////////////////////////
    public long getAppContainerPageModifiedCheckIntervalMilis() {
    	return _appContainerPageModifiedCheckIntervalMilis.get();
    }
/////////////////////////////////////////////////////////////////////////////////////////
//	CLONE
/////////////////////////////////////////////////////////////////////////////////////////
    public R01HPortalPageManagerConfig cloneOverriddenWith(final R01HPortalPageManagerConfig other) {
    	int initialCapacity = other.getInitialCapacity() > 0 ? other.getInitialCapacity() : this.getInitialCapacity();
    	int maxSize = other.getMaxSize() > 0 ? other.getMaxSize() : this.getMaxSize();
    	TimeLapse appContainterPageModifiedCheckInterval = other.getAppContainterPageModifiedCheckInterval() != null ? other.getAppContainterPageModifiedCheckInterval()
    																												 : this.getAppContainterPageModifiedCheckInterval();
    	Path lastResourceContainerPageFileIfRequestedNotFound = other.getLastResourceContainerPageFileIfRequestedNotFound() != null ? other.getLastResourceContainerPageFileIfRequestedNotFound()
    																															    : this.getLastResourceContainerPageFileIfRequestedNotFound();
    	return new R01HPortalPageManagerConfig(initialCapacity,maxSize,
    										   appContainterPageModifiedCheckInterval,
    										   lastResourceContainerPageFileIfRequestedNotFound);
    }
/////////////////////////////////////////////////////////////////////////////////////////
//  DEBUG
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public CharSequence debugInfo() {
		return Strings.customized("\t-Initial/Max size: {}/{}\n" +
								  "\t-Page modification check interval: {}\n" +
								  "\t-Last resource container page file if requested one NOT found: {}",
								  _initialCapacity,_maxSize,
								  _appContainterPageModifiedCheckInterval,
								  _lastResourceContainerPageFileIfRequestedNotFound);
	}
}
