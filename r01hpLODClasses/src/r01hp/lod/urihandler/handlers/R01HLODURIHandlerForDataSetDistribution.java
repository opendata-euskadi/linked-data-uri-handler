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
 *      URIs                        |  /distribution/{graph} |
 *        |                         +------------+-----------+
 *        |                                      |
 *        |                                      |
 *        |              +----------+MIME=HTML---+---MIME=RDF+---+
 *        |              |                                       |
 *        |              |                                       |
 *        |     +----------------+[*****-CLIENT REDIR ****]+---------------+
 *        +              |                                       |
 *                       |                                       |
 * +--------------------------------------------------------------------------+
 *                       |                                       |
 *        +   +----------v---------------+        +--------------v----------------+
 *        |   |                          |        |                               |
 *        |   | /doc/distribution/{graph}|        |/data/distribution/{resource}  |
 *        |   |                          |        |                               |
 *        |   +----------+---------------+        +--------------+----------------+
 * Representation        |                                       |
 *      URLs             |                              ¿ /distribution/{graph} ?
 *        |              |                                       |
 *        |    +---------v----------+                  +---------v---------+
 *        |    |                    |                  |                   |
 *        |    |        ELDA        |                  |   TRIPLE-STORE    |
 *        |    |                    |                  |                   |
 *        v    +--------------------+                  +---------^---------+
 *                       |                                       |
 *                       +---------¿ /distribution/{graph} ?-----+
 *         
 * </pre>
 */
public class R01HLODURIHandlerForDataSetDistribution 
     extends R01HLODURIHandlerForHasRepresentationURIBase  {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public R01HLODURIHandlerForDataSetDistribution(final R01HLODURIHandlerConfig config) {
		super(config);
	}
}
