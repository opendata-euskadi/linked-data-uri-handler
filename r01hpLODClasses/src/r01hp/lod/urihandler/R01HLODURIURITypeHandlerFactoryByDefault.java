package r01hp.lod.urihandler;

import r01hp.lod.config.R01HLODURIHandlerConfig;
import r01hp.lod.urihandler.handlers.R01HLODURIHandler;
import r01hp.lod.urihandler.handlers.R01HLODURIHandlerForDataSet;
import r01hp.lod.urihandler.handlers.R01HLODURIHandlerForDataSetDistribution;
import r01hp.lod.urihandler.handlers.R01HLODURIHandlerForELI;
import r01hp.lod.urihandler.handlers.R01HLODURIHandlerForID;
import r01hp.lod.urihandler.handlers.R01HLODURIHandlerForOntologyDefinition;
import r01hp.lod.urihandler.handlers.R01HLODURIHandlerForSPARQLQuery;
import r01hp.lod.urihandler.handlers.R01HLODURIHandlerForTripleStoreData;
import r01hp.lod.urihandler.handlers.R01HLODURIHandlerForTripleStoreDataAsDoc;

public class R01HLODURIURITypeHandlerFactoryByDefault
     extends R01HLODURIURITypeHandlerFactoryBase {
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////	
	public R01HLODURIURITypeHandlerFactoryByDefault(final R01HLODURIHandlerConfig config) {
		super(config);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public R01HLODURIHandler from(final R01HLODURIType type) {
		switch(type) {
		// resources (note that some resource types lake ELI or data-sets has their own 
		case ID:
			return new R01HLODURIHandlerForID(_config);
		// ELI
		case ELI:
			return new R01HLODURIHandlerForELI(_config);
		// data-set related entities: data-set entity & data-set distribution
		case DATASET:
			return new R01HLODURIHandlerForDataSet(_config);
		// data set distribution
		case DISTRIBUTION:
			return new R01HLODURIHandlerForDataSetDistribution(_config);
		// triple-store 
		case DATA:
			return new R01HLODURIHandlerForTripleStoreData(_config);
		// elda
		case DOC:
			return new R01HLODURIHandlerForTripleStoreDataAsDoc(_config);
		// sparql query
		case SPARQL:
			return new R01HLODURIHandlerForSPARQLQuery(_config);
		// Skos
		// ontology definition (.owl)
		case ONTOLOGY_DEF:
			return new R01HLODURIHandlerForOntologyDefinition(_config);
		case SKOS:
			return null;
		default:
			throw new IllegalArgumentException(type + " is not a valid LOD URI type!");
		}
	}
}
