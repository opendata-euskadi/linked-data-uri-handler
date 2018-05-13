package r01hp.util.parser;


import lombok.NoArgsConstructor;
import r01f.io.CharacterStreamSource;

@NoArgsConstructor
  class R01HTokenizerStateHandlerForTagStart
extends R01HTokenizerStateHandlerForTagBase {
/////////////////////////////////////////////////////////////////////////////////////////
//  METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean read(final R01HTokenizer tokenizer,final CharacterStreamSource charReader) throws R01HParseError {
		boolean tokenFinished = false;
		
		char c = charReader.read();
		if (tokenizer.getCurrentTokenText().length() == 0 && c == '<') {
			tokenizer.addTextToCurrentToken(c);		
		}
		else if (c == '>') {
        	tokenizer.addTextToCurrentToken(c);		// read and finish
    		tokenizer.nextState(R01HTokenizerState.Text);
    		tokenFinished = true;
        }
		else if (c == '<') {
			// any < char inside a tag is not allowed: ie: <bo<dy>
        	charReader.unread(1);					// unread... it's another token	
    		tokenizer.nextState(R01HTokenizerState.Text);
    		tokenFinished = false;
		}
//        else if (!_isAllowedChar(c)) {
//        	charReader.unread(1);					// unread... it's another token	
//    		tokenizer.nextState(R01HTokenizerState.Text);
//    		tokenFinished = false;
//        }
        else if (c == CharacterStreamSource.NULL_CHAR) {                
            throw new R01HParseError(charReader.currentPosition()-1,"Null char detected");
        } 
        else if (c == CharacterStreamSource.EOF) {
        	tokenizer.nextState(R01HTokenizerState.EOF);	// we've done!
        	tokenFinished = true;
        }
        else {
        	tokenizer.addTextToCurrentToken(c);	
        }
        return tokenFinished;
	}
}
