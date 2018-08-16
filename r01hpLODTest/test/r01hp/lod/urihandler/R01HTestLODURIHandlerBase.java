package r01hp.lod.urihandler;

import java.util.ArrayList;
import java.util.Collection;

import com.google.common.collect.Lists;

import r01f.types.Path;
import r01f.types.TimeLapse;
import r01f.types.url.Host;
import r01f.types.url.UrlPath;
import r01hp.lod.config.R01HLODAppServerConfig;
import r01hp.lod.config.R01HLODEldaConfig;
import r01hp.lod.config.R01HLODTripleStoreConfig;
import r01hp.lod.config.R01HLODTripleStoreHostWithRole;
import r01hp.lod.config.R01HLODURIHandlerConfig;


abstract class R01HTestLODURIHandlerBase {
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	protected R01HLODURIHandlerConfig _loadLODURIHandlerConfig() {
		Collection<R01HLODTripleStoreHostWithRole> tripleStoreHosts =  new ArrayList<R01HLODTripleStoreHostWithRole>();
		tripleStoreHosts.add(new R01HLODTripleStoreHostWithRole(Host.of("http://localhost:9999"),
															    R01HLODTripleStoreHostWithRole.READ_ROLE));
		return new R01HLODURIHandlerConfig(Host.from("id.euskadi.eus"),Host.from("data.euskadi.eus"),Host.from("api.euskadi.eus"),Host.from("doc.euskadi.eus"),Host.from("www.euskadi.eus"),
										   new R01HLODAppServerConfig(Lists.newArrayList(Host.from("localhost:8080"))),
										   true,		// use mock is main entity of page resolver
										   new R01HLODEldaConfig(Path.from("D:/eclipse/projects_platea/r01hp/r01hpConfig/loc_win")),
									  	   new R01HLODTripleStoreConfig(false,				// use generic proxy
									  			  					    TimeLapse.of("1s"),	// proxy timeout
									  			  					    false,				// proxy debug
									  			  					    tripleStoreHosts,
									  			  					    UrlPath.from("/blazegraph/namespace/euskadi_db/sparql")));
	}
}
