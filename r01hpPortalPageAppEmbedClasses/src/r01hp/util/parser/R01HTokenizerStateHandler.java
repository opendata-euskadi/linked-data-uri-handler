package r01hp.util.parser;

import r01f.io.CharacterStreamSource;

interface R01HTokenizerStateHandler {
/////////////////////////////////////////////////////////////////////////////////////////
//  METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Reads a character and returns true if the token is finished
	 * (see {@link R01HTokenizerObservable})
	 * @param tokenizer
	 * @param charReader
	 * @return
	 * @throws R01HParseError
	 */
	public boolean read(final R01HTokenizer tokenizer,final CharacterStreamSource charReader) throws R01HParseError;
}
