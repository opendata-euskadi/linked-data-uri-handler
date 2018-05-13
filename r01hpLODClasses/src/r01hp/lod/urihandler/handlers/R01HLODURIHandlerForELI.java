package r01hp.lod.urihandler.handlers;

import r01hp.lod.config.R01HLODURIHandlerConfig;
import r01hp.lod.urihandler.R01HLODMainEntityOfPageResolver;

/**
 * Handles uris like: <pre>/eli/{resource}</pre>
	 *             ^
	 *             |                                                      +-------------------+
	 *             |                                                      |  /eli/{resource}  |
	 *         Resource                                                   +---------+---------+
	 *           URIs                                                               |
	 *             |                     Is main entity                             |
	 *             |                     of page?------------MIME=HTML--------------+-------MIME=RDF------+
	 *             |                            |                                                         |
	 *             |              +-----NO------+-----YES------+                                          |
	 *             |              |                            |                                          |
	 *             |              |                            |                                          |
	 *             |              |                            |                                          |
	 *         +---------------------------------------[***** CLIENT REDIR ****]--------------------------------------+
	 *             |              |                            |                                          |
	 *             |   +----------v----------+       +---------v----------+                    +----------v-----------+
	 *             |   | /doc/eli/{resource} |       |   /eli/{resource}  |                    | /data/eli/{resource} |
	 *             |   +---------+-----------+       +---------+----------+                    +----------+-----------+
	 *                          |                              |                                          |
	 *       Representation     |                              |                                    ¿ /eli/{resource} ?   
	 *           URLs           |                              |                                          |
	 *             |   +--------v---------+          +---------v--------+                        +--------v--------+
	 *             |   |                  |          |                  |                        |                 |
	 *             |   |      ELDA        |          |        Web       |                        |   Triple-Store  |
	 *             |   |                  |          |                  |                        |                 |
	 *             v   +------------------+          +------------------+                        +--------^--------+
	 *                          |                                                                         |
	 *                          +--------------------------¿ /eli/{resource} ?----------------------------+
	 *       
	 * 
 */
public class R01HLODURIHandlerForELI 
     extends R01HLODURIHandlerForHasWebRepresentationURIBase  {     
	
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////	
	public R01HLODURIHandlerForELI(final R01HLODURIHandlerConfig config) {
		super(config);
	}
	public R01HLODURIHandlerForELI(final R01HLODURIHandlerConfig config,
								   final R01HLODMainEntityOfPageResolver isMainEntityOfPageResolver) {
		super(config,
			  isMainEntityOfPageResolver);
	}
}
