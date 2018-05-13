package r01hp.util.parser;

import lombok.NoArgsConstructor;

@NoArgsConstructor
abstract class R01HTokenizerStateHandlerBase
    implements R01HTokenizerStateHandler {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	protected static boolean _isAllowedChar(final String allowedChars,
											final char c) {
		boolean outAllowed = false;
		for (int i=0; i< allowedChars.length(); i++) {
			if (allowedChars.charAt(i) == c) {
				outAllowed = true;
				break;
			}
		}
		return outAllowed;
	}
}
