package r01hp.bootstrap.core.lod;



import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.servlet.ServletModule;

import lombok.extern.slf4j.Slf4j;
import r01f.guids.CommonOIDs.AppCode;
import r01f.guids.CommonOIDs.AppComponent;
import r01f.xmlproperties.XMLPropertiesBuilder;
import r01f.xmlproperties.XMLPropertiesForApp;
import r01f.xmlproperties.XMLPropertiesForAppComponent;
import r01hp.bootstrap.portal.appembed.R01HPortalPageEmbedServletFilterGuiceModule;
import r01hp.lod.config.R01HLODURIHandlerConfig;
import r01hp.lod.urihandler.filter.R01HLODURIHandlerServletFilter;
import r01hp.portal.appembed.R01HPortalPageAppEmbedServletFilter;
import r01hp.portal.appembed.config.R01HPortalPageAppEmbedServletFilterConfig;
import r01hp.portal.appembed.config.R01HPortalPageLoaderConfig;
import r01hp.portal.appembed.config.R01HPortalPageLoaderConfigBase;
import r01hp.portal.appembed.config.R01HPortalPageManagerConfig;
import r01hp.portal.appembed.help.R01HPortalPageEmbedServletFilterDefaultHelp;
import r01hp.portal.appembed.metrics.R01HPortalPageAppEmbedMetricsConfig;



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
		
		// [1] - PORTAL PAGE APP EMBED FILTER =================================================================================================
		XMLPropertiesForAppComponent appEmbedXmlProps = xmlProps.forComponent(AppComponent.forId("portalpageappembedfilter"));
		// a) app embedding
		final R01HPortalPageAppEmbedServletFilterConfig appEmbedConfig = new R01HPortalPageAppEmbedServletFilterConfig(appEmbedXmlProps)
																				// not portal-page embedded urls
																				.withNotPortalEmbeddedUrlPatterns("^/eli/.*",
																												  "^/dataset/.*",
																												  "^/distribution/.*",
																												  "^/graph/.*",
																												  
																												  "^/def/.*",
																												  "^/kos/.*",
																												  
																												  "^/elda-assets/.*");
		// b) portal page mgr
		final R01HPortalPageManagerConfig pageMgrConfig = new R01HPortalPageManagerConfig(appEmbedXmlProps);
		final R01HPortalPageLoaderConfig pageLoaderrConfig = R01HPortalPageLoaderConfigBase.createFrom(appEmbedXmlProps);
		
		// c) metrics
		final R01HPortalPageAppEmbedMetricsConfig metricsConfig = new R01HPortalPageAppEmbedMetricsConfig(appEmbedXmlProps);

		// d) install
		binder.install(new R01HPortalPageEmbedServletFilterGuiceModule(appEmbedConfig,
																	   pageMgrConfig,pageLoaderrConfig,
																	   metricsConfig,
																	   R01HPortalPageEmbedServletFilterDefaultHelp.class));
		
		// [2] - URIHandler ==================================================================================================================
		binder.install(new ServletModule() {
								@Override
								protected void configureServlets() {
									// portal page app embedder filter
									this.filterRegex("^/sparql/?.*")
										.through(R01HPortalPageAppEmbedServletFilter.class);									
									
									// URI handler filter 
									// only requests that comes from the web layer (NOT internal proxy requests to the triplestore server)
									String regEx = 	// any character NOT preceded by /read/blazegraph or /write/blazegraph
													"(?:" +					
														"(?<!/(?:read|write)/blazegraph/?)." +	// any character NOT preceded by /read/blazegraph or /write/blazegraph
													")*";	// multiple characters matching the previous
									this.filterRegex(regEx)		
										.through(R01HLODURIHandlerServletFilter.class);
								}
					   });
		
		// [3] - [triple-store] (/(read|write)/blazegraph/...) =============================================================================== 
		binder.install(new R01HLODTripleStoreProxyServletGuiceModule(uriHandlerConfig));
		
		// [4] - ELDA ========================================================================================================================
		binder.install(new R01HLODELDAServletGuiceModule(uriHandlerConfig));
		
		log.warn("[END]- LOD BOOTSTRAPPING.....................................");
	}
}
