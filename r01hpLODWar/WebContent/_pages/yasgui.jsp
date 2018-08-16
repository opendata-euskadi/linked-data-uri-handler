<%@page import="com.google.inject.Injector"%>
<%@page import="r01hp.lod.config.R01HLODURIHandlerConfig"%>
<%@page import="r01hp.lod.config.R01HLODTripleStoreConfig"%>
<%@page import="r01hp.lod.urihandler.R01HLODResourceType"%>
<%@page import="r01f.locale.Language"%>
<%@page import="r01f.types.url.Url"%>
<%@page import="r01f.types.url.UrlPath"%>
<%
/////////////////////////////////////////////////////////////////////////////////////////
//	SPARQL ENDPOINT URL
//	Beware that there're TWO sparql endpoint urls:
//		- The public one
//		- The internal one
//  Here the PUBLIC sparql url is used since it's a CLIENT-SIDE (pure-javascript) integration
//
//                                          site/sparql
//                                               +
//                                               |
//                                         +-----v------+
//                                         |   Public   |
//                                         |     Web    |
//                                         +------------+
//                                               |
//                                               |
//                                        +------v-------+
//                                        |  LOD Proxy   |
//                                        |              |
//                                        +--------------+
//                                               |
//                                               v
//                        triple-store-server:port/blazegraph/namespaces/db/sparql
//                                       +-----------------+
//                                       |   Triple-Store  |
//                                       |   (BlazeGraph)  |
//                                       |                 |
//                                       +-----------------+
/////////////////////////////////////////////////////////////////////////////////////////

    // Get the injector
	Injector injector = (Injector)pageContext.getServletContext()
    									     .getAttribute(Injector.class.getName());
	// Get the uri handler config
	R01HLODURIHandlerConfig uriHandlerConfig = injector.getInstance(R01HLODURIHandlerConfig.class);
	
	// now get the INTERNAL sparql endpoint url 
	// BEWARE!!! do NOT make a direct call to a blazegraph instance,
	//			 use the proxy in order to have load balance and redundancy
    //
    //                   +----------------+
    //                   |     LODWAR     |
    //                   |            |   +-------------+
    //                   +------------|---+             |
    //                    |    proxy  v  |        DO NOT DO THIS
    //                    +--------------+              |
    //                        |     |                   |
    //           +------------+     +--------------+    |
    //           |                                 |    |
    //   +-----------------+              +-----------------+
    //   |  TripleStore    |              |  TripleStore    |
    //   +-----------------+              +-----------------+
	//
	// direct call to a blazegraph instance (DO NOT DO THIS!!!)
	// Url internalSparqlEndPointUrl = uriHandlerConfig.getTripleStoreConfig()
	//												.getInternalSPARQLEndPointUrl();
	// ... do this instead
	
	// BEWARE!!!! The url of the SPARQL endpoint needed here is the PUBLIC one
	//					http://{lang}.domain/sparql
	//			  The SPARQL endpoint url abobe is the INTERNAL one (the one at the BLAZEGRAPH server)
	//					http://triple-store-host:9999/sparql/namespaces/{db}/sparql
	String requestURI = request.getRequestURI();
	UrlPath publicSparqlEndPointUrlPath = UrlPath.from(R01HLODResourceType.SPARQL.getPathToken(),
													   "execute");	
	
%>

<!DOCTYPE html>
<html>
<head>
	<meta charset="utf-8">
	<meta http-equiv="X-UA-Compatible" content="IE=edge">
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<link href='http://cdn.jsdelivr.net/yasqe/2.11.10/yasqe.min.css' rel='stylesheet' type='text/css'></link>
	<link href='http://cdn.jsdelivr.net/yasr/2.10.8/yasr.min.css' rel='stylesheet' type='text/css'></link>
	<title>Open Data Euskadi</title>
</head>
<body>
	<div id='yasqe'></div>
  	<div id="yasr"></div>
  	
	<script src="http://cdnjs.cloudflare.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script>
 	<script src='http://cdn.jsdelivr.net/yasqe/2.2/yasqe.bundled.min.js'></script>
  	<script src='http://cdn.jsdelivr.net/yasr/2.4/yasr.bundled.min.js'></script>
	<script type="text/javascript">
  	$.ajaxSetup({
			type		: "POST",
			cache 		: false,
			contentType	: 'application/x-www-form-urlencoded; charset=UTF-8',
			beforeSend 	: function(xhr) {
								// enable CORS
								xhr.setRequestHeader("Access-Control-Allow-Origin","*");
								xhr.setRequestHeader("X-Requested-With","XMLHttpRequest");
	    				  },
	    	crossDomain	: false
	});
	var yasqe = YASQE(document.getElementById("yasqe"),
					  {
						backdrop	: true,
						persistent	: null,
						sparql		: {
											endpoint: "<%=publicSparqlEndPointUrlPath.asAbsoluteString()%>",	// the PUBLIC sparql endpoint url
											showQueryButton: true
									  }
					  });
	var yasr = YASR(document.getElementById("yasr"), 
					{
						// this way, the URLs in the results are prettified using the defined prefixes in the query
						getUsedPrefixes: yasqe.getPrefixesFromQuery
					});
	// link both together
	yasqe.options.sparql.handlers.success =  function(data,status,response) {
												yasr.setResponse({
																	response	: data,
																	contentType	: response.getResponseHeader("Content-Type")
																 });
											 };
	yasqe.options.sparql.handlers.error = function(xhr,textStatus,errorThrown) {
												yasr.setResponse({
																	exception: textStatus + ": " + errorThrown
																 });
										  };
	yasqe.options.sparql.callbacks.complete = yasr.setResponse;
	yasr.options.getUsedPrefixes = yasqe.getPrefixesFromQuery;
	</script>
</body>
</html>
