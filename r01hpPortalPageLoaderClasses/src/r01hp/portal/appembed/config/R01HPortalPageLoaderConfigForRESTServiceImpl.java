package r01hp.portal.appembed.config;

import java.util.Collection;

import org.w3c.dom.Node;

import com.google.common.base.Function;

import lombok.Getter;
import lombok.experimental.Accessors;
import r01f.types.url.Url;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;
import r01f.util.types.collections.Lists;
import r01f.xmlproperties.XMLPropertiesForAppComponent;
import r01hp.portal.appembed.R01HPortalPageLoaderImpl;

@Accessors(prefix="_")
public class R01HPortalPageLoaderConfigForRESTServiceImpl 
     extends R01HPortalPageLoaderConfigBase {
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTANTS
/////////////////////////////////////////////////////////////////////////////////////////
	private static final Url DEF_ENDPOINT_URL = Url.from("http://localhost/r01hpPortalPageProviderRESTServiceWar/");
	
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter private final Collection<Url> _restServiceEndPointUrls;
	@Getter private final R01HPortalPageLoaderImpl _impl = R01HPortalPageLoaderImpl.REST_SERVICE;
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public R01HPortalPageLoaderConfigForRESTServiceImpl() {
		this(Lists.newArrayList(DEF_ENDPOINT_URL));
	}
	public R01HPortalPageLoaderConfigForRESTServiceImpl(final Collection<Url> restServiceEndPointUrls) {
		_restServiceEndPointUrls = restServiceEndPointUrls;
	}
	public R01HPortalPageLoaderConfigForRESTServiceImpl(final XMLPropertiesForAppComponent props) {
		this(// endpointUrls
			 props.propertyAt("portalpageappembedfilter/portalServer/portalFiles/restServiceLoader")
				  .asObjectList(new Function<Node,Url>() {
										@Override
										public Url apply(final Node node) {
											return null;
										}
				  				},
						  		// def val
				  				Lists.newArrayList(DEF_ENDPOINT_URL)));
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public R01HPortalPageLoaderConfig cloneOverriddenWith(final R01HPortalPageLoaderConfig other) {
		R01HPortalPageLoaderConfigForRESTServiceImpl otherREST = (R01HPortalPageLoaderConfigForRESTServiceImpl)other;
		
		Collection<Url> restServiceEndPointUrls = CollectionUtils.hasData(otherREST.getRestServiceEndPointUrls()) ? otherREST.getRestServiceEndPointUrls()
																												  : this.getRestServiceEndPointUrls();
		
		return new R01HPortalPageLoaderConfigForRESTServiceImpl(restServiceEndPointUrls);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	DEBUG
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public CharSequence debugInfo() {
		return Strings.customized("\tREST service page loader using : {}",
								  _restServiceEndPointUrls);
	}
}
