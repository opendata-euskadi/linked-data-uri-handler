package r01hp.portal.appembed.help;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import r01hp.portal.appembed.R01HPortalPageAppEmbedContext;

public interface R01HPortalPageEmbedServletFilterHelp {
	/**
	 * Adds a log entry for the request
	 * @param ctx
	 */
	public void addRequestLogEntry(final R01HPortalPageAppEmbedContext ctx,
								   final long elapsedMilis);
	/**
	 * Renders the help page
	 * @param realHttpReq
	 * @param realHttpResp
	 * @throws IOException
	 */
	public abstract void renderHelp(final HttpServletRequest realHttpReq,
                           			final HttpServletResponse realHttpResp) throws IOException;
}
