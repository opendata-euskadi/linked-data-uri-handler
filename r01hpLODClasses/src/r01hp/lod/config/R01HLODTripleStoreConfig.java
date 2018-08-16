package r01hp.lod.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Maps;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.config.ContainsConfigData;
import r01f.debug.Debuggable;
import r01f.guids.CommonOIDs.Role;
import r01f.servlet.HttpProxyServletConfig;
import r01f.types.Path;
import r01f.types.TimeLapse;
import r01f.types.url.Host;
import r01f.types.url.Url;
import r01f.types.url.UrlPath;
import r01f.util.OSUtils;
import r01f.util.OSUtils.OSType;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

@Slf4j
@Accessors(prefix="_")
@RequiredArgsConstructor
public class R01HLODTripleStoreConfig 	
  implements ContainsConfigData,
  			 Debuggable {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter private final boolean _useGenericHttpProxy;
	@Getter private final TimeLapse _proxyTimeout;
	@Getter private final boolean _debugProxyEnabled;
	@Getter private final Collection<R01HLODTripleStoreHostWithRole> _internalTripleStoreServerHosts;
	@Getter private final UrlPath _internalSPARQLEndPointUrlPath;
/////////////////////////////////////////////////////////////////////////////////////////
//	SPARQL ENDPOINT URL
//	Beware that there're TWO sparql endpoint urls:
//		- The public one
//		- The internal one
//                                      {lang}.domain/sparql
//                                               +
//                                               |
//                                         +-----v------+
//                                         |   Public   |
//                                         |     Web    |
//                                         +------------+
//                                               |
//                                               |
//                                        +------v-------+
//                                        |  LOD Proxy   |
//                                        |              |
//                                        +--------------+
//                                               |
//                                               v
//                        triple-store-server:port/blazegraph/namespaces/db/sparql
//                                       +-----------------+
//                                       |   Triple-Store  |
//                                       |   (BlazeGraph)  |
//                                       |                 |
//                                       +-----------------+
/////////////////////////////////////////////////////////////////////////////////////////
	public UrlPath getPublicSPARQLEndPointUrlPath() {
		return new UrlPath("sparql");
	}
	public Url getPublicSPARQLEndPointUrlAt(final Host publicHost) {
		return new Url(publicHost,
					   this.getPublicSPARQLEndPointUrlPath());
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return true if there's more than a single triple-store host
	 */
	public boolean tripleStoreIsClustered() {
		return _internalTripleStoreServerHosts != null 
			&& _internalTripleStoreServerHosts.size() > 1;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	DEBUG
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public CharSequence debugInfo() {
		return Strings.customized("TripleStore end-point internal host={} url path pattern={}",
								  FluentIterable.from(_internalTripleStoreServerHosts)
								  				.transform(new Function<R01HLODTripleStoreHostWithRole,String>() {
																	@Override
																	public String apply(final R01HLODTripleStoreHostWithRole host) {
																		return host.debugInfo().toString();
																	}
								  						   })
								  				.toList(),
								  _internalSPARQLEndPointUrlPath);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	PROXY
/////////////////////////////////////////////////////////////////////////////////////////
	public static Map<String,String> proxyWLSingleServletParamsFor(final R01HLODURIHandlerConfig uriHandlerConfig,
																   final Role role) {
		// out properties (see R01HProxyDefForSingleWLS)
		//		- WebLogicHost = host
		//		- WebLogicPort = port
		//		- ConnectTimeoutSecs = timeout
		//		- PathTrim = read | write
		R01HLODTripleStoreHostWithRole targerServerHostWithRole = FluentIterable.from(uriHandlerConfig.getTripleStoreConfig()
																							  .getInternalTripleStoreServerHosts())
																		 .firstMatch(R01HLODTripleStoreHostWithRole.matcherFor(role))
																		 .orNull();
		if (targerServerHostWithRole == null) throw new IllegalStateException(Strings.customized("There's NO triple-store host with role={} configured!",
																			  					 role));
		Host targerServerHost = targerServerHostWithRole.getHost();
		Map<String,String> outWLSParams = Maps.newHashMapWithExpectedSize(3);
		outWLSParams.put("WebLogicHost",targerServerHost.asUrl().getHost().asString());
		outWLSParams.put("WebLogicPort",Integer.toString(targerServerHost.asUrl().getPortOrDefault(80)));		// port 80 by default
		if (uriHandlerConfig.getTripleStoreConfig().getProxyTimeout() != null) {
			outWLSParams.put("ConnectTimeoutSecs",Long.toString(uriHandlerConfig.getTripleStoreConfig().getProxyTimeout().asMilis() / 1000));
		}
		if (role.is(R01HLODTripleStoreHostWithRole.READ_ROLE)) {
			outWLSParams.put("PathTrim",READ_TRIPLESTORE_PROXY_SERVLET_PATHTRIM);
		} else if (role.is(R01HLODTripleStoreHostWithRole.WRITE_ROLE)) {
			outWLSParams.put("PathTrim",WRITE_TRIPLESTORE_PROXY_SERVLET_PATHTRIM);
		}
		
		// debug
		if (uriHandlerConfig.getTripleStoreConfig().isDebugProxyEnabled()) _addWLSDebugParams(outWLSParams);

		return outWLSParams;
	}
	public static Map<String,String> proxyWLClusterServletParamsFor(final R01HLODURIHandlerConfig uriHandlerConfig,
																	final Role role) {
		// out properties (see R01HProxyDefForWLSCluster)
		//		- WebLogicCluster = host1:port1|host2:port2
		//		- WebLogicPort = port
		//		- ConnectTimeoutSecs = timeout
		//		- PathTrim = read | write
		Collection<R01HLODTripleStoreHostWithRole> targerServerHostsWithRole = FluentIterable.from(uriHandlerConfig.getTripleStoreConfig()
																							  .getInternalTripleStoreServerHosts())
																					 .filter(R01HLODTripleStoreHostWithRole.matcherFor(role))
																					 .toList();
		if (CollectionUtils.isNullOrEmpty(targerServerHostsWithRole)) throw new IllegalStateException(Strings.customized("There's NO triple-store host with role={} configured!",
																			  					 	  role));
		
		Collection<Host> targerServerHosts = FluentIterable.from(targerServerHostsWithRole)
														   .transform(new Function<R01HLODTripleStoreHostWithRole,Host>() {
																				@Override
																				public Host apply(R01HLODTripleStoreHostWithRole host) {
																					return host.getHost();
																				}
														   			  })
														   .toList();
		StringBuilder hostProxyStr = new StringBuilder();
		for (Iterator<Host> hostIt = targerServerHosts.iterator(); hostIt.hasNext(); ) {
			Host host = hostIt.next();
			hostProxyStr.append(Strings.customized("{}:{}",
												   host.asUrl().getHost().asString(),
												   host.asUrl().getPortOrDefault(80)));		// port 80 by default			
			if (hostIt.hasNext()) hostProxyStr.append("|");
		}
		Map<String,String> outWLSParams = Maps.newHashMapWithExpectedSize(3);
		outWLSParams.put("WebLogicCluster",hostProxyStr.toString());
		if (uriHandlerConfig.getTripleStoreConfig().getProxyTimeout() != null) {
			outWLSParams.put("ConnectTimeoutSecs",Long.toString(uriHandlerConfig.getTripleStoreConfig().getProxyTimeout().asMilis() / 1000));
		}
		if (role.is(R01HLODTripleStoreHostWithRole.READ_ROLE)) {
			outWLSParams.put("PathTrim",READ_TRIPLESTORE_PROXY_SERVLET_PATHTRIM);
		} else if (role.is(R01HLODTripleStoreHostWithRole.WRITE_ROLE)) {
			outWLSParams.put("PathTrim",WRITE_TRIPLESTORE_PROXY_SERVLET_PATHTRIM);
		}
		
		// debug
		if (uriHandlerConfig.getTripleStoreConfig().isDebugProxyEnabled()) _addWLSDebugParams(outWLSParams);
		
		return outWLSParams;
	}
	private static void _addWLSDebugParams(final Map<String,String> outWLSParams) {
		Path tempDir = OSUtils.getOS() == OSType.WINDOWS ? Path.from("d:/temp_dev/r01hp")
														 : Path.from("/tmp/r01hp");
		outWLSParams.put("DEBUG","ALL");	// BEWARE!!! always UPERCASE!!!!
		outWLSParams.put("WLLogFile",tempDir.joinedWith("r01hpWLProxy.log").asAbsoluteString());
//		outWLSParams.put("WLTempDir",tempDir.asAbsoluteString());
		outWLSParams.put("DebugConfigInfo","ON");
		outWLSParams.put("verbose","true");
		log.warn("BEWARE: wl proxy is in DEBUG MODE; log file is at: {}",tempDir.asAbsoluteString());
	}
	public static Map<String,String> proxyServletParamsFor(final R01HLODURIHandlerConfig uriHandlerConfig,
														   final Role role) {
		// out properties (see R01HProxyDefForWLSCluster)
		//		- TargetAppServerHost = host1:port1|host2:port2
		//		- TargetAppServerPort = port
		//		- ConnectTimeoutSecs = timeout
		//		- PathTrim = read | write
		R01HLODTripleStoreHostWithRole targerServerHostWithRole = FluentIterable.from(uriHandlerConfig.getTripleStoreConfig()
																							  .getInternalTripleStoreServerHosts())
																		 .firstMatch(R01HLODTripleStoreHostWithRole.matcherFor(role))
																		 .orNull();
		if (targerServerHostWithRole == null) throw new IllegalStateException(Strings.customized("There's NO triple-store host with role={} configured!",
																			  					 role));
		Host targerServerHost = targerServerHostWithRole.getHost();
		
		Map<String,String> outProxyParams = new HashMap<String,String>();
		outProxyParams.put(HttpProxyServletConfig.INIT_PARAM_NAME_FOR_TARGET_APP_SERVER_HOST_NAME, 
							targerServerHost.getId());
		outProxyParams.put(HttpProxyServletConfig.INIT_PARAM_NAME_FOR_TARGET_APP_SERVER_HOST_PORT,
						   Integer.toString(targerServerHost.asUrl().getPort()));
		
		if (role.is(R01HLODTripleStoreHostWithRole.READ_ROLE)) {
			outProxyParams.put(HttpProxyServletConfig.INIT_PARAM_NAME_FOR_PATH_TRIM,
							   READ_TRIPLESTORE_PROXY_SERVLET_PATHTRIM);
		} else if (role.is(R01HLODTripleStoreHostWithRole.WRITE_ROLE)) {
			outProxyParams.put(HttpProxyServletConfig.INIT_PARAM_NAME_FOR_PATH_TRIM,
							   WRITE_TRIPLESTORE_PROXY_SERVLET_PATHTRIM);
		}
		// allow big post data
		outProxyParams.put(HttpProxyServletConfig.INIT_PARAM_NAME_FOR_MAX_FILE_UPLOAD_SIZE,
						   Integer.toString(Integer.MAX_VALUE));
		return outProxyParams;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	private static final String READ_TRIPLESTORE_PROXY_SERVLET_PATHTRIM =  "/" + R01HLODURIHandlerConfig.LOD_WAR_NAME + "/read";
	private static final String WRITE_TRIPLESTORE_PROXY_SERVLET_PATHTRIM =  "/" + R01HLODURIHandlerConfig.LOD_WAR_NAME + "/write";
}
