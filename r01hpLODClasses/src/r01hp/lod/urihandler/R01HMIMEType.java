package r01hp.lod.urihandler;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import com.google.common.collect.Lists;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.mime.MimeType;
import r01f.types.CanBeRepresentedAsString;
import r01f.util.types.Strings;

/**
 * Taken from http://docs.rdf4j.org/rest-api/#_content_types and http://docs.stardog.com/#_http_headers_content_type_accept
 */
@Accessors(prefix="_")
@RequiredArgsConstructor
public enum R01HMIMEType
 implements CanBeRepresentedAsString {
	
	JSON(new MimeType("application/json")),
	
	RDFXML(new MimeType("application/rdf+xml")),
	BinaryRDF(new MimeType("application/x-binary-rdf")),
	RDFJSON(new MimeType("application/rdf+json")),
	
	Turtle(new MimeType("text/turtle")),
	NTriples(new MimeType("text/plain")),
	N3(new MimeType("text/rdf+n3")),
	NQuads(new MimeType("text/x-nquads")),
	
	JSONLD(new MimeType("application/ld+json")),
	TriG(new MimeType("application/trig")),
	TriX(new MimeType("application/trix")),
	
	SPARQLXMLResultsFormat(new MimeType("application/sparql-results+xml")),
	SPARQLJSONResultsFormat(new MimeType("application/sparql-results+json")),
	SPARQLBooleanResults(new MimeType("text/boolean")),
	SPARQLBinaryResults(new MimeType("application/x-binary-rdf-results-table")),
	
	CSV(new MimeType("text/csv")),
	TABVAL(new MimeType("text/tab-separated-values")),
	
	HTML(new MimeType("text/html"));
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter private final MimeType _mime;
/////////////////////////////////////////////////////////////////////////////////////////
//	STRING CONVERSION
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public String toString() {
		return _mime.toString();
	}
	@Override
	public String asString() {
		return this.toString();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	METHOD
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Return true if the given request accepts the mimetype
	 * @param req
	 * @return
	 */
	public boolean isAcceptedBy(final HttpServletRequest req) {
		String acceptHeader = req.getHeader("Accept");
		if (Strings.isNOTNullOrEmpty(acceptHeader)) return false;
		return acceptHeader.contains(_mime.getName());	
	}
	public boolean is(final R01HMIMEType other) {
		return this == other;
	}
	public boolean isNOT(final R01HMIMEType other) {
		boolean is = this.is(other);
		return !is;
	}
	public boolean isIn(final R01HMIMEType... others) {
		boolean outIsIn = false;
		for (R01HMIMEType other : others) {
			if (this == other) {
				outIsIn = true;
				break;
			}
		}
		return outIsIn;
	}
	public boolean isNOTIn(final R01HMIMEType... others) {
		boolean isIn = this.isIn(others);
		return !isIn;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
	public static Collection<R01HMIMEType> fromRequest(final HttpServletRequest req) {
		String acceptHeader = req.getHeader("Accept");
		Collection<R01HMIMEType> outMimes = Lists.newArrayList();
		for (R01HMIMEType mime : R01HMIMEType.values()) {
			if (acceptHeader.contains(mime.getMime().getName())) {
				outMimes.add(mime);
			}
		}
		return outMimes;
	}
	public static boolean canBeFromFileExtension(final String ext) {
		return Strings.isContainedWrapper(ext)
							.containsAny("jsonld","rdf","ttl");
	}
	public static R01HMIMEType fromFileExtension(final String ext) {
		R01HMIMEType outMime = null;			// by default
		if (Strings.isNullOrEmpty(ext)) {
			return null;
		}
		if (ext.equals("jsonld")) {
			outMime =  R01HMIMEType.JSONLD;
		} else if (ext.equals("rdf")) {
			outMime =  R01HMIMEType.RDFXML;
		} else if (ext.equals(".ttl")) {
			outMime =  R01HMIMEType.Turtle;
		} else {
			// nothing	
		}
		return outMime;
	}
}