package r01hp.bootstrap.core.lod;

import java.util.Map;

import com.google.inject.servlet.ServletModule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import r01f.guids.CommonOIDs.Role;
import r01f.util.types.collections.CollectionUtils;
import r01hp.lod.config.R01HLODTripleStoreConfig;
import r01hp.lod.config.R01HLODTripleStoreHostWithRole;
import r01hp.lod.config.R01HLODURIHandlerConfig;
import r01hp.lod.urihandler.filter.R01HLODSecurityServletFilter;
import r01hp.portal.appembed.R01HPortalPageAppEmbedServletFilter;

@Slf4j
@RequiredArgsConstructor
  class R01HLODTripleStoreProxyServletGuiceModule
extends ServletModule {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////	
	private final R01HLODURIHandlerConfig _uriHandlerConfig;
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	protected void configureServlets() {		
		// portal page embedd
		// Regex:	/(?:read|write)/blazegraph/? 	> read/blazegraph or write/blazegraph 
		//			(?:					
		//				any char NOT followed by html
		//				(?!html)		> negative look ahead
		//				.				> any char
		//			)*	> multiple chars NOT followed by thml
		this.filterRegex("/(?:read|write)/blazegraph/?(?:(?!html).)*")		// read|write/blazegraph NOT followed by anything ending in html
			.through(R01HPortalPageAppEmbedServletFilter.class);
		
		// Security filter
		this.filterRegex("/(?:read|write)/blazegraph")
			.through(R01HLODSecurityServletFilter.class);	// checks that /blazegraph is ONLY accessible from internal net
		
		
		// simple http proxy
		if (_uriHandlerConfig.getTripleStoreConfig().isUseGenericHttpProxy()) {
			log.info("... configuring triple-store proxy using generic http proxy");
			
			// ---- read triple-store
			this.serveRegex("/read/blazegraph/?.*")
				.with(new r01f.servlet.HttpProxyServlet(),
					  _proxyParamsFor(R01HLODTripleStoreHostWithRole.READ_ROLE));
			
			// ---- write triple-store
			this.serveRegex("/write/blazegraph/?.*")
				.with(new r01f.servlet.HttpProxyServlet(),
					  _proxyParamsFor(R01HLODTripleStoreHostWithRole.WRITE_ROLE));
		}
// LOCAL: Tomcat > do NOT commit commented!!
/*
		// WLProxy: cluster of triple-store servers
		else if (_uriHandlerConfig.getTripleStoreConfig().tripleStoreIsClustered()) {
			log.info("... configuring triple-store proxy using weblogic cluster-server proxy");
			
			// ---- read triple-store
			this.serveRegex("/read/blazegraph/?.*")
			 	.with(new weblogic.servlet.proxy.HttpClusterServlet(),					
			 		  _proxyParamsFor(R01HLODTripleStoreHostWithRole.READ_ROLE));	
			
			// ---- write triple-store
			this.serveRegex("/write/blazegraph/?.*")
			 	.with(new weblogic.servlet.proxy.HttpClusterServlet(),					
			 		  _proxyParamsFor(R01HLODTripleStoreHostWithRole.WRITE_ROLE));
		} 
		// WLProxy: single triple-store server
		else {
			log.info("... configuring triple-store proxy using weblogic single-server proxy");
			
			// ---- read triple-store
			this.serveRegex("/read/blazegraph/?.*")
				.with(new weblogic.servlet.proxy.HttpProxyServlet(),			
					  _proxyParamsFor(R01HLODTripleStoreHostWithRole.READ_ROLE));
			
			// ---- write triple-store
			this.serveRegex("/write/blazegraph/?.*")
				.with(new weblogic.servlet.proxy.HttpProxyServlet(),			
					  _proxyParamsFor(R01HLODTripleStoreHostWithRole.WRITE_ROLE));
		}
*/
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	private Map<String,String> _proxyParamsFor(final Role role) {
		Map<String,String> outParams = null;
		
		// simple http proxy
		if (_uriHandlerConfig.getTripleStoreConfig().isUseGenericHttpProxy()) {
			outParams = R01HLODTripleStoreConfig.proxyServletParamsFor(_uriHandlerConfig,
														 			   role);
		}
		// WLProxy: cluster of triple-store servers
		else if (_uriHandlerConfig.getTripleStoreConfig().tripleStoreIsClustered()) {
			outParams = R01HLODTripleStoreConfig.proxyWLClusterServletParamsFor(_uriHandlerConfig,
																			    role);
		} 
		// WLProxy: single triple-store server
		else {
			outParams = R01HLODTripleStoreConfig.proxyWLSingleServletParamsFor(_uriHandlerConfig,
															 				   role);
		}
		
		// return
		_logParams(outParams);
		return outParams;
	}
	private static void _logParams(final Map<String,String> params) {
		if (CollectionUtils.isNullOrEmpty(params)) log.warn("NO triple-store proxy params!!!");
		log.info("triple-store proxy params:");
		for (Map.Entry<String,String> me : params.entrySet()) {
			log.info("\t - {} = {}",me.getKey(),me.getValue());
		}
	}
}
