package r01hp.lod.urihandler.handlers;

import r01hp.lod.config.R01HLODURIHandlerConfig;

/**
 * Handles uris for a dataset distribution in a DCAT file: <pre>http://data.euskadi.eus/distribution/{NamedGraph}/[lang]/format</pre> 
 * (lang is optional)
 * 
 * <pre>
 *        ^
 *        |
 *        |
 *    Resource                      +------------------------+
 *      URIs                        |      /graph/{graph}    |
 *        |                         +------------+-----------+
 *        |                                      |
 *        |                                      |
 *        |              +----------+MIME=HTML---+---MIME=RDF+--+
 *        |              |                                      |
 *        |              |                                      |
 *        |     +--------------+[*****-CLIENT REDIR ****]+------------+
 *        +              |                                      |
 *                       |                                      |
 * +--------------------------------------------------------------------------+
 *                       |                                      |
 *        +   +----------v------------+            +------------v-------------+
 *        |   |                       |            |                          |
 *        |   |   /doc/graph/{graph}  |            |  /data/graph/{resource}  |
 *        |   |                       |            |                          |
 *        |   +----------+------------+            +------------+-------------+
 * Representation        |                                      |
 *      URLs             |                              ¿ /graph/{graph} ?
 *        |              |                                       |
 *        |    +---------v----------+                  +---------v---------+
 *        |    |                    |                  |                   |
 *        |    |        ELDA        |                  |   TRIPLE-STORE    |
 *        |    |                    |                  |                   |
 *        v    +--------------------+                  +---------^---------+
 *                       |                                       |
 *                       +---------¿ /graph/{graph} ?------------+
 *         
 * </pre>
 */
public class R01HLODURIHandlerForGraph 
     extends R01HLODURIHandlerForHasRepresentationURIBase  {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public R01HLODURIHandlerForGraph(final R01HLODURIHandlerConfig config) {
		super(config);
	}	
}
