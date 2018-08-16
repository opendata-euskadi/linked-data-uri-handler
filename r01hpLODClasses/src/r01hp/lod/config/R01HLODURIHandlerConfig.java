package r01hp.lod.config;

import java.util.Collection;

import org.w3c.dom.Node;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.config.ContainsConfigData;
import r01f.debug.Debuggable;
import r01f.guids.CommonOIDs.Role;
import r01f.types.Path;
import r01f.types.TimeLapse;
import r01f.types.url.Host;
import r01f.types.url.UrlPath;
import r01f.util.types.Strings;
import r01f.xmlproperties.XMLPropertiesForAppComponent;

@Accessors(prefix="_")
@RequiredArgsConstructor
public class R01HLODURIHandlerConfig 	
  implements ContainsConfigData, 
  			 Debuggable {
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTANTS
/////////////////////////////////////////////////////////////////////////////////////////
	public static final String LOD_WAR_NAME = "r01hpLODWar";
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter private final Host _idSite;
	@Getter private final Host _dataSite;
	@Getter private final Host _apiSite;
	@Getter private final Host _docSite;
	@Getter private final Host _webSite;

	
	@Getter private final R01HLODAppServerConfig _appServerConfig;
	@Getter private final boolean _useMockIsMainEntityOfPageResolver;
	@Getter private final R01HLODEldaConfig _eldaConfig;
	@Getter private final R01HLODTripleStoreConfig _tripleStoreConfig;

/////////////////////////////////////////////////////////////////////////////////////////
//	BUILDER METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	public static R01HLODURIHandlerConfig loadFrom(final XMLPropertiesForAppComponent xmlProps) {
		// sites
		Host idSite = xmlProps.propertyAt("/lod/sites/idSite")
							  .asHost(Host.of("http://id.euskadi.eus"));
		Host dataSite = xmlProps.propertyAt("/lod/sites/dataSite")
							    .asHost(Host.of("http://data.euskadi.eus"));
		Host apiSite = xmlProps.propertyAt("/lod/sites/apiSite")
							    .asHost(Host.of("http://api.euskadi.eus"));
		Host docSite = xmlProps.propertyAt("/lod/sites/docSite")
							    .asHost(Host.of("http://doc.euskadi.eus"));
		Host webSite = xmlProps.propertyAt("/lod/sites/docSite")
							   .asHost(Host.of("http://web.euskadi.eus"));
		
		Collection<Host> appServerHosts = xmlProps.propertyAt("/lod/appServer/hosts")
								  				  .asObjectList(new Function<Node,Host>() {
																		public Host apply(final Node node) {
																			return Host.from(node.getTextContent());
																		}
								  				  				},
								  						  		Lists.newArrayList(Host.of("localhost")));
		boolean useMockIsMainEntityOfPageResolver = xmlProps.propertyAt("/lod/uriHandler/useMockIsMainEntityOfPageResolver/")
															.asBoolean(false);
		R01HLODEldaConfig eldaCfg = _loadEldaConfigFrom(xmlProps);
		R01HLODTripleStoreConfig tripleStoreCfg = _loadTripleStoreConfigFrom(xmlProps);
		return new R01HLODURIHandlerConfig(idSite,dataSite,apiSite,docSite,webSite,
										   new R01HLODAppServerConfig(appServerHosts),
										   useMockIsMainEntityOfPageResolver,
										   eldaCfg,
									  	   tripleStoreCfg);
	}
	private static R01HLODEldaConfig _loadEldaConfigFrom(final XMLPropertiesForAppComponent xmlProps) {
		Path eldaConfigRootPath = xmlProps.propertyAt("/lod/elda/configRootPath")
												.asPath();
		return new R01HLODEldaConfig(eldaConfigRootPath); 
	}
	private static R01HLODTripleStoreConfig _loadTripleStoreConfigFrom(final XMLPropertiesForAppComponent xmlProps) {
		// use generic http proxy
		boolean useGenericHttpProxy = xmlProps.propertyAt("/lod/triplestore/useGenericHttpProxy/")
								 						 .asBoolean(true);
		// proxy timeout
		TimeLapse proxyTimeout = xmlProps.propertyAt("/lod/triplestore/proxyConnectTimeout/")
										 .asTimeLapse("2s");	// 2 seecond by default
		
		// debug proxy
		boolean debugProxyEnabled = xmlProps.propertyAt("/lod/triplestore/proxyDebugEnabled")
											.asBoolean(false);
		
		// end point hosts
		Collection<R01HLODTripleStoreHostWithRole> endPointHosts = FluentIterable.from(xmlProps.propertyAt("/lod/triplestore/endPoint/hosts/")
														  			 						   .nodeListIterable())
																	   .transform(new Function<Node,R01HLODTripleStoreHostWithRole>() {
																							@Override
																							public R01HLODTripleStoreHostWithRole apply(final Node node) {
																								String hostStr = node.getTextContent();
																								String roleStr = node.getAttributes() != null
																														? node.getAttributes().getNamedItem("role") != null 
																																? node.getAttributes().getNamedItem("role").getNodeValue()
																																: null
																														: null;
																								Host host = Strings.isNOTNullOrEmpty(hostStr) ? Host.of(hostStr)
																																			  : null;
																								Role role = Strings.isNOTNullOrEmpty(roleStr) ? Role.forId(roleStr)
																																			  : null;
																								if (role != null
																								 && role.isNOTContainedIn(R01HLODTripleStoreHostWithRole.READ_ROLE,
																														  R01HLODTripleStoreHostWithRole.WRITE_ROLE)) {
																									throw new IllegalStateException(roleStr + " is NOT a valid role: check the triple-store config!"); 
																								}
																								if (host == null) throw new IllegalStateException("Invalid triple-store host in config!!");
																								return new R01HLODTripleStoreHostWithRole(host,role);
																							}
																				   })
																	   .toList();
		// end point url path pattern
		UrlPath urlPath = xmlProps.propertyAt("/lod/triplestore/endPoint/sparqlUrlPath/")
								 .asUrlPath(UrlPath.from("/blazegraph/namespace/euskadi_db/sparql"));
		
		return new R01HLODTripleStoreConfig(useGenericHttpProxy,
										    proxyTimeout,
										    debugProxyEnabled,
											endPointHosts,
											urlPath);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	DEBUG
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public CharSequence debugInfo() {
		return Strings.customized("\t-site: id={} data={} api={} doc={} web={}\n" +
								  "\t-                          app server: {}\n" +
								  "\t-use mock isMainEntityOfPage resolver: {}\n" +
								  "\t-                         Elda config: {}\n" +
								  "\t-                  TripleStore config: {}",
								  _idSite,_dataSite,_apiSite,_docSite,_webSite,
								  _appServerConfig != null ? _appServerConfig.debugInfo() : "",
								  _useMockIsMainEntityOfPageResolver,
								  _eldaConfig != null ? _eldaConfig.debugInfo() : "",
								  _tripleStoreConfig != null ? _tripleStoreConfig.debugInfo() : "");
	}
	
}
