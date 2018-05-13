package r01hp.lod.urihandler.handlers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01hp.lod.config.R01HLODURIHandlerConfig;

@Accessors(prefix="_")
@RequiredArgsConstructor
abstract class R01HLODURIHandlerBase 
	implements R01HLODURIHandler {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS	
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter protected final R01HLODURIHandlerConfig _config;
}
