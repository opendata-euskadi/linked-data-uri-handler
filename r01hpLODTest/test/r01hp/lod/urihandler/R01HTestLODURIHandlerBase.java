package r01hp.lod.urihandler;

import java.util.ArrayList;
import java.util.Collection;

import r01f.types.Path;
import r01f.types.TimeLapse;
import r01f.types.url.Host;
import r01f.types.url.Url;
import r01hp.lod.config.R01HLODEldaConfig;
import r01hp.lod.config.R01HLODTripleStoreConfig;
import r01hp.lod.config.R01HLODTripleStoreHostWithRole;
import r01hp.lod.config.R01HLODURIHandlerConfig;


abstract class R01HTestLODURIHandlerBase {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	protected static final Host HOST = Host.of("http://data.euskadi.eus");
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	protected R01HLODURIHandlerConfig _loadLODURIHandlerConfig() {
		Collection<R01HLODTripleStoreHostWithRole> tripleStoreHosts =  new ArrayList<R01HLODTripleStoreHostWithRole>();
		tripleStoreHosts.add(new R01HLODTripleStoreHostWithRole(Host.of("http://localhost:9999"),
															    R01HLODTripleStoreHostWithRole.READ_ROLE));
		return new R01HLODURIHandlerConfig(Url.from("data.euskadi.eus"),
										   true,		// use mock is main entity of page resolver
										   new Host("localhost:8080"),
										   new R01HLODEldaConfig(Path.from("D:/eclipse/projects_platea/r01hp/r01hpConfig/loc_win")),
									  	   new R01HLODTripleStoreConfig(false,				// use generic proxy
									  			  					    TimeLapse.of("1s"),	// proxy timeout
									  			  					    false,				// proxy debug
									  			  					    tripleStoreHosts,
									  			  					    "/blazegraph/namespace/{}/sparql"));
	}
}
