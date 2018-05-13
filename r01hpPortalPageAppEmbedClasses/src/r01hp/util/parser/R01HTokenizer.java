package r01hp.util.parser;

import r01f.io.CharacterStreamSource;

class R01HTokenizer {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	private final CharacterStreamSource _charReader;
	
	private StringBuilder _currTokenText = new StringBuilder();
	private R01HTokenizerState _currState = R01HTokenizerState.Text;
	
	private R01HTokenizerState _nextState = R01HTokenizerState.Text;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public R01HTokenizer(final CharacterStreamSource charReader) {
		_charReader = charReader;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  STATE METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	public R01HTokenizerState getCurrentState() {
		return _currState;
	}
	public boolean hasToChangeState() {
		return _nextState != _currState;
	}
	public void changeState() {
		_currState = _nextState;
	}
	public void nextState(final R01HTokenizerState nextState) {
		_nextState = nextState;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  READ METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	public boolean read() throws R01HParseError {
		return _currState.read(this,_charReader);
	}
	public StringBuilder getCurrentTokenText() {
		return _currTokenText;
	}
	public void addTextToCurrentToken(final char character) {
		_currTokenText.append(character);
	}
	public void addTextToCurrentToken(final String str) {
		_currTokenText.append(str);
	}
	public R01HToken getCurrentToken() {
		if (_currTokenText == null) return null;		// no token to emit
		
		// Build token
		R01HToken outToken = new R01HToken(_currState.getType(),
								   		   _currTokenText.toString());
		// set current token from next token data
		_currTokenText = new StringBuilder();
		
		return outToken;
	}
}
