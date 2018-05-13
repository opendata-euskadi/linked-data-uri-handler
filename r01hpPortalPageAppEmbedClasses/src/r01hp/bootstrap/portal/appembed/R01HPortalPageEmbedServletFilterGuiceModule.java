package r01hp.bootstrap.portal.appembed;

import javax.inject.Singleton;

import com.google.inject.Binder;
import com.google.inject.Module;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.uadetector.UserAgentStringParser;
import r01hp.portal.appembed.R01HPortalContainerPageManager;
import r01hp.portal.appembed.R01HPortalPageAppEmbedServletFilter;
import r01hp.portal.appembed.R01HPortalPageAppEmbedServletFilterConfig;
import r01hp.portal.appembed.R01HUserAgentParser;
import r01hp.portal.appembed.help.R01HPortalPageEmbedServletFilterHelp;

@Slf4j
@RequiredArgsConstructor
public class R01HPortalPageEmbedServletFilterGuiceModule
  implements Module {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	private final R01HPortalPageAppEmbedServletFilterConfig _config;
	private final Class<? extends R01HPortalPageEmbedServletFilterHelp> _filterHelpType;
/////////////////////////////////////////////////////////////////////////////////////////
//  MODULE
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public void configure(final Binder binder) {
		log.warn("[START BINDING APP CONTAINER PAGE FILTER]");
		// config
		binder.bind(R01HPortalPageAppEmbedServletFilterConfig.class)
			  .toInstance(_config);
		
		// portalpageappembedfilter help
		binder.bind(R01HPortalPageEmbedServletFilterHelp.class)
			  .to(_filterHelpType)
			  .in(Singleton.class);
		
		// Portal page manager
		binder.bind(R01HPortalContainerPageManager.class)
			  .in(Singleton.class);
		
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
