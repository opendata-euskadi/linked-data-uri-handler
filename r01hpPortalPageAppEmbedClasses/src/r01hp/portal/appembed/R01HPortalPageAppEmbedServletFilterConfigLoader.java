package r01hp.portal.appembed;

import java.util.Collection;
import java.util.regex.Pattern;

import javax.servlet.FilterConfig;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.FluentIterable;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import r01f.types.Path;
import r01f.types.TimeLapse;
import r01f.types.url.Url;
import r01f.util.types.Strings;
import r01hp.portal.appembed.config.R01HPortalPageLoaderConfig;
import r01hp.portal.appembed.config.R01HPortalPageLoaderConfigForFileSystemImpl;
import r01hp.portal.appembed.config.R01HPortalPageLoaderConfigForRESTServiceImpl;
import r01hp.portal.appembed.config.R01HPortalPageManagerConfig;
import r01hp.portal.common.R01HPortalPageCopy;

@Slf4j
@NoArgsConstructor(access=AccessLevel.PRIVATE)
abstract class R01HPortalPageAppEmbedServletFilterConfigLoader {
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////	
	/**
	 * Loads the {@link R01HPortalPageManagerConfig} from the web.xml config
	 * @param filterConfig
	 * @return
	 */
	public static R01HPortalPageManagerConfig loadPortalPageManagerConfigFrom(final FilterConfig filterConfig) {
		final int initialCapacity;                                    
		final int maxSize;                                            
		final TimeLapse appContainterPageModifiedCheckInterval;       
		final Path lastResourceContainerPageFileIfRequestedNotFound;
		
		// The default container page file to be used if the requested one is not found
		String lastResourceContainerPageFileIfRequestedNotFoundStr = filterConfig.getInitParameter("r01hp.appembed.defaultContainerPageFileIfRequestedNotFound");
		
		if (Strings.isNOTNullOrEmpty(lastResourceContainerPageFileIfRequestedNotFoundStr)) {
			Path newLastResourceContainerPageFileIfRequestedNotFound = Path.from(lastResourceContainerPageFileIfRequestedNotFoundStr);
																		
			lastResourceContainerPageFileIfRequestedNotFound = newLastResourceContainerPageFileIfRequestedNotFound;
		} else {
			lastResourceContainerPageFileIfRequestedNotFound = null;
		}
		initialCapacity = 10;
		maxSize = 100;
		appContainterPageModifiedCheckInterval = TimeLapse.createFor("20s");
		
		// return 
		return new R01HPortalPageManagerConfig(R01HPortalPageCopy.LIVE,
											   initialCapacity,maxSize,
											   appContainterPageModifiedCheckInterval,
											   lastResourceContainerPageFileIfRequestedNotFound);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	public static R01HPortalPageLoaderConfig loadPortalPageLoaderConfigFrom(final FilterConfig filterConfig) {
		String newAppContainerPageFilesRootPath = filterConfig.getInitParameter("r01hp.appembed.appContainerPageFilesRootPath");
		String newAppContainerPageFilesRelPathStr = filterConfig.getInitParameter("r01hp.appembed.appContainerPageFilesRelPath");
		String newAppContainerPageLoaderRESTServiceUrls = filterConfig.getInitParameter("r01hp.appembed.appContainerPageLoaderRESTServiceURLs");
				
		R01HPortalPageLoaderConfig outCfg = null;
		if (Strings.isNOTNullOrEmpty(newAppContainerPageFilesRootPath)
		 && Strings.isNOTNullOrEmpty(newAppContainerPageFilesRelPathStr)) {
			 log.warn("Location where to look after container page files overriden al web.xml (servlet filter init params): appContainerPageFilesRootPath={}, appContainerPageFilesRelPath={}",
					  newAppContainerPageFilesRootPath,newAppContainerPageFilesRelPathStr);
			
			 Path appContainerPageFilesRootPath = Path.from(newAppContainerPageFilesRootPath);
			 Path appContainerPageFilesRelPath = Path.from(newAppContainerPageFilesRelPathStr);
			 outCfg = new R01HPortalPageLoaderConfigForFileSystemImpl(appContainerPageFilesRootPath,appContainerPageFilesRootPath,
					 												  appContainerPageFilesRelPath);
		} else if (Strings.isNOTNullOrEmpty(newAppContainerPageLoaderRESTServiceUrls)) {
			 log.warn("REST service where to load container page files overriden al web.xml (servlet filter init params): appContainerPageLoaderRESTServiceURLs={}",
					  newAppContainerPageLoaderRESTServiceUrls);
			 Collection<Url> restServiceEndPointUrls = FluentIterable.from(Splitter.on(Pattern.compile("[;,]"))
					 										   					   .split(newAppContainerPageLoaderRESTServiceUrls))
					 												 .transform(new Function<String,Url>() {
																						@Override
																						public Url apply(final String url) {
																							return Url.from(url);
																						}
					 												 			})
					 												 .toList();
			 outCfg = new R01HPortalPageLoaderConfigForRESTServiceImpl(restServiceEndPointUrls);
		}
		return outCfg;
	}
}
