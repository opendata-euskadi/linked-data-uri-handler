package r01hp.portal.appembed.help;

import java.io.IOException;
import java.io.Writer;

import javax.inject.Inject;

import r01hp.portal.appembed.R01HPortalPageAppEmbedServletFilterConfig;
import r01hp.portal.appembed.metrics.R01HPortalPageAppEmbedMetrics;

public class R01HPortalPageEmbedServletFilterDefaultHelp 
	 extends R01HPortalPageEmbedServletFilterHelpBase {
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	@Inject
	public R01HPortalPageEmbedServletFilterDefaultHelp(final R01HPortalPageAppEmbedServletFilterConfig config,
													   final R01HPortalPageAppEmbedMetrics metrics) {
		super(config, 
			  metrics);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	protected void _renderMyHEADResources(final Writer w) throws IOException {
		// do nothing
	}
	@Override
	protected void _renderMyTabs(final Writer w) throws IOException {
		// do nothing
	}
	@Override
	protected void _renderMyTabsContent(final Writer w) throws IOException {
		// do nothing		
	}
}
