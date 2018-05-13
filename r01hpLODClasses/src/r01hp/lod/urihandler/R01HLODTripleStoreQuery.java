package r01hp.lod.urihandler;

import java.io.Serializable;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import r01f.types.CanBeRepresentedAsString;
import r01f.types.url.Url;
import r01f.util.types.StringEncodeUtils;
import r01f.util.types.Strings;

@Slf4j
@RequiredArgsConstructor
public class R01HLODTripleStoreQuery 
  implements CanBeRepresentedAsString,
  			 Serializable {

	private static final long serialVersionUID = -5106728569855735018L;
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	private final String _query;
/////////////////////////////////////////////////////////////////////////////////////////
//	CanBeRepresentedAsString
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public String asString() {
		try {
			return StringEncodeUtils.urlDecode(_query).toString();
		} catch(Throwable th) {
			log.error("error decoding query string: {}",_query,th);
		}
		return null;
	}
	
	@Override
	public String toString() {
		return this.asString();
	}
	@Override
	public boolean equals(final Object other) {
		if (other == null) return false;
		if (this == other) return true;
		if (!(other instanceof R01HLODTripleStoreQuery)) return false;
		R01HLODTripleStoreQuery otherQry = (R01HLODTripleStoreQuery)other;
		return _query.equals(otherQry.toString());
	}
	@Override
	public int hashCode() {
		return _query != null ? _query.hashCode() : 0;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	BUILDERS
/////////////////////////////////////////////////////////////////////////////////////////
	public static R01HLODTripleStoreQuery describe(final Url uri) {
		return new R01HLODTripleStoreQuery(Strings.customized("DESCRIBE <{}>",
														 	  uri));
	}
	public static R01HLODTripleStoreQuery isMainEntityOfPage(final Url uri) {
		return new R01HLODTripleStoreQuery(Strings.customized("PREFIX schema:<http://schema.org/> " +
						    							 	  "ASK { <{}> schema:mainEntityOfPage ?page }",
						    							 	  uri));
	}
	public static R01HLODTripleStoreQuery mainEntityOfPage(final Url uri) {
		return new R01HLODTripleStoreQuery(Strings.customized("PREFIX schema:<http://schema.org/> " +
						    							 	  "SELECT ?page WHERE { <{}> schema:mainEntityOfPage ?page .}",
						    							 	  uri));
	}
	
}
