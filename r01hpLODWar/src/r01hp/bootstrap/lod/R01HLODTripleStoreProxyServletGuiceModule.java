package r01hp.bootstrap.lod;

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
		// Security filter
		this.filter("/read/blazegraph/*")
			.through(R01HLODSecurityServletFilter.class);	// checks that /blazegraph is ONLY accessible from internal net
		this.filter("/write/blazegraph/*")
			.through(R01HLODSecurityServletFilter.class);	// checks that /blazegraph is ONLY accessible from internal net
		
		
		// simple http proxy
		if (_uriHandlerConfig.getTripleStoreConfig().isUseGenericHttpProxy()) {
			// ---- read triple-store
			this.serve("/read/blazegraph/*")
				.with(new r01f.servlet.HttpProxyServlet(),
					  _proxyParamsFor(R01HLODTripleStoreHostWithRole.READ_ROLE));
			
			// ---- write triple-store
			this.serve("/write/blazegraph/*")
				.with(new r01f.servlet.HttpProxyServlet(),
					  _proxyParamsFor(R01HLODTripleStoreHostWithRole.WRITE_ROLE));
		}
/* LOCAL: DO NOT COMMIT		
		// WLProxy: cluster of triple-store servers
		else if (_uriHandlerConfig.getTripleStoreConfig().tripleStoreIsClustered()) {
			// ---- read triple-store
			this.serve("/read/blazegraph/*")
			 	.with(new weblogic.servlet.proxy.HttpClusterServlet(),					
			 		  _proxyParamsFor(R01HLODTripleStoreHostWithRole.READ_ROLE));	
			
			// ---- write triple-store
			this.serve("/write/blazegraph/*")
			 	.with(new weblogic.servlet.proxy.HttpClusterServlet(),					
			 		  _proxyParamsFor(R01HLODTripleStoreHostWithRole.WRITE_ROLE));
		} 
		// WLProxy: single triple-store server
		else {
			// ---- read triple-store
			this.serve("/read/blazegraph/*")
				.with(new weblogic.servlet.proxy.HttpProxyServlet(),			
					  _proxyParamsFor(R01HLODTripleStoreHostWithRole.READ_ROLE));
			
			// ---- write triple-store
			this.serve("/write/blazegraph/*")
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
		if (CollectionUtils.isNullOrEmpty(params)) log.warn("NO triple-sotre proxy params!!!");
		log.info("triple-store proxy params:");
		for (Map.Entry<String,String> me : params.entrySet()) {
			log.info("\t - {} = {}",me.getKey(),me.getValue());
		}
	}
}
