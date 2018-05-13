package r01hp.lod.servletfilter;

import java.util.Map;

import org.junit.Test;

import lombok.extern.slf4j.Slf4j;
import r01f.guids.CommonOIDs.AppCode;
import r01f.guids.CommonOIDs.AppComponent;
import r01f.util.types.collections.CollectionUtils;
import r01f.xmlproperties.XMLPropertiesBuilder;
import r01f.xmlproperties.XMLPropertiesForAppComponent;
import r01hp.lod.config.R01HLODTripleStoreConfig;
import r01hp.lod.config.R01HLODTripleStoreHostWithRole;
import r01hp.lod.config.R01HLODURIHandlerConfig;

@Slf4j
public class R01HTestLODProxyConfig {
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////	
	@Test
	public void testLODProxyConfig() {
		XMLPropertiesForAppComponent xmlProps = XMLPropertiesBuilder.createForApp(AppCode.forId("r01hp"))
																 	.notUsingCache()
																 	.forComponent(AppComponent.forId("lod"));	
		R01HLODURIHandlerConfig uriHandlerConfig = R01HLODURIHandlerConfig.loadFrom(xmlProps);
		
		boolean debug = false;
		
		log.info("SINGLE WLS_________________________________");
		Map<String,String> singleWLSProxyParams = R01HLODTripleStoreConfig.proxyServletParamsFor(uriHandlerConfig,
																								 R01HLODTripleStoreHostWithRole.READ_ROLE);
		_logParams(singleWLSProxyParams);

		log.info("CLUSTER WLS_________________________________");
		Map<String,String> clusterWLSProxyParams = R01HLODTripleStoreConfig.proxyWLSingleServletParamsFor(uriHandlerConfig,
																										  R01HLODTripleStoreHostWithRole.READ_ROLE);
		_logParams(clusterWLSProxyParams);
		
		log.info("PROXY_______________________________________");
		Map<String,String> proxyParams = R01HLODTripleStoreConfig.proxyWLClusterServletParamsFor(uriHandlerConfig,
																								 R01HLODTripleStoreHostWithRole.READ_ROLE);
		_logParams(proxyParams);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	private static void _logParams(final Map<String,String> params) {
		if (CollectionUtils.isNullOrEmpty(params)) log.warn("NO triple-sotre proxy params!!!");
		log.info("triple-store proxy params:");
		for (Map.Entry<String,String> me : params.entrySet()) {
			log.info("\t - {} = {}",me.getKey(),me.getValue());
		}
	}
}
