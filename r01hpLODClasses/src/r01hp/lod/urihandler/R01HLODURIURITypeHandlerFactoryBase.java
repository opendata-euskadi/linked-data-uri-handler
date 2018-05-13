package r01hp.lod.urihandler;

import lombok.RequiredArgsConstructor;
import r01f.patterns.FactoryFrom;
import r01hp.lod.config.R01HLODURIHandlerConfig;
import r01hp.lod.urihandler.handlers.R01HLODURIHandler;

@RequiredArgsConstructor
abstract class R01HLODURIURITypeHandlerFactoryBase
    implements FactoryFrom<R01HLODURIType,R01HLODURIHandler> {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	protected final R01HLODURIHandlerConfig _config;
}
