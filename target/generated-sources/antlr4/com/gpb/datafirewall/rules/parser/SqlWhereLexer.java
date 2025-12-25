// Generated from com/gpb/datafirewall/rules/parser/SqlWhere.g4 by ANTLR 4.13.1
package com.gpb.datafirewall.rules.parser;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class SqlWhereLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.13.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, IS=9, 
		NOT=10, AS=11, CAST=12, LIKE=13, IN=14, NULL_=15, AND=16, OR=17, LPAREN=18, 
		RPAREN=19, COMMA=20, DQIDENT=21, STRING=22, NUMBER=23, IDENT=24, WS=25;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "IS", 
			"NOT", "AS", "CAST", "LIKE", "IN", "NULL_", "AND", "OR", "LPAREN", "RPAREN", 
			"COMMA", "DQIDENT", "STRING", "NUMBER", "IDENT", "WS"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'.'", "'='", "'!='", "'<>'", "'<'", "'>'", "'<='", "'>='", null, 
			null, null, null, null, null, null, null, null, "'('", "')'", "','"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, "IS", "NOT", "AS", 
			"CAST", "LIKE", "IN", "NULL_", "AND", "OR", "LPAREN", "RPAREN", "COMMA", 
			"DQIDENT", "STRING", "NUMBER", "IDENT", "WS"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public SqlWhereLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "SqlWhere.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\u0004\u0000\u0019\u00a1\u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002"+
		"\u0001\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002"+
		"\u0004\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002"+
		"\u0007\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002"+
		"\u000b\u0007\u000b\u0002\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e"+
		"\u0002\u000f\u0007\u000f\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011"+
		"\u0002\u0012\u0007\u0012\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014"+
		"\u0002\u0015\u0007\u0015\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017"+
		"\u0002\u0018\u0007\u0018\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001"+
		"\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0003\u0001\u0003\u0001\u0003"+
		"\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005\u0001\u0006\u0001\u0006"+
		"\u0001\u0006\u0001\u0007\u0001\u0007\u0001\u0007\u0001\b\u0001\b\u0001"+
		"\b\u0001\t\u0001\t\u0001\t\u0001\t\u0001\n\u0001\n\u0001\n\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0001\f\u0001\r\u0001\r\u0001\r\u0001\u000e\u0001\u000e\u0001"+
		"\u000e\u0001\u000e\u0001\u000e\u0001\u000f\u0001\u000f\u0001\u000f\u0001"+
		"\u000f\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0011\u0001\u0011\u0001"+
		"\u0012\u0001\u0012\u0001\u0013\u0001\u0013\u0001\u0014\u0001\u0014\u0001"+
		"\u0014\u0001\u0014\u0005\u0014u\b\u0014\n\u0014\f\u0014x\t\u0014\u0001"+
		"\u0014\u0001\u0014\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0005"+
		"\u0015\u0080\b\u0015\n\u0015\f\u0015\u0083\t\u0015\u0001\u0015\u0001\u0015"+
		"\u0001\u0016\u0004\u0016\u0088\b\u0016\u000b\u0016\f\u0016\u0089\u0001"+
		"\u0016\u0001\u0016\u0004\u0016\u008e\b\u0016\u000b\u0016\f\u0016\u008f"+
		"\u0003\u0016\u0092\b\u0016\u0001\u0017\u0001\u0017\u0005\u0017\u0096\b"+
		"\u0017\n\u0017\f\u0017\u0099\t\u0017\u0001\u0018\u0004\u0018\u009c\b\u0018"+
		"\u000b\u0018\f\u0018\u009d\u0001\u0018\u0001\u0018\u0000\u0000\u0019\u0001"+
		"\u0001\u0003\u0002\u0005\u0003\u0007\u0004\t\u0005\u000b\u0006\r\u0007"+
		"\u000f\b\u0011\t\u0013\n\u0015\u000b\u0017\f\u0019\r\u001b\u000e\u001d"+
		"\u000f\u001f\u0010!\u0011#\u0012%\u0013\'\u0014)\u0015+\u0016-\u0017/"+
		"\u00181\u0019\u0001\u0000\u0013\u0002\u0000IIii\u0002\u0000SSss\u0002"+
		"\u0000NNnn\u0002\u0000OOoo\u0002\u0000TTtt\u0002\u0000AAaa\u0002\u0000"+
		"CCcc\u0002\u0000LLll\u0002\u0000KKkk\u0002\u0000EEee\u0002\u0000UUuu\u0002"+
		"\u0000DDdd\u0002\u0000RRrr\u0002\u0000\"\"\\\\\u0002\u0000\'\'\\\\\u0001"+
		"\u000009\u0003\u0000AZ__az\u0004\u000009AZ__az\u0003\u0000\t\n\r\r  \u00a9"+
		"\u0000\u0001\u0001\u0000\u0000\u0000\u0000\u0003\u0001\u0000\u0000\u0000"+
		"\u0000\u0005\u0001\u0000\u0000\u0000\u0000\u0007\u0001\u0000\u0000\u0000"+
		"\u0000\t\u0001\u0000\u0000\u0000\u0000\u000b\u0001\u0000\u0000\u0000\u0000"+
		"\r\u0001\u0000\u0000\u0000\u0000\u000f\u0001\u0000\u0000\u0000\u0000\u0011"+
		"\u0001\u0000\u0000\u0000\u0000\u0013\u0001\u0000\u0000\u0000\u0000\u0015"+
		"\u0001\u0000\u0000\u0000\u0000\u0017\u0001\u0000\u0000\u0000\u0000\u0019"+
		"\u0001\u0000\u0000\u0000\u0000\u001b\u0001\u0000\u0000\u0000\u0000\u001d"+
		"\u0001\u0000\u0000\u0000\u0000\u001f\u0001\u0000\u0000\u0000\u0000!\u0001"+
		"\u0000\u0000\u0000\u0000#\u0001\u0000\u0000\u0000\u0000%\u0001\u0000\u0000"+
		"\u0000\u0000\'\u0001\u0000\u0000\u0000\u0000)\u0001\u0000\u0000\u0000"+
		"\u0000+\u0001\u0000\u0000\u0000\u0000-\u0001\u0000\u0000\u0000\u0000/"+
		"\u0001\u0000\u0000\u0000\u00001\u0001\u0000\u0000\u0000\u00013\u0001\u0000"+
		"\u0000\u0000\u00035\u0001\u0000\u0000\u0000\u00057\u0001\u0000\u0000\u0000"+
		"\u0007:\u0001\u0000\u0000\u0000\t=\u0001\u0000\u0000\u0000\u000b?\u0001"+
		"\u0000\u0000\u0000\rA\u0001\u0000\u0000\u0000\u000fD\u0001\u0000\u0000"+
		"\u0000\u0011G\u0001\u0000\u0000\u0000\u0013J\u0001\u0000\u0000\u0000\u0015"+
		"N\u0001\u0000\u0000\u0000\u0017Q\u0001\u0000\u0000\u0000\u0019V\u0001"+
		"\u0000\u0000\u0000\u001b[\u0001\u0000\u0000\u0000\u001d^\u0001\u0000\u0000"+
		"\u0000\u001fc\u0001\u0000\u0000\u0000!g\u0001\u0000\u0000\u0000#j\u0001"+
		"\u0000\u0000\u0000%l\u0001\u0000\u0000\u0000\'n\u0001\u0000\u0000\u0000"+
		")p\u0001\u0000\u0000\u0000+{\u0001\u0000\u0000\u0000-\u0087\u0001\u0000"+
		"\u0000\u0000/\u0093\u0001\u0000\u0000\u00001\u009b\u0001\u0000\u0000\u0000"+
		"34\u0005.\u0000\u00004\u0002\u0001\u0000\u0000\u000056\u0005=\u0000\u0000"+
		"6\u0004\u0001\u0000\u0000\u000078\u0005!\u0000\u000089\u0005=\u0000\u0000"+
		"9\u0006\u0001\u0000\u0000\u0000:;\u0005<\u0000\u0000;<\u0005>\u0000\u0000"+
		"<\b\u0001\u0000\u0000\u0000=>\u0005<\u0000\u0000>\n\u0001\u0000\u0000"+
		"\u0000?@\u0005>\u0000\u0000@\f\u0001\u0000\u0000\u0000AB\u0005<\u0000"+
		"\u0000BC\u0005=\u0000\u0000C\u000e\u0001\u0000\u0000\u0000DE\u0005>\u0000"+
		"\u0000EF\u0005=\u0000\u0000F\u0010\u0001\u0000\u0000\u0000GH\u0007\u0000"+
		"\u0000\u0000HI\u0007\u0001\u0000\u0000I\u0012\u0001\u0000\u0000\u0000"+
		"JK\u0007\u0002\u0000\u0000KL\u0007\u0003\u0000\u0000LM\u0007\u0004\u0000"+
		"\u0000M\u0014\u0001\u0000\u0000\u0000NO\u0007\u0005\u0000\u0000OP\u0007"+
		"\u0001\u0000\u0000P\u0016\u0001\u0000\u0000\u0000QR\u0007\u0006\u0000"+
		"\u0000RS\u0007\u0005\u0000\u0000ST\u0007\u0001\u0000\u0000TU\u0007\u0004"+
		"\u0000\u0000U\u0018\u0001\u0000\u0000\u0000VW\u0007\u0007\u0000\u0000"+
		"WX\u0007\u0000\u0000\u0000XY\u0007\b\u0000\u0000YZ\u0007\t\u0000\u0000"+
		"Z\u001a\u0001\u0000\u0000\u0000[\\\u0007\u0000\u0000\u0000\\]\u0007\u0002"+
		"\u0000\u0000]\u001c\u0001\u0000\u0000\u0000^_\u0007\u0002\u0000\u0000"+
		"_`\u0007\n\u0000\u0000`a\u0007\u0007\u0000\u0000ab\u0007\u0007\u0000\u0000"+
		"b\u001e\u0001\u0000\u0000\u0000cd\u0007\u0005\u0000\u0000de\u0007\u0002"+
		"\u0000\u0000ef\u0007\u000b\u0000\u0000f \u0001\u0000\u0000\u0000gh\u0007"+
		"\u0003\u0000\u0000hi\u0007\f\u0000\u0000i\"\u0001\u0000\u0000\u0000jk"+
		"\u0005(\u0000\u0000k$\u0001\u0000\u0000\u0000lm\u0005)\u0000\u0000m&\u0001"+
		"\u0000\u0000\u0000no\u0005,\u0000\u0000o(\u0001\u0000\u0000\u0000pv\u0005"+
		"\"\u0000\u0000qu\b\r\u0000\u0000rs\u0005\\\u0000\u0000su\t\u0000\u0000"+
		"\u0000tq\u0001\u0000\u0000\u0000tr\u0001\u0000\u0000\u0000ux\u0001\u0000"+
		"\u0000\u0000vt\u0001\u0000\u0000\u0000vw\u0001\u0000\u0000\u0000wy\u0001"+
		"\u0000\u0000\u0000xv\u0001\u0000\u0000\u0000yz\u0005\"\u0000\u0000z*\u0001"+
		"\u0000\u0000\u0000{\u0081\u0005\'\u0000\u0000|\u0080\b\u000e\u0000\u0000"+
		"}~\u0005\\\u0000\u0000~\u0080\t\u0000\u0000\u0000\u007f|\u0001\u0000\u0000"+
		"\u0000\u007f}\u0001\u0000\u0000\u0000\u0080\u0083\u0001\u0000\u0000\u0000"+
		"\u0081\u007f\u0001\u0000\u0000\u0000\u0081\u0082\u0001\u0000\u0000\u0000"+
		"\u0082\u0084\u0001\u0000\u0000\u0000\u0083\u0081\u0001\u0000\u0000\u0000"+
		"\u0084\u0085\u0005\'\u0000\u0000\u0085,\u0001\u0000\u0000\u0000\u0086"+
		"\u0088\u0007\u000f\u0000\u0000\u0087\u0086\u0001\u0000\u0000\u0000\u0088"+
		"\u0089\u0001\u0000\u0000\u0000\u0089\u0087\u0001\u0000\u0000\u0000\u0089"+
		"\u008a\u0001\u0000\u0000\u0000\u008a\u0091\u0001\u0000\u0000\u0000\u008b"+
		"\u008d\u0005.\u0000\u0000\u008c\u008e\u0007\u000f\u0000\u0000\u008d\u008c"+
		"\u0001\u0000\u0000\u0000\u008e\u008f\u0001\u0000\u0000\u0000\u008f\u008d"+
		"\u0001\u0000\u0000\u0000\u008f\u0090\u0001\u0000\u0000\u0000\u0090\u0092"+
		"\u0001\u0000\u0000\u0000\u0091\u008b\u0001\u0000\u0000\u0000\u0091\u0092"+
		"\u0001\u0000\u0000\u0000\u0092.\u0001\u0000\u0000\u0000\u0093\u0097\u0007"+
		"\u0010\u0000\u0000\u0094\u0096\u0007\u0011\u0000\u0000\u0095\u0094\u0001"+
		"\u0000\u0000\u0000\u0096\u0099\u0001\u0000\u0000\u0000\u0097\u0095\u0001"+
		"\u0000\u0000\u0000\u0097\u0098\u0001\u0000\u0000\u0000\u00980\u0001\u0000"+
		"\u0000\u0000\u0099\u0097\u0001\u0000\u0000\u0000\u009a\u009c\u0007\u0012"+
		"\u0000\u0000\u009b\u009a\u0001\u0000\u0000\u0000\u009c\u009d\u0001\u0000"+
		"\u0000\u0000\u009d\u009b\u0001\u0000\u0000\u0000\u009d\u009e\u0001\u0000"+
		"\u0000\u0000\u009e\u009f\u0001\u0000\u0000\u0000\u009f\u00a0\u0006\u0018"+
		"\u0000\u0000\u00a02\u0001\u0000\u0000\u0000\n\u0000tv\u007f\u0081\u0089"+
		"\u008f\u0091\u0097\u009d\u0001\u0006\u0000\u0000";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}