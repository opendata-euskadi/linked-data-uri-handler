package r01hp.lod.urihandler;

import lombok.extern.slf4j.Slf4j;
import r01f.patterns.FactoryFrom;
import r01hp.lod.config.R01HLODURIHandlerConfig;
import r01hp.lod.urihandler.handlers.R01HLODURIHandler;

/**
 * Handles URIs
 * There're diferent type of URIs:
 * 
 * [1] - Resource identifiers: /id/{resource}
 *       =====================================================================
 *       All resources/entities are identified with an ID URI like /id/{resource} (the URI that identifies the triple's OBJECT)
 *       If an ID URI is requested to the web server it's handled differently whether the requested MIME is HTML or RDF
 *       		- If the requested MIME is HTML, the URI can belong to an entity that has an associated web page so query to the [triple-store] is issued just to check 
 *       		  the main-entity-of-page attribute
 *       				- if main-entity-of-page = true, it's an entity that has an associated web page so a CLIENT REDIR to /page/{resource} is issued
 *       				- if main-entity-of-page = false, it's an entity that DOES NOT have an associated web page so the [triple-store] RDF is "painted" in HTML format by ELDA
 *       				  so a CLIENT REDIR to /doc/{resource} is issued
 *       
 * 	     	- If the requested MIME is RDF, the URI is for a [triple-store] entity so it MUST go directly to the [triple-store] and this is also done
 *       		  issuing another CLIENT REDIR to /data/{resource}
 *       
 *       All the above also means that:
 *       		- All /doc/{resource} calls MUST have a MIME=HTML (although ELDA supports other MIMES)
 *       		- All /data/{resource} calls MUST hava a MIME=RDF
 *       <pre>
 *                   ^
 *                   |                                                      +------------------+
 *                   |                                                      |  /id/{resource}  |
 *               Resource                                                   +--------+---------+
 *                 URIs                                                              |
 *                   |                     Is main entity                            |
 *                   |                     of page?------------MIME=HTML-------------+-------MIME=RDF---------+
 *                   |                          |                                                             |
 *                   |            +-----NO------+------YES-----+                                              |
 *                   |            |                            |                                              |
 *                   |            |                            |                                              |
 *                   |            |                            |                                              |
 *               +---------------------------------------[***** CLIENT REDIR ****]----------------------------------------+
 *                   |            |                            |                                              |
 *                   |   +--------v--------+         +---------v--------+                            +--------v--------+
 *                   |   | /doc/{resource} |         | /page/{resource} |                            | /data/{resource}|
 *                   |   +--------+--------+         +---------+--------+                            +--------+--------+
 *                                |                            |                                              |
 *             Representation     |                            |                                              |
 *                 URLs           |                            |                                              |
 *                   |   +--------v---------+        +---------v--------+                            +--------v--------+
 *                   |   |                  |        |                  |                            |                 |
 *                   |   |      ELDA        |        |        Web       |                            |   Triple-Store  |
 *                   |   |                  |        |                  |                            |                 |
 *                   v   +------------------+        +------------------+                            +-----------------+
 *             
 *       </pre> 
 *       
 * [2] - Browse [triple-store] data using an HTML interface: /doc/{resource}
 *       =====================================================================================
 *       A /data/{resource} MUST be requested with MIME=HTML
 *       [ELDA] is used to browse the [triple-store] data (the triples) using an HTML UI
 *       All requests like /doc/{resource} with MIME=HTML are handled to [ELDA] which issues DESCRIBE SPARQL queries 
 *       to the [triple store]
 *       		
 *       		/doc/{resource} (MIME=RDF) results in a /sparql?query=DESCRIBE </id/{resource}> to the [triple-store]
 *       		that is pretty-printed by ELDA
 *       
 *        		Bear in mind that [triple]'s OBJECTs (entities / resources) are identified as /id/{resource}
 *        		... so ELDA translates que incoming URL /doc/{resource} to /id/{resource} 
 *                  (see /config/r01hp/elda/r01hp.elda.euskadi_es.config to guess how this translation works)
 *        
 * [3] - Data: /data/{resource}
 *       ======================================================================================
 *       A /data/{resource} MUST be requested with MIME=RDF
 *       These kind of request are just handled to the [triplestore] as DESCRIBE SPARQL queries
 *       
 *       	/data/{resource} (MIME=RDF) is translated to /sparql?query=DESCRIBE </id/{resource}>
 *          that is just returned to the client
 *          
 *          Bear in mind that [triple]'s OBJECTs (entities / resources) are identified as /id/{resource}
 *          ... so ELDA translates que incoming URL /doc/{resource} to /id/{resource} 
 *              (see /config/r01hp/elda/r01hp.elda.euskadi_es.config to guess how this translation works)
 *          
 * [4] - SPARQL queries: /sparql?query={the_query}
 *       ======================================================================================
 *       SPARQL queries are handled directly to the [SPARQL endpoint] of the [triple-store] or directed to a [SPARQL-GUI] (YASGUI)
 *       depending on the MIME-TYPE
 *       	- If MIME-TYPE=RDF the query is directed to the [SPARQL endpoint] of the [triple-store]
 *          - If MIME-TYPE=HTML the query is directed to the [SPARQL GUI] (YASGUI) 
 *            which in-turn issues /sparql?query={the query} with MIME-TYPE=RDF that's handled to the [triple-store]
 */
@Slf4j
public class R01HLODURIHandlerEngine {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	private final R01HLODURIHandlerConfig _config;
	private final FactoryFrom<R01HLODURIType,R01HLODURIHandler> _handlerFactory;
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////	
	public R01HLODURIHandlerEngine(final R01HLODURIHandlerConfig config,
							       final FactoryFrom<R01HLODURIType,R01HLODURIHandler> handlerFactory) {
		_config = config;
		_handlerFactory = handlerFactory;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Just handles the request data
	 * @param data
	 * @return
	 */
	public R01HLODHandledURIData handle(final R01HLODRequestedURIData data) {
		log.info("Request to be handled: {}",
				 data.debugInfo());
		
		R01HLODHandledURIData outHandleData = _handlerFactory.from(data.getUriType())
															 .handle(data);
		log.info("Handle info: {}",
				 outHandleData.debugInfo());
		return outHandleData;
	}

}
