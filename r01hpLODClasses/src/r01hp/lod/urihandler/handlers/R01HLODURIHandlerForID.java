package r01hp.lod.urihandler.handlers;

import r01hp.lod.config.R01HLODURIHandlerConfig;
import r01hp.lod.urihandler.R01HLODMainEntityOfPageResolver;

/**
 * Handles uris like: <pre>/id/{resource}</pre>
 * <pre>
 *             ^
 *             |                                                      +------------------+
 *             |                                                      |  /id/{resource}  |
 *         Resource                                                   +--------+---------+
 *           URIs                                                              |
 *             |                     Is main entity                            |
 *             |                     of page?------------MIME=HTML-------------+-------MIME=RDF---------+
 *             |                          |                                                             |
 *             |            +-----NO------+------YES-----+                                              |
 *             |            |                            |                                              |
 *             |            |                            |                                              |
 *             |            |                            |                                              |
 *         +---------------------------------------[***** CLIENT REDIR ****]----------------------------------------+
 *             |            |                            |                                              |
 *             |   +--------v--------+         +---------v---------+                           +--------v--------+
 *             |   | /doc/{resource} |         | {mainEntityOfPage}|                           | /data/{resource}|
 *             |   +--------+--------+         +---------+---------+                           +--------+--------+
 *                          |                            |                                              |
 *       Representation     |                           303                                             |
 *           URLs           |                            |                                              |
 *             |   +--------v---------+        +---------v--------+                            +--------v--------+
 *             |   |                  |        |                  |                            |                 |
 *             |   |      ELDA        |        |        Web       |                            |   Triple-Store  |
 *             |   |                  |        |                  |                            |                 |
 *             v   +------------------+        +------------------+                            +-----------------+
 *       
 * </pre>
 */
public class R01HLODURIHandlerForID 
     extends R01HLODURIHandlerForHasWebRepresentationURIBase  {
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public R01HLODURIHandlerForID(final R01HLODURIHandlerConfig config) {
		super(config);
	}
	public R01HLODURIHandlerForID(final R01HLODURIHandlerConfig config,
								  final R01HLODMainEntityOfPageResolver isMainEntityOfPageResolver) {
		super(config,
			  isMainEntityOfPageResolver);
	}	
}
