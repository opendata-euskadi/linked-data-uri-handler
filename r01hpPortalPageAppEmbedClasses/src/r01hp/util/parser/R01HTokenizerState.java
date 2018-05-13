package r01hp.util.parser;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.io.CharacterStreamSource;

/**
 * Models the tokenizer state as characters are being readed 
 * Every state has a {@link R01HTokenizerStateHandler} type that reads from the character stream and
 * guess what's the next state to move to
 */
@Accessors(prefix="_")
@RequiredArgsConstructor
enum R01HTokenizerState {
	Text		(R01HTokenType.Text,		new R01HTokenizerStateHandlerForText()),
	DocType		(R01HTokenType.DocType,		new R01HTokenizerStateHandlerForDocType()),
	StartTag	(R01HTokenType.StartTag,	new R01HTokenizerStateHandlerForTagStart()),
	EndTag		(R01HTokenType.EndTag,		new R01HTokenizerStateHandlerForTagEnd()),
	Comment		(R01HTokenType.Comment,		new R01HTokenizerStateHandlerForComment()),
	SSIInclude	(R01HTokenType.SSIInclude,	new R01HTokenizerStateHandlerForSSIInclude()),
	EOF			(R01HTokenType.EOF,			new R01HTokenizerStateHandlerForEOF());
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter private final R01HTokenType _type;
			private final R01HTokenizerStateHandler _tokenHandler;
/////////////////////////////////////////////////////////////////////////////////////////
//  METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Reads a token character; returns true if the token is finished
	 * (see {@link R01HTokenizerObservable})
	 * @param tokenizer
	 * @param charReader
	 * @return
	 * @throws R01HParseError
	 */
	public boolean read(final R01HTokenizer tokenizer,final CharacterStreamSource charReader) throws R01HParseError {
		return _tokenHandler.read(tokenizer,charReader);
	}
}
