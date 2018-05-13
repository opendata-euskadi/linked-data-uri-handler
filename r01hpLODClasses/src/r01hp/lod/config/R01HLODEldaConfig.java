package r01hp.lod.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.config.ContainsConfigData;
import r01f.debug.Debuggable;
import r01f.types.Path;
import r01f.util.types.Strings;

@Accessors(prefix="_")
@RequiredArgsConstructor
public class R01HLODEldaConfig 	
  implements ContainsConfigData,
  			 Debuggable {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter private final Path _rootConfigPath;
/////////////////////////////////////////////////////////////////////////////////////////
//	DEBUG
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public CharSequence debugInfo() {
		return Strings.customized("Elda's config root path={}",
								  _rootConfigPath.asAbsoluteString());
	}
}
