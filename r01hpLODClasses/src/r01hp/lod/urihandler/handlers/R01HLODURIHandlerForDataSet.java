package r01hp.lod.urihandler.handlers;

import r01hp.lod.config.R01HLODURIHandlerConfig;
import r01hp.lod.urihandler.R01HLODMainEntityOfPageResolver;

/**
 * Handles uris Dataset in a DCAT file: <pre>http://data.euskadi.eus/dataset/{NamedGraph}</pre> 
 * (lang is optional)
 * <pre>
 *             ^
 *             |                                                      +-------------------+
 *             |                                                      | /dataset/{graph}  |
 *         Resource                                                   +--------+----------+
 *           URIs                                                              |
 *             |                     Is main entity                            |
 *             |                     of page?------------MIME=HTML-------------+-------MIME=RDF------------+
 *             |                          |                                                                |
 *             |            +-----NO------+---------YES------+                                             |
 *             |            |                                |                                             |
 *             |            |                                |                                             |
 *             |            |                                |                                             |
 *         +---------------------------------------[***** CLIENT REDIR ****]--------------------------------------------+
 *             |            |                                |                                             |
 *             |   +--------v-------------+      +-----------v-------------+                   +---------- v-----------+
 *             |   | /doc/dataset/{graph} |      | {opendata dataset page} |                   | /data/dataset/{graph} |
 *             |   +--------+-------------+      +-----------+-------------+                   +---------- +-----------+
 *                          |                                |                                             |
 *       Representation     |                                |                                   ¿ /dataset/{graph} ?
 *           URLs           |                                |                                             |
 *             |   +--------v---------+            +---------v--------+                           +--------v--------+
 *             |   |                  |            |                  |                           |                 |
 *             |   |      ELDA        |            |        Web       |                           |   Triple-Store  |
 *             |   |                  |            |                  |                           |                 |
 *             v   +------------------+            +------------------+                           +---------^-------+
 *                          |                                                                               |
 *                          +-------------------------¿ /dataset/{graph} ?----------------------------------+
 *         
 * </pre>
 */
public class R01HLODURIHandlerForDataSet 
     extends R01HLODURIHandlerForHasWebRepresentationURIBase  {
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public R01HLODURIHandlerForDataSet(final R01HLODURIHandlerConfig config) {
		super(config);
	}
	public R01HLODURIHandlerForDataSet(final R01HLODURIHandlerConfig config,
									   final R01HLODMainEntityOfPageResolver isMainEntityOfPageResolver) {
		super(config,
			  isMainEntityOfPageResolver);
	}	
}
