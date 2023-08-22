package lang

import lang.Type.*

open class Group {
	open fun unravel(): List<Token> = listOf()
}

class Bundle(private val tokens: List<Token>) : Group() {
	override fun unravel(): List<Token> {
		return tokens
	}

	override fun toString(): String {
		return "(bundle).{$tokens}"
	}
}

class UnaryOperator(private val operator: Type, var expression: Group = Group()) : Group() {
	override fun unravel(): List<Token> =
		listOf(Token(NAME, operator.getNameOfMethod()), Token(OPEN)) +
				expression.unravel() +
				listOf(Token(CLOSE))

	override fun toString(): String {
		return "(unary).{${operator.getNameOfMethod()}}.{$expression}"
	}
}

class BinaryOperator(
	val operator: Type,
	var expression0: Group = Group(),
	var expression1: Group = Group()
) : Group() {
	override fun unravel(): List<Token> =
		listOf(Token(NAME, operator.getNameOfMethod()), Token(OPEN)) +
				expression0.unravel() +
				listOf(Token(COMMA)) +
				expression1.unravel() +
				listOf(Token(CLOSE))

	override fun toString(): String {
		return "(binary).{${operator.getNameOfMethod()}}.{$expression0}.{$expression1}"
	}
}

fun Type.getNameOfMethod(): String =
	when (this) {
		PLUS -> "add"
		MINUS -> "sub"
		MULTIPLY -> "mult"
		DIVIDE -> "div"
		MOD -> "mod"
		POWER -> "pow"
		AT -> "at"
		EQUALS -> "equals"
		NOT_EQUALS -> "notEquals"
		GREATER -> "greater"
		LESS -> "less"
		GREATER_EQUALS -> "greaterEquals"
		LESS_EQUALS -> "lessEquals"
		AND -> "and"
		OR -> "or"

		NOT -> "not"
		COPY -> "copy"

		else -> "unidentified"
	}
