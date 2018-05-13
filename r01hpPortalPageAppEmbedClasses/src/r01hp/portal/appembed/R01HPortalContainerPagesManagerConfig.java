package r01hp.portal.appembed;

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

@Accessors(prefix="_")
@RequiredArgsConstructor
public class R01HPortalContainerPagesManagerConfig
  implements ContainsConfigData,
  			 Debuggable {
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTANTS
/////////////////////////////////////////////////////////////////////////////////////////
    private static final Path DEFAULT_APP_CONTAINER_PAGE = Path.from("r01hp/portal/pages/r01hpDefaultAppContainerPortalPage.shtml");
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
    @Getter private final int _initialCapacity;
    @Getter private final int _maxSize;
    @Getter private final TimeLapse _appContainterPageModifiedCheckInterval;
    @Getter private final Path _defaultContainerPageFileIfRequestedNotFound;

    private final transient Memoized<Long> _appContainerPageModifiedCheckIntervalMilis = new Memoized<Long>() {
																									@Override
																									protected Long supply() {
																										return _appContainterPageModifiedCheckInterval.asMilis();
																									}
    																					};
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
    public R01HPortalContainerPagesManagerConfig() {
    	this(10,100,
    		 TimeLapse.createFor("20s"),
    		 DEFAULT_APP_CONTAINER_PAGE);
    }
    public R01HPortalContainerPagesManagerConfig(final XMLPropertiesForAppComponent props) {
    	this(props.propertyAt("/portalpageappembedfilter/portalServer/cacheConfig/initialCapacity").asInteger(10),props.propertyAt("/portalpageappembedfilter/portalServer/cacheConfig/maxSize").asInteger(100),
    		 props.propertyAt("/portalpageappembedfilter/portalServer/cacheConfig/checkInterval").asTimeLapse("200s"),
    		 props.propertyAt("/portalpageappembedfilter/portalServer/cacheConfig/defaultContainerPageFileIfRequestedNotFound").asPath(DEFAULT_APP_CONTAINER_PAGE));
    }
    public R01HPortalContainerPagesManagerConfig(final int initialCapacity,final int maxSize,
    										 	 final TimeLapse checkInterval) {
    	this(initialCapacity,maxSize,
    		 checkInterval,
    		 DEFAULT_APP_CONTAINER_PAGE);
    }
    public R01HPortalContainerPagesManagerConfig(final int initialCapacity,final int maxSize,
    										 	 final long appContainterPageModifiedCheckInterval,final TimeUnit unit) {
    	this(initialCapacity,maxSize,
    		 TimeLapse.createFor(appContainterPageModifiedCheckInterval,unit),
    		 DEFAULT_APP_CONTAINER_PAGE);
    }
    public R01HPortalContainerPagesManagerConfig(final int initialCapacity,final int maxSize,
    										 	 final long appContainterPageModifiedCheckInterval,final TimeUnit unit,
    										 	 final Path defaultAppContainerPage) {
    	this(initialCapacity,maxSize,
    		 TimeLapse.createFor(appContainterPageModifiedCheckInterval,unit),
    		 defaultAppContainerPage);
    }
/////////////////////////////////////////////////////////////////////////////////////////
//  GETTERS
/////////////////////////////////////////////////////////////////////////////////////////
    public long getAppContainerPageModifiedCheckIntervalMilis() {
    	return _appContainerPageModifiedCheckIntervalMilis.get();
    }
/////////////////////////////////////////////////////////////////////////////////////////
//  DEBUG
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public CharSequence debugInfo() {
		return Strings.customized("\t-Initial/Max size: {}/{}\n" +
								  "\t-Page modification check interval: {}\n" +
								  "\t-Default container page file if requested one NOT found: {}",
								  _initialCapacity,_maxSize,
								  _appContainterPageModifiedCheckInterval,
								  _defaultContainerPageFileIfRequestedNotFound);
	}
}
