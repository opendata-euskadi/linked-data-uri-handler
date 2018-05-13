package r01hp.portal.appembed.metrics;

import com.codahale.metrics.Timer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.types.Path;

@Accessors(prefix="_")
@RequiredArgsConstructor
public class R01HPortalPageAppEmbedMetricsContext {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////	
	@Getter private final Path _appModulePath;
	@Getter private final Timer.Context _globalMetricTimerCtx;
	@Getter private final Timer.Context _appModuleTimerCtx;
}
