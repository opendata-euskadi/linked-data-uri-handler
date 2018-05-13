<%@page import="com.google.inject.Injector"%>
<%@page import="r01hp.lod.config.R01HLODURIHandlerConfig"%>
<%@page import="r01hp.lod.config.R01HLODTripleStoreConfig"%>
<%@page import="r01hp.lod.urihandler.R01HLODURIType"%>
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
	UrlPath publicSparqlEndPointUrlPath = UrlPath.from(R01HLODURIType.SPARQL.getPathToken(),
													   "execute");	
	
%>

<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>OpenData Euskadi</title>
    <link href='http://cdn.jsdelivr.net/yasgui/2.5.0/yasgui.min.css' rel='stylesheet' type='text/css'/>
    <style>
      /** uncomment this if you'd like to hide the endpoint selector
      .yasgui .endpointText {display:none !important;}
      **/
    </style>
  </head>
  <body>
	  <div id='yasgui'></div>
    <script src='http://cdn.jsdelivr.net/yasgui/2.5.0/yasgui.min.js'></script>
    <script type="text/javascript">

	    var yasgui = YASGUI(document.getElementById("yasgui"), 
	    											{
												        yasqe:{
												        			sparql:	{
												        						endpoint:"<%=publicSparqlEndPointUrlPath.asAbsoluteString()%>"
												        					}
													    	  }
      												});
    </script>
  </body>
</html>
