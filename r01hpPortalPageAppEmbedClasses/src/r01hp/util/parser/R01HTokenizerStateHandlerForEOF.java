package r01hp.util.parser;

import lombok.NoArgsConstructor;
import r01f.io.CharacterStreamSource;

@NoArgsConstructor
     class R01HTokenizerStateHandlerForEOF
implements R01HTokenizerStateHandler {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean read(final R01HTokenizer tokenizer,final CharacterStreamSource charReader) throws R01HParseError {
		throw new IllegalStateException();	// never call this
	}
}
