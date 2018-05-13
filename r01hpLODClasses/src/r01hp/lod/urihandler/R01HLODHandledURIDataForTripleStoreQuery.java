package r01hp.lod.urihandler;

import lombok.Getter;
import lombok.experimental.Accessors;
import r01f.locale.Language;
import r01f.util.types.Strings;

@Accessors(prefix="_")
public class R01HLODHandledURIDataForTripleStoreQuery
	 extends R01HLODHandledURIDataBase {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter private final Language _lang;
	@Getter private final R01HLODTripleStoreQuery _tripleStoreQuery;
	@Getter protected final R01HMIMEType _mimeType;
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public R01HLODHandledURIDataForTripleStoreQuery(final Language lang,
													final R01HLODTripleStoreQuery query) {
		this(lang,
			 query,
			 R01HMIMEType.RDFXML);
	}
	public R01HLODHandledURIDataForTripleStoreQuery(final Language lang,
													final R01HLODTripleStoreQuery query,
													final R01HMIMEType mimeType) {
		super(R01HLODURIHandleAction.TRIPLE_STORE_QUERY);
		_lang = lang;
		_tripleStoreQuery = query;
		_mimeType = mimeType;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	DEBUG
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public CharSequence debugInfo() {
		return Strings.customized("TripleStore query={} for db={} (mime type={})",
								  _tripleStoreQuery,_lang,_mimeType);
	}
}
