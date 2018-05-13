package r01hp.portal.appembed.metrics;

import java.util.Map;

import javax.inject.Inject;
import javax.servlet.FilterConfig;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.collect.Maps;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.httpclient.HttpResponseCode;
import r01f.types.Path;
import r01f.util.types.Strings;

@Slf4j
@Accessors(prefix="_")
public class R01HPortalPageAppEmbedMetrics {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter private final R01HPortalPageAppEmbedMetricsConfig _config;
	@Getter private final MetricRegistry _registry;
			
	@Getter private final GlobalMetrics _globalMetrics;
	@Getter private final Map<Path,AppModuleMetrics> _appModuleMetrics;
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public R01HPortalPageAppEmbedMetrics() {
		this(new R01HPortalPageAppEmbedMetricsConfig(),		// disabled by default
			 new MetricRegistry());
	}
	public R01HPortalPageAppEmbedMetrics(final R01HPortalPageAppEmbedMetricsConfig config) {
		this(config,
			 new MetricRegistry());
	}
	@Inject
	public R01HPortalPageAppEmbedMetrics(final R01HPortalPageAppEmbedMetricsConfig config,
										 final MetricRegistry metricsRegistry) {
		_config = config;
		_registry = metricsRegistry;
		
		// Init the metrics
		_globalMetrics = new GlobalMetrics();
		_appModuleMetrics = Maps.newHashMapWithExpectedSize(100);
	}
	/**
	 * Creates NEW config object whose properties are overriden with the ones at web.xml file
	 * @param config
	 * @return
	 */
	public R01HPortalPageAppEmbedMetrics cloneOverridenWith(final FilterConfig config) {
		R01HPortalPageAppEmbedMetrics outConfig = this;
		
		String metricsEnabledStr = config.getInitParameter("r01hp.appembed.metricsEnabled");
		if (Strings.isNOTNullOrEmpty(metricsEnabledStr)) {
			log.warn("Metrics overriden al web.xml (servlet filter init params): {}",
					 metricsEnabledStr);
			boolean metricsEnabled = Boolean.parseBoolean(metricsEnabledStr);			
			outConfig = new R01HPortalPageAppEmbedMetrics(new R01HPortalPageAppEmbedMetricsConfig(metricsEnabled),
														  this.getRegistry());
		}
		return outConfig;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("resource")
	public R01HPortalPageAppEmbedMetricsContext preFilter(final Path appModulePath) {
    	_globalMetrics.getReqsCounter().inc();
    	final Timer.Context globalMetricTimerCtx = _globalMetrics.getReqTimer().time();

    	AppModuleMetrics appModuleMetrics = _appModuleMetrics.get(appModulePath);
    	if (appModuleMetrics == null) {
    		appModuleMetrics = new AppModuleMetrics(appModulePath);
    		_appModuleMetrics.put(appModulePath,appModuleMetrics);
    	}
    	appModuleMetrics.getReqsCounter().inc();
    	final Timer.Context appModuleTimerCtx = appModuleMetrics.getReqTimer().time();

    	return new R01HPortalPageAppEmbedMetricsContext(appModulePath,
    													globalMetricTimerCtx,
    													appModuleTimerCtx);
	}
	public void postFilter(final R01HPortalPageAppEmbedMetricsContext ctx,
						   final HttpResponseCode respCode) {
		// account the 500 response codes
		if (respCode.is500()) {
			_globalMetrics.getReqs500Counter().inc();
			AppModuleMetrics appModuleMetrics = _appModuleMetrics.get(ctx.getAppModulePath());
			if (appModuleMetrics != null) appModuleMetrics.getReqs500Counter().inc();
		}

		// close timers
        ctx.getGlobalMetricTimerCtx().close();
        ctx.getAppModuleTimerCtx().close();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
    @Accessors(prefix="_")
    private abstract class MetricsBase {
		@Getter private final Counter _reqsCounter;
		@Getter private final Counter _reqs500Counter;
		@Getter private final Timer _reqTimer;

		private MetricsBase(final String discriminator) {
			_reqsCounter = _registry.counter(MetricRegistry.name(R01HPortalPageAppEmbedMetrics.this.getClass(),
																	    discriminator,
																		"reqsCounter"));
			_reqs500Counter = _registry.counter(MetricRegistry.name(R01HPortalPageAppEmbedMetrics.this.getClass(),
																	       discriminator,
																		   "reqs500Counter"));
			_reqTimer = _registry.timer(MetricRegistry.name(R01HPortalPageAppEmbedMetrics.this.getClass(),
																   discriminator,
																   "reqTimer"));
		}
    }
    @Accessors(prefix="_")
    private class GlobalMetrics
    	  extends MetricsBase {
		private GlobalMetrics() {
			super("total");
		}
    }
    @Accessors(prefix="_")
    private class AppModuleMetrics
    	  extends MetricsBase {
    	@Getter private final Path _appModulePath;

		private AppModuleMetrics(final Path appModulePath) {
			super(appModulePath.asRelativeString().replaceAll("/","_"));
			_appModulePath = appModulePath;
		}
    }
}
