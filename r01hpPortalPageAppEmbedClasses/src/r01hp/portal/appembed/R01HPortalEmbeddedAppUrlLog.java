package r01hp.portal.appembed;

import java.io.IOException;
import java.io.Writer;
import java.util.Date;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.locale.Language;
import r01f.types.CanBeRepresentedAsString;
import r01f.util.types.Strings;
import r01f.util.types.locale.Languages;
import r01hp.portal.common.R01HPortalOIDs.R01HPortalID;
import r01hp.portal.common.R01HPortalOIDs.R01HPortalPageID;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;
import rx.functions.Action1;

/**
 * A fixed-length little log of the last urls proxied through the container page include servlet filter
 * (see {@link R01HPortalPageAppEmbedServletFilter})
 */
public class R01HPortalEmbeddedAppUrlLog {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	private final R01HIncludedAppUrlLogEntry[] _logEntries;
	private int _currLogEntryWritePos = 0;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public R01HPortalEmbeddedAppUrlLog(final int logSize) {
		_logEntries = new R01HIncludedAppUrlLogEntry[logSize];		// stores the log entries
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Resets the request log
	 */
	public void reset() {
		for(int i=0; i<_logEntries.length; i++) {
			_logEntries[i] = null;
		}
	}
	/**
	 * Adds a new log entry
	 * @param ctx
	 */
	public void add(final R01HPortalPageAppEmbedContext ctx,
					final long elapsedMilis) {
    	R01HIncludedAppUrlLogEntry logEntry = new R01HIncludedAppUrlLogEntry(System.currentTimeMillis(),
    																		 ctx.getClientIp(),
    																		 ctx.getRequestedUrlPath().asAbsoluteString(),
    																		 !ctx.isIncludeInAppContainerPageDisabled(),
    																		 elapsedMilis,
    																		 ctx.getPortalId(),ctx.getPageId(),ctx.getLang());
    	_logEntries[_currLogEntryWritePos] = logEntry;
    	_currLogEntryWritePos++;
    	if (_currLogEntryWritePos == _logEntries.length) _currLogEntryWritePos = 0;		// circular log
	}
	/**
	 * Returns all log entries
	 * @return
	 */
	public Observable<R01HIncludedAppUrlLogEntry> getEntries() {
		return Observable.create(new OnSubscribe<R01HIncludedAppUrlLogEntry>() {
										@Override
										public void call(final Subscriber<? super R01HIncludedAppUrlLogEntry> observer) {
											observer.onStart();
											for(int i=_currLogEntryWritePos; i < _logEntries.length; i++) {
												if (_logEntries[i] != null) observer.onNext(_logEntries[i]);
											}
											for(int i=0; i < _currLogEntryWritePos; i++) {
												if (_logEntries[i] != null) observer.onNext(_logEntries[i]);
											}
											observer.onCompleted();
										}
						  		 });
	}
	/**
	 * Writes the log entries to the given writer
	 * @param w
	 */
	public void printEntriesTo(final Writer w) {
		this.getEntries().subscribe(new Action1<R01HIncludedAppUrlLogEntry>() {
												@Override
												public void call(final R01HIncludedAppUrlLogEntry entry) {
													try {
														w.write(entry.asString());
														w.write("\n");
													} catch(IOException ioEx) { /* ignored */ }
												}
									});
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Accessors(prefix="_")
	@RequiredArgsConstructor
	public class R01HIncludedAppUrlLogEntry 
	  implements CanBeRepresentedAsString {
		@Getter private final long _timeStamp;
		@Getter private final String _clientIp;
		@Getter private final String _url;
		@Getter private final boolean _includedInAppContainerPage;
		@Getter private final long _elapsedMilis;
		@Getter private final R01HPortalID _portalId;
		@Getter private final R01HPortalPageID _pageId;
		@Getter private final Language _lang;
		
		@Override
		public String asString() {
			return Strings.customized("{} | {} | {} | {} milis | {}-{}/{} | {}",
									  new Date(_timeStamp),
									  _clientIp,
									  _includedInAppContainerPage,
									  _elapsedMilis,
									  _portalId,_pageId,Languages.countryLowerCase(_lang),_url);
		}
	}
}
