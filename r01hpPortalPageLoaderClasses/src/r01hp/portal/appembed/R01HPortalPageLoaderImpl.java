package r01hp.portal.appembed;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.enums.EnumWithCode;
import r01f.enums.EnumWithCodeWrapper;
import r01f.xmlproperties.XMLPropertiesForAppComponent;

@Accessors(prefix="_")
@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
public enum R01HPortalPageLoaderImpl 
 implements EnumWithCode<String,R01HPortalPageLoaderImpl> {
	FILE_SYSTEM("fileSystem"),
	REST_SERVICE("restService");
	
	@Getter private final String _code;
	@Getter private final Class<String> _codeType = String.class;
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////	
	private static final EnumWithCodeWrapper<String,R01HPortalPageLoaderImpl> DELEGATE = EnumWithCodeWrapper.wrapEnumWithCode(R01HPortalPageLoaderImpl.class);

	@Override
	public boolean isIn(final R01HPortalPageLoaderImpl... els) {
		return DELEGATE.isIn(this,els);
	}
	@Override
	public boolean is(final R01HPortalPageLoaderImpl el) {
		return DELEGATE.is(this,el);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	public static R01HPortalPageLoaderImpl configuredAt(final XMLPropertiesForAppComponent props) {
		return props.propertyAt("portalpageappembedfilter/portalServer/portalFiles/@loaderImpl")
					.asEnumElement(R01HPortalPageLoaderImpl.class,
								   R01HPortalPageLoaderImpl.FILE_SYSTEM);	// default val
	}
}
