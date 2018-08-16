package r01hp.portal.appembed.config;

import java.util.concurrent.TimeUnit;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.config.ContainsConfigData;
import r01f.debug.Debuggable;
import r01f.patterns.Memoized;
import r01f.types.Path;
import r01f.types.TimeLapse;
import r01f.util.types.Strings;
import r01f.xmlproperties.XMLPropertiesForAppComponent;
import r01hp.portal.common.R01HPortalPageCopy;

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
	@Getter private final R01HPortalPageCopy _copy;		// the page copy to be used: working copy or live copy
	
    @Getter private final int _initialCapacity;			// cache initial capacity
    @Getter private final int _maxSize;					// cache max capacity
    
    @Getter private final TimeLapse _appContainterPageModifiedCheckInterval;		// when to check for new page versions
    
    @Getter private final Path _lastResourceContainerPageFileIfRequestedNotFound;	// the path of the portal page when the requested one is NOT found

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
    	this(R01HPortalPageCopy.LIVE,
    		 10,100,
    		 TimeLapse.createFor("20s"),
    		 DEFAULT_APP_CONTAINER_PAGE);
    }
    public R01HPortalPageManagerConfig(final R01HPortalPageCopy copy,
    								   final int initialCapacity,final int maxSize,
    								   final TimeLapse checkInterval) {
    	this(copy,
    		 initialCapacity,maxSize,
    		 checkInterval,
    		 DEFAULT_APP_CONTAINER_PAGE);
    }
    public R01HPortalPageManagerConfig(final R01HPortalPageCopy copy,
    								   final int initialCapacity,final int maxSize,
    								   final long appContainterPageModifiedCheckInterval,final TimeUnit unit) {
    	this(copy,
    		 initialCapacity,maxSize,
    		 TimeLapse.createFor(appContainterPageModifiedCheckInterval,unit),
    		 DEFAULT_APP_CONTAINER_PAGE);
    }
    public R01HPortalPageManagerConfig(final R01HPortalPageCopy copy,
    								   final int initialCapacity,final int maxSize,
    								   final long appContainterPageModifiedCheckInterval,final TimeUnit unit,
    								   final Path defaultAppContainerPage) {
    	this(copy,
    		 initialCapacity,maxSize,
    		 TimeLapse.createFor(appContainterPageModifiedCheckInterval,unit),
    		 defaultAppContainerPage);
    }
    public R01HPortalPageManagerConfig(final Path defaultAppContainerPage) {
    	this(R01HPortalPageCopy.LIVE,
    		 10,100,
    		 TimeLapse.createFor("20s"),
    		 defaultAppContainerPage);
    }
    public R01HPortalPageManagerConfig(final XMLPropertiesForAppComponent props) {
    	this(// portal page copy to be used
    		 props.propertyAt("portalpageappembedfilter/portalServer/pageCopyToBeUsed")
				  .asEnumElement(R01HPortalPageCopy.class,
						  		 R01HPortalPageCopy.WORK),
    		 // cache initial capacity & maxsize
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
    	R01HPortalPageCopy copy = other.getCopy() != null ? other.getCopy() : this.getCopy();
    	int initialCapacity = other.getInitialCapacity() > 0 ? other.getInitialCapacity() : this.getInitialCapacity();
    	int maxSize = other.getMaxSize() > 0 ? other.getMaxSize() : this.getMaxSize();
    	TimeLapse appContainterPageModifiedCheckInterval = other.getAppContainterPageModifiedCheckInterval() != null ? other.getAppContainterPageModifiedCheckInterval()
    																												 : this.getAppContainterPageModifiedCheckInterval();
    	Path lastResourceContainerPageFileIfRequestedNotFound = other.getLastResourceContainerPageFileIfRequestedNotFound() != null ? other.getLastResourceContainerPageFileIfRequestedNotFound()
    																															    : this.getLastResourceContainerPageFileIfRequestedNotFound();
    	return new R01HPortalPageManagerConfig(copy,
    			 							   initialCapacity,maxSize,
    										   appContainterPageModifiedCheckInterval,
    										   lastResourceContainerPageFileIfRequestedNotFound);
    }
/////////////////////////////////////////////////////////////////////////////////////////
//  DEBUG
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public CharSequence debugInfo() {
		return Strings.customized("\t-Using page copy={}\n" +
								  "\t-Initial/Max size: {}/{}\n" +
								  "\t-Page modification check interval: {}\n" +
								  "\t-Last resource container page file if requested one NOT found: {}",
								  _copy,
								  _initialCapacity,_maxSize,
								  _appContainterPageModifiedCheckInterval,
								  _lastResourceContainerPageFileIfRequestedNotFound);
	}
}
