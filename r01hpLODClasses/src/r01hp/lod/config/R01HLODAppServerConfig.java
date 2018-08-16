package r01hp.lod.config;

import java.util.Collection;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.config.ContainsConfigData;
import r01f.debug.Debuggable;
import r01f.types.url.Host;
import r01f.types.url.UrlPath;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

@Accessors(prefix="_")
@RequiredArgsConstructor
public class R01HLODAppServerConfig 	
  implements ContainsConfigData,
  			 Debuggable {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter private final Collection<Host> _appServerHosts;
	@Getter private UrlPath _lodWarUrlPath = UrlPath.from(R01HLODURIHandlerConfig.LOD_WAR_NAME);
/////////////////////////////////////////////////////////////////////////////////////////
//	DEBUG
/////////////////////////////////////////////////////////////////////////////////////////
	public CharSequence debugInfo() {
		return Strings.customized("Hosts: {}",
								  CollectionUtils.hasData(_appServerHosts) ? _appServerHosts : "");
	}
}
