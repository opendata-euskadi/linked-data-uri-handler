package r01hp.portal.appembed;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Iterables;

import lombok.extern.slf4j.Slf4j;
import r01f.httpclient.HttpClient;
import r01f.types.Path;
import r01f.types.url.Url;
import r01f.util.enums.Enums;
import r01f.util.types.Strings;
import r01hp.portal.appembed.config.R01HPortalPageLoaderConfigForRESTServiceImpl;
import r01hp.portal.common.R01HPortalOIDs.R01HPortalID;
import r01hp.portal.common.R01HPortalOIDs.R01HPortalPageID;
import r01hp.portal.common.R01HPortalPageCopy;

@Slf4j
public class R01HPortalPageLoaderRESTServiceImpl 
     extends R01HPortalPageLoaderBase {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
    private R01HPortalPageLoaderConfigForRESTServiceImpl _config;
    
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
    public R01HPortalPageLoaderRESTServiceImpl(final R01HPortalPageLoaderConfigForRESTServiceImpl cfg) {
    	_config = cfg;
    }
	public R01HPortalPageLoaderRESTServiceImpl(final Collection<Url> pageLoaderRestEndPointUrls) {
		_config = new R01HPortalPageLoaderConfigForRESTServiceImpl(pageLoaderRestEndPointUrls);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	R01HPortalPageLoader
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public R01HLoadedContainerPortalPage loadWorkCopyFor(final R01HPortalID portalId,final R01HPortalPageID pageId) throws IOException {
		return _loadFor(portalId,pageId,
						R01HPortalPageCopy.WORK);
	}
	@Override
	public R01HLoadedContainerPortalPage loadLiveCopyFor(final R01HPortalID portalId,final R01HPortalPageID pageId) throws IOException {
		return _loadFor(portalId,pageId,
						R01HPortalPageCopy.LIVE);		
	}
	private R01HLoadedContainerPortalPage _loadFor(final R01HPortalID portalId,final R01HPortalPageID pageId,
												   final R01HPortalPageCopy copy) throws IOException {
		// [1] - Compose the endpoint url
		// TODO use netflix's ribbon to balance between multiple rest-service endpoint urls
		String urlTemplate = "{}/r01hpPortalPageProviderRESTServiceWar/portals/{}/pages/{}/{}";
		Url  endPointUrl = Url.from(Strings.customized(urlTemplate,
													   Iterables.<Url>getFirst(_config.getRestServiceEndPointUrls(),
															   			  		Url.from("http://localhost")),				// default value
													   portalId,pageId,
													   copy.is(R01HPortalPageCopy.WORK) ? "workingcopy"
															   							: "livecopy"));	
		log.info("Endpoint url={}",endPointUrl);
		// [2] - Retrieve
    	InputStream is = null;
    	long lastModifiedTimeStamp = 0;
    	Path pagePath = null;
    	try {
    		is = HttpClient.forUrl(Url.from(endPointUrl))
						   .GET()
						   .loadAsStream()
						   .directNoAuthConnectedWithTimeout(1000);
	        log.info("... loaded app container page {}-{} from {}",
	        		 portalId,pageId,
	        		 endPointUrl);
    	} catch (Throwable th) {
    		th.printStackTrace(System.out);
    		log.error("Error loading an app container page {}-{} file from {}: {}",
    				  portalId,pageId,
    				  endPointUrl,
    				  th.getMessage(),
    				  th);    		
    	} 
    	// [3] - Get the header
    	if (is != null) {
    		ByteArrayOutputStream bos = new ByteArrayOutputStream(180);
    		int b = is.read();
    		while (((char)b) != '\n'
    			  && b != -1) {
    			bos.write(b);
    			b = is.read();
    		}
    		String header = new String(bos.toByteArray());
    		log.info("Portal page header returned by the REST service: {}",
    				  header);
    		
    		// <!-- web01-container (WORK) [1524550504623] d:/temp_dev/r01hp/web01/html/pages/portal/web01-container.shtml -->
    		Pattern HEADER_PATTERN = Pattern.compile("<!-- ([^-]+)-([^ ]+) \\(([^)]+)\\) \\[([^]]+)\\] ([^ ]+) -->");
    		Matcher m = HEADER_PATTERN.matcher(header);
    		if (m.find()) {
    			R01HPortalID receivedPortalId = R01HPortalID.forId(m.group(1));
    			R01HPortalPageID receivedPortalPageId = R01HPortalPageID.forId(m.group(2));
    			R01HPortalPageCopy receivedCopy = Enums.wrap(R01HPortalPageCopy.class)
    												   .fromName(m.group(3));
    			lastModifiedTimeStamp = Long.parseLong(m.group(4));
    			pagePath = Path.from(m.group(5));
    			
    			// security checks
    			if (receivedPortalId.isNOT(portalId)) {
    				log.error("The received portalId={} is NOT the requested one: {}",
    						  receivedPortalId,portalId);
    				return null;
    			}
    			if (receivedPortalPageId.isNOT(pageId)) {
    				log.error("The received pageId={} is NOT the requested one: {}",
    						  receivedPortalPageId,pageId);
    				return null;    				
    			}
    			if (receivedCopy != copy) {
    				log.error("The received copy={} is NOT the requested one: {}",
    						  receivedCopy,copy);
    				return null;
    			}
    			
    		} else {
    			log.error("Bad portal page header returned by the loader REST service: {}",
    					  header);
    			return null;
    		}
    	}
    	
    	// [4] - Return
    	return new R01HLoadedContainerPortalPage(portalId,pageId,
    											 copy,
    											 lastModifiedTimeStamp,
    											 pagePath,
    											 is); 
	}
}
