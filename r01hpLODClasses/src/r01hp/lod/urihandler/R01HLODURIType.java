package r01hp.lod.urihandler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.enums.EnumExtended;
import r01f.enums.EnumExtendedWrapper;
import r01f.types.url.Host;
import r01f.util.types.collections.Lists;

/**
 * The URI types
 * 		- id  : http://id.site/{resource}
 * 		- data: http://data.site/{resource}
 * 		- doc : http://doc.site/{resource}
 * 		- api : http://api.site/{resource}
 * 		- web : http://www.site/{resource} 
 */
@Accessors(prefix="_")
@RequiredArgsConstructor(access=AccessLevel.PRIVATE) 
public enum R01HLODURIType 
 implements EnumExtended<R01HLODURIType> {
	ID				(Pattern.compile("^id\\..*")),
	DATA			(Pattern.compile("^data\\..*")),
	DOC				(Pattern.compile("^doc\\..*")),
	API				(Pattern.compile("^api\\..*")),
	WEB				(Pattern.compile("^www\\..*")),
	LOCAL			(Pattern.compile("localhost"));
	
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter private final Pattern _hostRegExPattern;

	private static final EnumExtendedWrapper<R01HLODURIType> DELEGATE = EnumExtendedWrapper.wrapEnumExtended(R01HLODURIType.class);
	
/////////////////////////////////////////////////////////////////////////////////////////
//	METHODS
/////////////////////////////////////////////////////////////////////////////////////////	
	public static R01HLODURIType of(final Host host) {
		R01HLODURIType outType = null;
		for (R01HLODURIType type : R01HLODURIType.values()) {
			Matcher m = type.getHostRegExPattern()
							.matcher(host.getId());
			if (m.matches()) {
				outType = type;
				break;
			}
		}
		if (outType == null) throw new IllegalArgumentException("The host '" + host.getId() + "' is NOT one of the handled types: " + Lists.newArrayList(R01HLODURIType.values()));
		return outType;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean isIn(final R01HLODURIType... els) {
		return DELEGATE.isIn(this,els);
	}
	@Override
	public boolean is(final R01HLODURIType el) {
		return DELEGATE.is(this,el);
	}
}