package r01hp.bootstrap.lod;



import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.servlet.ServletModule;

import lombok.extern.slf4j.Slf4j;
import r01f.guids.CommonOIDs.AppCode;
import r01f.guids.CommonOIDs.AppComponent;
import r01f.util.types.collections.Lists;
import r01f.xmlproperties.XMLPropertiesBuilder;
import r01f.xmlproperties.XMLPropertiesForApp;
import r01f.xmlproperties.XMLPropertiesForAppComponent;
import r01hp.bootstrap.portal.appembed.R01HPortalPageEmbedServletFilterGuiceModule;
import r01hp.lod.config.R01HLODURIHandlerConfig;
import r01hp.lod.urihandler.filter.R01HLODURIHandlerServletFilter;
import r01hp.portal.appembed.R01HPortalPageAppEmbedServletFilter;
import r01hp.portal.appembed.R01HPortalPageAppEmbedServletFilterConfig;
import r01hp.portal.appembed.help.R01HPortalPageEmbedServletFilterDefaultHelp;



@Slf4j
public class R01HLODWarBootstrapGuiceModule
  implements Module {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public R01HLODWarBootstrapGuiceModule() {
		super();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	BIND
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void configure(final Binder binder) {
		log.warn("[START] LOD BOOTSTRAPPING.....................................");
		
		XMLPropertiesForApp xmlProps = XMLPropertiesBuilder.createForApp(AppCode.forId("r01hp"))
														   .notUsingCache();
		
		// [0] - Get & bind the properties		
		XMLPropertiesForAppComponent lodXmlProps = xmlProps.forComponent(AppComponent.forId("lod"));	
		final R01HLODURIHandlerConfig uriHandlerConfig = R01HLODURIHandlerConfig.loadFrom(lodXmlProps);
		log.debug("\n\n[URI HANDLER CONFIG]>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n{}",
				  uriHandlerConfig.debugInfo());		
		binder.bind(R01HLODURIHandlerConfig.class)
			  .toInstance(uriHandlerConfig);
		
		// [1] - Get the & bind the portal page app embed 
		//		 BEWARE the not-embedded urls
		XMLPropertiesForAppComponent appEmbedXmlProps = xmlProps.forComponent(AppComponent.forId("portalpageappembedfilter"));
		final R01HPortalPageAppEmbedServletFilterConfig appEmbedConfig = new R01HPortalPageAppEmbedServletFilterConfig(appEmbedXmlProps)
																				.withNotPortalEmbeddedUrlPatterns("^/id/.*",
																												  "^/elda/.*",
																												  "^/dataset/.*",
																												  "^/distribution/.*",
																												  "^/graph/.*",
																												  
																												  "^/def/.*",
																												  "^/kos/.*",
																												  "^/data/.*",
																												  
																												  "^/elda-assets/.*");
		log.debug("\n\n[PORTAL PAGE APP EMBED CONFIG]>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n{}",
				  appEmbedConfig.debugInfo());
		binder.install(new R01HPortalPageEmbedServletFilterGuiceModule(appEmbedConfig,
																	   R01HPortalPageEmbedServletFilterDefaultHelp.class));
		
		
		// [1] - Global filter: all requests go through this filter
		binder.install(new ServletModule() {
								@Override
								protected void configureServlets() {
									// portal page app embedder filter
									this.filterRegex("/(?:doc|sparql)/?.*")
										.through(R01HPortalPageAppEmbedServletFilter.class);
									this.filterRegex("/(?:read|write)/blazegraph/?.*")
										.through(R01HPortalPageAppEmbedServletFilter.class);
									
									
									// URI handler filter
									this.filterRegex("/(?:id|eli|dataset|distribution|graph|doc|data|sparql|def|kos)/?.*")		// filter("/*")
										.through(R01HLODURIHandlerServletFilter.class);
								}
					   });
		// [2] - Proxy request to the [triple-store] (/blazegraph/db/{resource}) 
		binder.install(new R01HLODTripleStoreProxyServletGuiceModule(uriHandlerConfig));
		
		// [3] - elda jersery filter
		binder.install(new R01HLODELDAServletGuiceModule(uriHandlerConfig));
		
		log.warn("[END]- LOD BOOTSTRAPPING.....................................");
	}
}
