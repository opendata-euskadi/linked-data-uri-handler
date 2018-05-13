package r01hp.lod.urihandler;

import r01f.types.url.Url;

public interface R01HLODMainEntityOfPageResolver {
	public boolean isMainEntityOfPage(final Url uri);
	public Url mainEntityOfPage(final Url uri);
}
