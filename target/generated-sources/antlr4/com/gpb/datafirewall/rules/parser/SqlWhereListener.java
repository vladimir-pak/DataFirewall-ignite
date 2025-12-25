// Generated from com/gpb/datafirewall/rules/parser/SqlWhere.g4 by ANTLR 4.13.1
package com.gpb.datafirewall.rules.parser;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link SqlWhereParser}.
 */
public interface SqlWhereListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link SqlWhereParser#parse}.
	 * @param ctx the parse tree
	 */
	void enterParse(SqlWhereParser.ParseContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlWhereParser#parse}.
	 * @param ctx the parse tree
	 */
	void exitParse(SqlWhereParser.ParseContext ctx);
	/**
	 * Enter a parse tree produced by the {@code AndExpr}
	 * labeled alternative in {@link SqlWhereParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterAndExpr(SqlWhereParser.AndExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code AndExpr}
	 * labeled alternative in {@link SqlWhereParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitAndExpr(SqlWhereParser.AndExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code PredicateOnly}
	 * labeled alternative in {@link SqlWhereParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterPredicateOnly(SqlWhereParser.PredicateOnlyContext ctx);
	/**
	 * Exit a parse tree produced by the {@code PredicateOnly}
	 * labeled alternative in {@link SqlWhereParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitPredicateOnly(SqlWhereParser.PredicateOnlyContext ctx);
	/**
	 * Enter a parse tree produced by the {@code NotExpr}
	 * labeled alternative in {@link SqlWhereParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterNotExpr(SqlWhereParser.NotExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code NotExpr}
	 * labeled alternative in {@link SqlWhereParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitNotExpr(SqlWhereParser.NotExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ParenExpr}
	 * labeled alternative in {@link SqlWhereParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterParenExpr(SqlWhereParser.ParenExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ParenExpr}
	 * labeled alternative in {@link SqlWhereParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitParenExpr(SqlWhereParser.ParenExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code OrExpr}
	 * labeled alternative in {@link SqlWhereParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterOrExpr(SqlWhereParser.OrExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code OrExpr}
	 * labeled alternative in {@link SqlWhereParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitOrExpr(SqlWhereParser.OrExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code IsNullExpr}
	 * labeled alternative in {@link SqlWhereParser#predicate}.
	 * @param ctx the parse tree
	 */
	void enterIsNullExpr(SqlWhereParser.IsNullExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code IsNullExpr}
	 * labeled alternative in {@link SqlWhereParser#predicate}.
	 * @param ctx the parse tree
	 */
	void exitIsNullExpr(SqlWhereParser.IsNullExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code IsNotNullExpr}
	 * labeled alternative in {@link SqlWhereParser#predicate}.
	 * @param ctx the parse tree
	 */
	void enterIsNotNullExpr(SqlWhereParser.IsNotNullExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code IsNotNullExpr}
	 * labeled alternative in {@link SqlWhereParser#predicate}.
	 * @param ctx the parse tree
	 */
	void exitIsNotNullExpr(SqlWhereParser.IsNotNullExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code CompareExpr}
	 * labeled alternative in {@link SqlWhereParser#predicate}.
	 * @param ctx the parse tree
	 */
	void enterCompareExpr(SqlWhereParser.CompareExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code CompareExpr}
	 * labeled alternative in {@link SqlWhereParser#predicate}.
	 * @param ctx the parse tree
	 */
	void exitCompareExpr(SqlWhereParser.CompareExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code FuncExpr}
	 * labeled alternative in {@link SqlWhereParser#predicate}.
	 * @param ctx the parse tree
	 */
	void enterFuncExpr(SqlWhereParser.FuncExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code FuncExpr}
	 * labeled alternative in {@link SqlWhereParser#predicate}.
	 * @param ctx the parse tree
	 */
	void exitFuncExpr(SqlWhereParser.FuncExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code LikeExpr}
	 * labeled alternative in {@link SqlWhereParser#predicate}.
	 * @param ctx the parse tree
	 */
	void enterLikeExpr(SqlWhereParser.LikeExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code LikeExpr}
	 * labeled alternative in {@link SqlWhereParser#predicate}.
	 * @param ctx the parse tree
	 */
	void exitLikeExpr(SqlWhereParser.LikeExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code InExpr}
	 * labeled alternative in {@link SqlWhereParser#predicate}.
	 * @param ctx the parse tree
	 */
	void enterInExpr(SqlWhereParser.InExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code InExpr}
	 * labeled alternative in {@link SqlWhereParser#predicate}.
	 * @param ctx the parse tree
	 */
	void exitInExpr(SqlWhereParser.InExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code FieldValue}
	 * labeled alternative in {@link SqlWhereParser#value}.
	 * @param ctx the parse tree
	 */
	void enterFieldValue(SqlWhereParser.FieldValueContext ctx);
	/**
	 * Exit a parse tree produced by the {@code FieldValue}
	 * labeled alternative in {@link SqlWhereParser#value}.
	 * @param ctx the parse tree
	 */
	void exitFieldValue(SqlWhereParser.FieldValueContext ctx);
	/**
	 * Enter a parse tree produced by the {@code NumberValue}
	 * labeled alternative in {@link SqlWhereParser#value}.
	 * @param ctx the parse tree
	 */
	void enterNumberValue(SqlWhereParser.NumberValueContext ctx);
	/**
	 * Exit a parse tree produced by the {@code NumberValue}
	 * labeled alternative in {@link SqlWhereParser#value}.
	 * @param ctx the parse tree
	 */
	void exitNumberValue(SqlWhereParser.NumberValueContext ctx);
	/**
	 * Enter a parse tree produced by the {@code StringValue}
	 * labeled alternative in {@link SqlWhereParser#value}.
	 * @param ctx the parse tree
	 */
	void enterStringValue(SqlWhereParser.StringValueContext ctx);
	/**
	 * Exit a parse tree produced by the {@code StringValue}
	 * labeled alternative in {@link SqlWhereParser#value}.
	 * @param ctx the parse tree
	 */
	void exitStringValue(SqlWhereParser.StringValueContext ctx);
	/**
	 * Enter a parse tree produced by the {@code FuncValue}
	 * labeled alternative in {@link SqlWhereParser#value}.
	 * @param ctx the parse tree
	 */
	void enterFuncValue(SqlWhereParser.FuncValueContext ctx);
	/**
	 * Exit a parse tree produced by the {@code FuncValue}
	 * labeled alternative in {@link SqlWhereParser#value}.
	 * @param ctx the parse tree
	 */
	void exitFuncValue(SqlWhereParser.FuncValueContext ctx);
	/**
	 * Enter a parse tree produced by the {@code CastValue}
	 * labeled alternative in {@link SqlWhereParser#value}.
	 * @param ctx the parse tree
	 */
	void enterCastValue(SqlWhereParser.CastValueContext ctx);
	/**
	 * Exit a parse tree produced by the {@code CastValue}
	 * labeled alternative in {@link SqlWhereParser#value}.
	 * @param ctx the parse tree
	 */
	void exitCastValue(SqlWhereParser.CastValueContext ctx);
	/**
	 * Enter a parse tree produced by the {@code FuncCall}
	 * labeled alternative in {@link SqlWhereParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void enterFuncCall(SqlWhereParser.FuncCallContext ctx);
	/**
	 * Exit a parse tree produced by the {@code FuncCall}
	 * labeled alternative in {@link SqlWhereParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void exitFuncCall(SqlWhereParser.FuncCallContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlWhereParser#functionArgs}.
	 * @param ctx the parse tree
	 */
	void enterFunctionArgs(SqlWhereParser.FunctionArgsContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlWhereParser#functionArgs}.
	 * @param ctx the parse tree
	 */
	void exitFunctionArgs(SqlWhereParser.FunctionArgsContext ctx);
	/**
	 * Enter a parse tree produced by {@link SqlWhereParser#cmpOp}.
	 * @param ctx the parse tree
	 */
	void enterCmpOp(SqlWhereParser.CmpOpContext ctx);
	/**
	 * Exit a parse tree produced by {@link SqlWhereParser#cmpOp}.
	 * @param ctx the parse tree
	 */
	void exitCmpOp(SqlWhereParser.CmpOpContext ctx);
}