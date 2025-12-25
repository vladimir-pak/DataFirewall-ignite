// Generated from com/gpb/datafirewall/rules/parser/SqlWhere.g4 by ANTLR 4.13.1
package com.gpb.datafirewall.rules.parser;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link SqlWhereParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface SqlWhereVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link SqlWhereParser#parse}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParse(SqlWhereParser.ParseContext ctx);
	/**
	 * Visit a parse tree produced by the {@code AndExpr}
	 * labeled alternative in {@link SqlWhereParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAndExpr(SqlWhereParser.AndExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code PredicateOnly}
	 * labeled alternative in {@link SqlWhereParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPredicateOnly(SqlWhereParser.PredicateOnlyContext ctx);
	/**
	 * Visit a parse tree produced by the {@code NotExpr}
	 * labeled alternative in {@link SqlWhereParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNotExpr(SqlWhereParser.NotExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ParenExpr}
	 * labeled alternative in {@link SqlWhereParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParenExpr(SqlWhereParser.ParenExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code OrExpr}
	 * labeled alternative in {@link SqlWhereParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrExpr(SqlWhereParser.OrExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code IsNullExpr}
	 * labeled alternative in {@link SqlWhereParser#predicate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIsNullExpr(SqlWhereParser.IsNullExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code IsNotNullExpr}
	 * labeled alternative in {@link SqlWhereParser#predicate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIsNotNullExpr(SqlWhereParser.IsNotNullExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code CompareExpr}
	 * labeled alternative in {@link SqlWhereParser#predicate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCompareExpr(SqlWhereParser.CompareExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code FuncExpr}
	 * labeled alternative in {@link SqlWhereParser#predicate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuncExpr(SqlWhereParser.FuncExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code LikeExpr}
	 * labeled alternative in {@link SqlWhereParser#predicate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLikeExpr(SqlWhereParser.LikeExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code InExpr}
	 * labeled alternative in {@link SqlWhereParser#predicate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInExpr(SqlWhereParser.InExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code FieldValue}
	 * labeled alternative in {@link SqlWhereParser#value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldValue(SqlWhereParser.FieldValueContext ctx);
	/**
	 * Visit a parse tree produced by the {@code NumberValue}
	 * labeled alternative in {@link SqlWhereParser#value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumberValue(SqlWhereParser.NumberValueContext ctx);
	/**
	 * Visit a parse tree produced by the {@code StringValue}
	 * labeled alternative in {@link SqlWhereParser#value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringValue(SqlWhereParser.StringValueContext ctx);
	/**
	 * Visit a parse tree produced by the {@code FuncValue}
	 * labeled alternative in {@link SqlWhereParser#value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuncValue(SqlWhereParser.FuncValueContext ctx);
	/**
	 * Visit a parse tree produced by the {@code CastValue}
	 * labeled alternative in {@link SqlWhereParser#value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCastValue(SqlWhereParser.CastValueContext ctx);
	/**
	 * Visit a parse tree produced by the {@code FuncCall}
	 * labeled alternative in {@link SqlWhereParser#functionCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuncCall(SqlWhereParser.FuncCallContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlWhereParser#functionArgs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionArgs(SqlWhereParser.FunctionArgsContext ctx);
	/**
	 * Visit a parse tree produced by {@link SqlWhereParser#cmpOp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCmpOp(SqlWhereParser.CmpOpContext ctx);
}