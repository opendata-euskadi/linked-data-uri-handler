package r01hp.util.parser;

import lombok.NoArgsConstructor;
import r01f.io.CharacterStreamSource;

@NoArgsConstructor
  class R01HTokenizerStateHandlerForText
extends R01HTokenizerStateHandlerBase {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean read(final R01HTokenizer tokenizer,final CharacterStreamSource charReader) throws R01HParseError {
		boolean tokenFinished = false;
		
		char c = charReader.read();
        if (c == '<') {
        	if (charReader.nextMatchesPattern(1,"[a-zA-z_]")) {
        		charReader.unread(1);								// unread... it's another token
        		tokenizer.nextState(R01HTokenizerState.StartTag);
        		tokenFinished = true;
        	}
        	else if (charReader.nextMatchesPattern(2,"/[a-zA-Z_]")) {
        		charReader.unread(1);								// unread... it's another token
        		tokenizer.nextState(R01HTokenizerState.EndTag);
        		tokenFinished = true;
        	}
        	else if (charReader.nextEquals("!--#")) {
        		charReader.unread(1);								// unread... it's another token
        		tokenizer.nextState(R01HTokenizerState.SSIInclude);
        		tokenFinished = true;
        	}
        	else if (charReader.nextEquals("!--")) {
        		charReader.unread(1);								// unread... it's another token
        		tokenizer.nextState(R01HTokenizerState.Comment);
        		tokenFinished = true;
        	}
        	else if (charReader.nextEqualsIgnoreCase("!DOCTYPE")) {
        		charReader.unread(1);								// unread... it's another token
        		tokenizer.nextState(R01HTokenizerState.DocType);
        		tokenFinished = true;
        	} 
        	else {
        		tokenizer.addTextToCurrentToken(c);
        	}
        }
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
        return tokenFinished;	// it's all done?
	}
}
