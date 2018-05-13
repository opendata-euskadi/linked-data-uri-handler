package r01hp.lod.urihandler;

import r01hp.lod.config.R01HLODURIHandlerConfig;
import r01hp.lod.urihandler.handlers.R01HLODURIHandler;
import r01hp.lod.urihandler.handlers.R01HLODURIHandlerForID;

public class R01HLODURIURITypeHandlerFactoryUsingMainEntityOfPageResolver
     extends R01HLODURIURITypeHandlerFactoryByDefault {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	private final R01HLODMainEntityOfPageResolver _mainEntityOfPageResolver;
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////	
	public R01HLODURIURITypeHandlerFactoryUsingMainEntityOfPageResolver(final R01HLODURIHandlerConfig config,
																			  final R01HLODMainEntityOfPageResolver mainEntityOfPageResolver) {
		super(config);
		_mainEntityOfPageResolver = mainEntityOfPageResolver;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public R01HLODURIHandler from(final R01HLODURIType type) {
		if (type == R01HLODURIType.ID) {
			return new R01HLODURIHandlerForID(_config,
												   _mainEntityOfPageResolver);
		}
		return super.from(type);
	}
}
