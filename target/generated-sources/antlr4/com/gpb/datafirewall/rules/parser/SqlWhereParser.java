// Generated from com/gpb/datafirewall/rules/parser/SqlWhere.g4 by ANTLR 4.13.1
package com.gpb.datafirewall.rules.parser;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class SqlWhereParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, IS=9, 
		NOT=10, AS=11, CAST=12, LIKE=13, IN=14, NULL_=15, AND=16, OR=17, LPAREN=18, 
		RPAREN=19, COMMA=20, DQIDENT=21, STRING=22, NUMBER=23, IDENT=24, WS=25;
	public static final int
		RULE_parse = 0, RULE_expression = 1, RULE_predicate = 2, RULE_value = 3, 
		RULE_functionCall = 4, RULE_functionArgs = 5, RULE_cmpOp = 6;
	private static String[] makeRuleNames() {
		return new String[] {
			"parse", "expression", "predicate", "value", "functionCall", "functionArgs", 
			"cmpOp"
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

	@Override
	public String getGrammarFileName() { return "SqlWhere.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public SqlWhereParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ParseContext extends ParserRuleContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode EOF() { return getToken(SqlWhereParser.EOF, 0); }
		public ParseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parse; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlWhereListener ) ((SqlWhereListener)listener).enterParse(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlWhereListener ) ((SqlWhereListener)listener).exitParse(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlWhereVisitor ) return ((SqlWhereVisitor<? extends T>)visitor).visitParse(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ParseContext parse() throws RecognitionException {
		ParseContext _localctx = new ParseContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_parse);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(14);
			expression(0);
			setState(15);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExpressionContext extends ParserRuleContext {
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression; }
	 
		public ExpressionContext() { }
		public void copyFrom(ExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class AndExprContext extends ExpressionContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode AND() { return getToken(SqlWhereParser.AND, 0); }
		public AndExprContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlWhereListener ) ((SqlWhereListener)listener).enterAndExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlWhereListener ) ((SqlWhereListener)listener).exitAndExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlWhereVisitor ) return ((SqlWhereVisitor<? extends T>)visitor).visitAndExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class PredicateOnlyContext extends ExpressionContext {
		public PredicateContext predicate() {
			return getRuleContext(PredicateContext.class,0);
		}
		public PredicateOnlyContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlWhereListener ) ((SqlWhereListener)listener).enterPredicateOnly(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlWhereListener ) ((SqlWhereListener)listener).exitPredicateOnly(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlWhereVisitor ) return ((SqlWhereVisitor<? extends T>)visitor).visitPredicateOnly(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NotExprContext extends ExpressionContext {
		public TerminalNode NOT() { return getToken(SqlWhereParser.NOT, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public NotExprContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlWhereListener ) ((SqlWhereListener)listener).enterNotExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlWhereListener ) ((SqlWhereListener)listener).exitNotExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlWhereVisitor ) return ((SqlWhereVisitor<? extends T>)visitor).visitNotExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ParenExprContext extends ExpressionContext {
		public TerminalNode LPAREN() { return getToken(SqlWhereParser.LPAREN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(SqlWhereParser.RPAREN, 0); }
		public ParenExprContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlWhereListener ) ((SqlWhereListener)listener).enterParenExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlWhereListener ) ((SqlWhereListener)listener).exitParenExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlWhereVisitor ) return ((SqlWhereVisitor<? extends T>)visitor).visitParenExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class OrExprContext extends ExpressionContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode OR() { return getToken(SqlWhereParser.OR, 0); }
		public OrExprContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlWhereListener ) ((SqlWhereListener)listener).enterOrExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlWhereListener ) ((SqlWhereListener)listener).exitOrExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlWhereVisitor ) return ((SqlWhereVisitor<? extends T>)visitor).visitOrExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionContext expression() throws RecognitionException {
		return expression(0);
	}

	private ExpressionContext expression(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ExpressionContext _localctx = new ExpressionContext(_ctx, _parentState);
		ExpressionContext _prevctx = _localctx;
		int _startState = 2;
		enterRecursionRule(_localctx, 2, RULE_expression, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(25);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NOT:
				{
				_localctx = new NotExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(18);
				match(NOT);
				setState(19);
				expression(3);
				}
				break;
			case CAST:
			case DQIDENT:
			case STRING:
			case NUMBER:
			case IDENT:
				{
				_localctx = new PredicateOnlyContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(20);
				predicate();
				}
				break;
			case LPAREN:
				{
				_localctx = new ParenExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(21);
				match(LPAREN);
				setState(22);
				expression(0);
				setState(23);
				match(RPAREN);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(35);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(33);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
					case 1:
						{
						_localctx = new AndExprContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(27);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(28);
						match(AND);
						setState(29);
						expression(6);
						}
						break;
					case 2:
						{
						_localctx = new OrExprContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(30);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(31);
						match(OR);
						setState(32);
						expression(5);
						}
						break;
					}
					} 
				}
				setState(37);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PredicateContext extends ParserRuleContext {
		public PredicateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_predicate; }
	 
		public PredicateContext() { }
		public void copyFrom(PredicateContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class LikeExprContext extends PredicateContext {
		public ValueContext value() {
			return getRuleContext(ValueContext.class,0);
		}
		public TerminalNode LIKE() { return getToken(SqlWhereParser.LIKE, 0); }
		public TerminalNode STRING() { return getToken(SqlWhereParser.STRING, 0); }
		public LikeExprContext(PredicateContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlWhereListener ) ((SqlWhereListener)listener).enterLikeExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlWhereListener ) ((SqlWhereListener)listener).exitLikeExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlWhereVisitor ) return ((SqlWhereVisitor<? extends T>)visitor).visitLikeExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class IsNotNullExprContext extends PredicateContext {
		public ValueContext value() {
			return getRuleContext(ValueContext.class,0);
		}
		public TerminalNode IS() { return getToken(SqlWhereParser.IS, 0); }
		public TerminalNode NOT() { return getToken(SqlWhereParser.NOT, 0); }
		public TerminalNode NULL_() { return getToken(SqlWhereParser.NULL_, 0); }
		public IsNotNullExprContext(PredicateContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlWhereListener ) ((SqlWhereListener)listener).enterIsNotNullExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlWhereListener ) ((SqlWhereListener)listener).exitIsNotNullExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlWhereVisitor ) return ((SqlWhereVisitor<? extends T>)visitor).visitIsNotNullExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class IsNullExprContext extends PredicateContext {
		public ValueContext value() {
			return getRuleContext(ValueContext.class,0);
		}
		public TerminalNode IS() { return getToken(SqlWhereParser.IS, 0); }
		public TerminalNode NULL_() { return getToken(SqlWhereParser.NULL_, 0); }
		public IsNullExprContext(PredicateContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlWhereListener ) ((SqlWhereListener)listener).enterIsNullExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlWhereListener ) ((SqlWhereListener)listener).exitIsNullExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlWhereVisitor ) return ((SqlWhereVisitor<? extends T>)visitor).visitIsNullExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class CompareExprContext extends PredicateContext {
		public List<ValueContext> value() {
			return getRuleContexts(ValueContext.class);
		}
		public ValueContext value(int i) {
			return getRuleContext(ValueContext.class,i);
		}
		public CmpOpContext cmpOp() {
			return getRuleContext(CmpOpContext.class,0);
		}
		public CompareExprContext(PredicateContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlWhereListener ) ((SqlWhereListener)listener).enterCompareExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlWhereListener ) ((SqlWhereListener)listener).exitCompareExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlWhereVisitor ) return ((SqlWhereVisitor<? extends T>)visitor).visitCompareExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class InExprContext extends PredicateContext {
		public List<ValueContext> value() {
			return getRuleContexts(ValueContext.class);
		}
		public ValueContext value(int i) {
			return getRuleContext(ValueContext.class,i);
		}
		public TerminalNode IN() { return getToken(SqlWhereParser.IN, 0); }
		public TerminalNode LPAREN() { return getToken(SqlWhereParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(SqlWhereParser.RPAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(SqlWhereParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SqlWhereParser.COMMA, i);
		}
		public InExprContext(PredicateContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlWhereListener ) ((SqlWhereListener)listener).enterInExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlWhereListener ) ((SqlWhereListener)listener).exitInExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlWhereVisitor ) return ((SqlWhereVisitor<? extends T>)visitor).visitInExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class FuncExprContext extends PredicateContext {
		public FunctionCallContext functionCall() {
			return getRuleContext(FunctionCallContext.class,0);
		}
		public FuncExprContext(PredicateContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlWhereListener ) ((SqlWhereListener)listener).enterFuncExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlWhereListener ) ((SqlWhereListener)listener).exitFuncExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlWhereVisitor ) return ((SqlWhereVisitor<? extends T>)visitor).visitFuncExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PredicateContext predicate() throws RecognitionException {
		PredicateContext _localctx = new PredicateContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_predicate);
		int _la;
		try {
			setState(69);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				_localctx = new IsNullExprContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(38);
				value();
				setState(39);
				match(IS);
				setState(40);
				match(NULL_);
				}
				break;
			case 2:
				_localctx = new IsNotNullExprContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(42);
				value();
				setState(43);
				match(IS);
				setState(44);
				match(NOT);
				setState(45);
				match(NULL_);
				}
				break;
			case 3:
				_localctx = new CompareExprContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(47);
				value();
				setState(48);
				cmpOp();
				setState(49);
				value();
				}
				break;
			case 4:
				_localctx = new FuncExprContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(51);
				functionCall();
				}
				break;
			case 5:
				_localctx = new LikeExprContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(52);
				value();
				setState(53);
				match(LIKE);
				setState(54);
				match(STRING);
				}
				break;
			case 6:
				_localctx = new InExprContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(56);
				value();
				setState(57);
				match(IN);
				setState(58);
				match(LPAREN);
				setState(59);
				value();
				setState(64);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(60);
					match(COMMA);
					setState(61);
					value();
					}
					}
					setState(66);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(67);
				match(RPAREN);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ValueContext extends ParserRuleContext {
		public ValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_value; }
	 
		public ValueContext() { }
		public void copyFrom(ValueContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class CastValueContext extends ValueContext {
		public TerminalNode CAST() { return getToken(SqlWhereParser.CAST, 0); }
		public TerminalNode LPAREN() { return getToken(SqlWhereParser.LPAREN, 0); }
		public ValueContext value() {
			return getRuleContext(ValueContext.class,0);
		}
		public TerminalNode AS() { return getToken(SqlWhereParser.AS, 0); }
		public TerminalNode IDENT() { return getToken(SqlWhereParser.IDENT, 0); }
		public TerminalNode RPAREN() { return getToken(SqlWhereParser.RPAREN, 0); }
		public CastValueContext(ValueContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlWhereListener ) ((SqlWhereListener)listener).enterCastValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlWhereListener ) ((SqlWhereListener)listener).exitCastValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlWhereVisitor ) return ((SqlWhereVisitor<? extends T>)visitor).visitCastValue(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NumberValueContext extends ValueContext {
		public TerminalNode NUMBER() { return getToken(SqlWhereParser.NUMBER, 0); }
		public NumberValueContext(ValueContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlWhereListener ) ((SqlWhereListener)listener).enterNumberValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlWhereListener ) ((SqlWhereListener)listener).exitNumberValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlWhereVisitor ) return ((SqlWhereVisitor<? extends T>)visitor).visitNumberValue(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class FieldValueContext extends ValueContext {
		public TerminalNode DQIDENT() { return getToken(SqlWhereParser.DQIDENT, 0); }
		public FieldValueContext(ValueContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlWhereListener ) ((SqlWhereListener)listener).enterFieldValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlWhereListener ) ((SqlWhereListener)listener).exitFieldValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlWhereVisitor ) return ((SqlWhereVisitor<? extends T>)visitor).visitFieldValue(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class FuncValueContext extends ValueContext {
		public FunctionCallContext functionCall() {
			return getRuleContext(FunctionCallContext.class,0);
		}
		public FuncValueContext(ValueContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlWhereListener ) ((SqlWhereListener)listener).enterFuncValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlWhereListener ) ((SqlWhereListener)listener).exitFuncValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlWhereVisitor ) return ((SqlWhereVisitor<? extends T>)visitor).visitFuncValue(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StringValueContext extends ValueContext {
		public TerminalNode STRING() { return getToken(SqlWhereParser.STRING, 0); }
		public StringValueContext(ValueContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlWhereListener ) ((SqlWhereListener)listener).enterStringValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlWhereListener ) ((SqlWhereListener)listener).exitStringValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlWhereVisitor ) return ((SqlWhereVisitor<? extends T>)visitor).visitStringValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ValueContext value() throws RecognitionException {
		ValueContext _localctx = new ValueContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_value);
		try {
			setState(82);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case DQIDENT:
				_localctx = new FieldValueContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(71);
				match(DQIDENT);
				}
				break;
			case NUMBER:
				_localctx = new NumberValueContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(72);
				match(NUMBER);
				}
				break;
			case STRING:
				_localctx = new StringValueContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(73);
				match(STRING);
				}
				break;
			case IDENT:
				_localctx = new FuncValueContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(74);
				functionCall();
				}
				break;
			case CAST:
				_localctx = new CastValueContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(75);
				match(CAST);
				setState(76);
				match(LPAREN);
				setState(77);
				value();
				setState(78);
				match(AS);
				setState(79);
				match(IDENT);
				setState(80);
				match(RPAREN);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FunctionCallContext extends ParserRuleContext {
		public FunctionCallContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionCall; }
	 
		public FunctionCallContext() { }
		public void copyFrom(FunctionCallContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class FuncCallContext extends FunctionCallContext {
		public List<TerminalNode> IDENT() { return getTokens(SqlWhereParser.IDENT); }
		public TerminalNode IDENT(int i) {
			return getToken(SqlWhereParser.IDENT, i);
		}
		public TerminalNode LPAREN() { return getToken(SqlWhereParser.LPAREN, 0); }
		public FunctionArgsContext functionArgs() {
			return getRuleContext(FunctionArgsContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(SqlWhereParser.RPAREN, 0); }
		public FuncCallContext(FunctionCallContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlWhereListener ) ((SqlWhereListener)listener).enterFuncCall(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlWhereListener ) ((SqlWhereListener)listener).exitFuncCall(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlWhereVisitor ) return ((SqlWhereVisitor<? extends T>)visitor).visitFuncCall(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionCallContext functionCall() throws RecognitionException {
		FunctionCallContext _localctx = new FunctionCallContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_functionCall);
		int _la;
		try {
			_localctx = new FuncCallContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(84);
			match(IDENT);
			setState(89);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__0) {
				{
				{
				setState(85);
				match(T__0);
				setState(86);
				match(IDENT);
				}
				}
				setState(91);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(92);
			match(LPAREN);
			setState(93);
			functionArgs();
			setState(94);
			match(RPAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FunctionArgsContext extends ParserRuleContext {
		public List<ValueContext> value() {
			return getRuleContexts(ValueContext.class);
		}
		public ValueContext value(int i) {
			return getRuleContext(ValueContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(SqlWhereParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SqlWhereParser.COMMA, i);
		}
		public FunctionArgsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionArgs; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlWhereListener ) ((SqlWhereListener)listener).enterFunctionArgs(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlWhereListener ) ((SqlWhereListener)listener).exitFunctionArgs(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlWhereVisitor ) return ((SqlWhereVisitor<? extends T>)visitor).visitFunctionArgs(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionArgsContext functionArgs() throws RecognitionException {
		FunctionArgsContext _localctx = new FunctionArgsContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_functionArgs);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(104);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 31461376L) != 0)) {
				{
				setState(96);
				value();
				setState(101);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(97);
					match(COMMA);
					setState(98);
					value();
					}
					}
					setState(103);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CmpOpContext extends ParserRuleContext {
		public CmpOpContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_cmpOp; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SqlWhereListener ) ((SqlWhereListener)listener).enterCmpOp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SqlWhereListener ) ((SqlWhereListener)listener).exitCmpOp(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SqlWhereVisitor ) return ((SqlWhereVisitor<? extends T>)visitor).visitCmpOp(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CmpOpContext cmpOp() throws RecognitionException {
		CmpOpContext _localctx = new CmpOpContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_cmpOp);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(106);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 508L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 1:
			return expression_sempred((ExpressionContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean expression_sempred(ExpressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 5);
		case 1:
			return precpred(_ctx, 4);
		}
		return true;
	}

	public static final String _serializedATN =
		"\u0004\u0001\u0019m\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0001\u0000\u0001\u0000\u0001"+
		"\u0000\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0003\u0001\u001a\b\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0005\u0001\"\b"+
		"\u0001\n\u0001\f\u0001%\t\u0001\u0001\u0002\u0001\u0002\u0001\u0002\u0001"+
		"\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001"+
		"\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001"+
		"\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001"+
		"\u0002\u0001\u0002\u0001\u0002\u0005\u0002?\b\u0002\n\u0002\f\u0002B\t"+
		"\u0002\u0001\u0002\u0001\u0002\u0003\u0002F\b\u0002\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0003\u0003S\b\u0003\u0001"+
		"\u0004\u0001\u0004\u0001\u0004\u0005\u0004X\b\u0004\n\u0004\f\u0004[\t"+
		"\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0005\u0001"+
		"\u0005\u0001\u0005\u0005\u0005d\b\u0005\n\u0005\f\u0005g\t\u0005\u0003"+
		"\u0005i\b\u0005\u0001\u0006\u0001\u0006\u0001\u0006\u0000\u0001\u0002"+
		"\u0007\u0000\u0002\u0004\u0006\b\n\f\u0000\u0001\u0001\u0000\u0002\bv"+
		"\u0000\u000e\u0001\u0000\u0000\u0000\u0002\u0019\u0001\u0000\u0000\u0000"+
		"\u0004E\u0001\u0000\u0000\u0000\u0006R\u0001\u0000\u0000\u0000\bT\u0001"+
		"\u0000\u0000\u0000\nh\u0001\u0000\u0000\u0000\fj\u0001\u0000\u0000\u0000"+
		"\u000e\u000f\u0003\u0002\u0001\u0000\u000f\u0010\u0005\u0000\u0000\u0001"+
		"\u0010\u0001\u0001\u0000\u0000\u0000\u0011\u0012\u0006\u0001\uffff\uffff"+
		"\u0000\u0012\u0013\u0005\n\u0000\u0000\u0013\u001a\u0003\u0002\u0001\u0003"+
		"\u0014\u001a\u0003\u0004\u0002\u0000\u0015\u0016\u0005\u0012\u0000\u0000"+
		"\u0016\u0017\u0003\u0002\u0001\u0000\u0017\u0018\u0005\u0013\u0000\u0000"+
		"\u0018\u001a\u0001\u0000\u0000\u0000\u0019\u0011\u0001\u0000\u0000\u0000"+
		"\u0019\u0014\u0001\u0000\u0000\u0000\u0019\u0015\u0001\u0000\u0000\u0000"+
		"\u001a#\u0001\u0000\u0000\u0000\u001b\u001c\n\u0005\u0000\u0000\u001c"+
		"\u001d\u0005\u0010\u0000\u0000\u001d\"\u0003\u0002\u0001\u0006\u001e\u001f"+
		"\n\u0004\u0000\u0000\u001f \u0005\u0011\u0000\u0000 \"\u0003\u0002\u0001"+
		"\u0005!\u001b\u0001\u0000\u0000\u0000!\u001e\u0001\u0000\u0000\u0000\""+
		"%\u0001\u0000\u0000\u0000#!\u0001\u0000\u0000\u0000#$\u0001\u0000\u0000"+
		"\u0000$\u0003\u0001\u0000\u0000\u0000%#\u0001\u0000\u0000\u0000&\'\u0003"+
		"\u0006\u0003\u0000\'(\u0005\t\u0000\u0000()\u0005\u000f\u0000\u0000)F"+
		"\u0001\u0000\u0000\u0000*+\u0003\u0006\u0003\u0000+,\u0005\t\u0000\u0000"+
		",-\u0005\n\u0000\u0000-.\u0005\u000f\u0000\u0000.F\u0001\u0000\u0000\u0000"+
		"/0\u0003\u0006\u0003\u000001\u0003\f\u0006\u000012\u0003\u0006\u0003\u0000"+
		"2F\u0001\u0000\u0000\u00003F\u0003\b\u0004\u000045\u0003\u0006\u0003\u0000"+
		"56\u0005\r\u0000\u000067\u0005\u0016\u0000\u00007F\u0001\u0000\u0000\u0000"+
		"89\u0003\u0006\u0003\u00009:\u0005\u000e\u0000\u0000:;\u0005\u0012\u0000"+
		"\u0000;@\u0003\u0006\u0003\u0000<=\u0005\u0014\u0000\u0000=?\u0003\u0006"+
		"\u0003\u0000><\u0001\u0000\u0000\u0000?B\u0001\u0000\u0000\u0000@>\u0001"+
		"\u0000\u0000\u0000@A\u0001\u0000\u0000\u0000AC\u0001\u0000\u0000\u0000"+
		"B@\u0001\u0000\u0000\u0000CD\u0005\u0013\u0000\u0000DF\u0001\u0000\u0000"+
		"\u0000E&\u0001\u0000\u0000\u0000E*\u0001\u0000\u0000\u0000E/\u0001\u0000"+
		"\u0000\u0000E3\u0001\u0000\u0000\u0000E4\u0001\u0000\u0000\u0000E8\u0001"+
		"\u0000\u0000\u0000F\u0005\u0001\u0000\u0000\u0000GS\u0005\u0015\u0000"+
		"\u0000HS\u0005\u0017\u0000\u0000IS\u0005\u0016\u0000\u0000JS\u0003\b\u0004"+
		"\u0000KL\u0005\f\u0000\u0000LM\u0005\u0012\u0000\u0000MN\u0003\u0006\u0003"+
		"\u0000NO\u0005\u000b\u0000\u0000OP\u0005\u0018\u0000\u0000PQ\u0005\u0013"+
		"\u0000\u0000QS\u0001\u0000\u0000\u0000RG\u0001\u0000\u0000\u0000RH\u0001"+
		"\u0000\u0000\u0000RI\u0001\u0000\u0000\u0000RJ\u0001\u0000\u0000\u0000"+
		"RK\u0001\u0000\u0000\u0000S\u0007\u0001\u0000\u0000\u0000TY\u0005\u0018"+
		"\u0000\u0000UV\u0005\u0001\u0000\u0000VX\u0005\u0018\u0000\u0000WU\u0001"+
		"\u0000\u0000\u0000X[\u0001\u0000\u0000\u0000YW\u0001\u0000\u0000\u0000"+
		"YZ\u0001\u0000\u0000\u0000Z\\\u0001\u0000\u0000\u0000[Y\u0001\u0000\u0000"+
		"\u0000\\]\u0005\u0012\u0000\u0000]^\u0003\n\u0005\u0000^_\u0005\u0013"+
		"\u0000\u0000_\t\u0001\u0000\u0000\u0000`e\u0003\u0006\u0003\u0000ab\u0005"+
		"\u0014\u0000\u0000bd\u0003\u0006\u0003\u0000ca\u0001\u0000\u0000\u0000"+
		"dg\u0001\u0000\u0000\u0000ec\u0001\u0000\u0000\u0000ef\u0001\u0000\u0000"+
		"\u0000fi\u0001\u0000\u0000\u0000ge\u0001\u0000\u0000\u0000h`\u0001\u0000"+
		"\u0000\u0000hi\u0001\u0000\u0000\u0000i\u000b\u0001\u0000\u0000\u0000"+
		"jk\u0007\u0000\u0000\u0000k\r\u0001\u0000\u0000\u0000\t\u0019!#@ERYeh";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}