package r01hp.bootstrap.portal.appembed;

import javax.inject.Singleton;

import com.google.inject.Binder;
import com.google.inject.Module;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.uadetector.UserAgentStringParser;
import r01hp.portal.appembed.R01HPortalPageAppEmbedServletFilter;
import r01hp.portal.appembed.R01HUserAgentParser;
import r01hp.portal.appembed.config.R01HPortalPageAppEmbedServletFilterConfig;
import r01hp.portal.appembed.config.R01HPortalPageManagerConfig;
import r01hp.portal.appembed.config.R01HPortalPageLoaderConfig;
import r01hp.portal.appembed.help.R01HPortalPageEmbedServletFilterHelp;
import r01hp.portal.appembed.metrics.R01HPortalPageAppEmbedMetricsConfig;

@Slf4j
@RequiredArgsConstructor
public class R01HPortalPageEmbedServletFilterGuiceModule
  implements Module {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	private final R01HPortalPageAppEmbedServletFilterConfig _filterConfig;
	private final R01HPortalPageManagerConfig _pageManagerConfig;
	private final R01HPortalPageLoaderConfig _pageLoaderConfig;
	private final R01HPortalPageAppEmbedMetricsConfig _metricsConfig;
	private final Class<? extends R01HPortalPageEmbedServletFilterHelp> _filterHelpType;
/////////////////////////////////////////////////////////////////////////////////////////
//  MODULE
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public void configure(final Binder binder) {
		log.warn("[START BINDING APP CONTAINER PAGE FILTER]");
		
		// metrics bindings / metrics servlet bindings
		binder.install(new R01HMetricsGuiceBindingsModule(_metricsConfig));			// if metrics are NOT enabled, nothing is binded
		
		// portal page appembed filter help
		binder.bind(R01HPortalPageEmbedServletFilterHelp.class)
			  .to(_filterHelpType)
			  .in(Singleton.class);
		
		// filter config
		binder.bind(R01HPortalPageAppEmbedServletFilterConfig.class)
			  .toInstance(_filterConfig);
		
		// Portal page manager & page provider
		binder.bind(R01HPortalPageLoaderConfig.class)
			  .toInstance(_pageLoaderConfig);
		binder.bind(R01HPortalPageManagerConfig.class)
			  .toInstance(_pageManagerConfig);
		
		// Filter as singleton (guice requires it)
		binder.bind(R01HPortalPageAppEmbedServletFilter.class)
			  .in(Singleton.class);
		
		// UserAgent parser
		// (beware that the user agent parser lifecycle (thread start/stop) are managed 
		//	at servlet context starting / stopping)
		binder.bind(UserAgentStringParser.class)
			  .to(R01HUserAgentParser.class)
			  .in(Singleton.class);
		
		log.warn("[END BINDING APP CONTAINER PAGE FILTER]\n\n\n");
	}

}
