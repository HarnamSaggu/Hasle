package lang

import lang.Type.*

fun simplifyOperators(tokens: List<Token>): List<Token> {
	val groups = expressionToGroups(tokens)
	val groupsUnary = replaceUnaryOperators(groups)
	val groupsUnaryBinary = replaceBinaryOperator(groupsUnary)

	return if (groupsUnaryBinary.isEmpty()) {
		listOf()
	} else {
		groupsUnaryBinary[0].unravel()
	}
}

private fun expressionToGroups(tokens: List<Token>): List<Group> {
	val groups = mutableListOf<Group>()

	var index = 0
	val accumulator = mutableListOf<Token>()
	while (index < tokens.size) {
		val token = tokens[index]

		when {
			token.isUnaryOperator() -> {
				if (accumulator.isNotEmpty()) {
					groups.add(Bundle(accumulator.toList()))
					accumulator.clear()
				}
				groups.add(UnaryOperator(token.type))
			}

			token.isBinaryOperator() -> {
				groups.add(Bundle(accumulator.toList()))
				accumulator.clear()
				groups.add(BinaryOperator(token.type))
			}

			token.isOpenBracket() -> {
				accumulator.add(token)

				index++
				var count = 1
				while (index < tokens.size && count != 0) {
					val currentToken = tokens[index]

					if (currentToken.isOpenBracket()) {
						count++
					} else if (currentToken.isCloseBracket()) {
						count--
					}

					accumulator.add(currentToken)

					if (count != 0) {
						index++
					}
				}
			}

			else -> {
				accumulator.add(token)
			}
		}

		index++
	}
	if (accumulator.isNotEmpty()) {
		groups.add(Bundle(accumulator.toList()))
	}

	return groups
}

private fun replaceUnaryOperators(groups: List<Group>): List<Group> {
	val newGroups = mutableListOf<Group>()
	val accumulator = mutableListOf<Group>()
	for (group in groups) {
		if (group is BinaryOperator) {
			newGroups.add(reduceUnaryBlock(accumulator.reversed()))
			newGroups.add(group)
			accumulator.clear()
		} else {
			accumulator.add(group)
		}
	}
	if (accumulator.isNotEmpty()) {
		newGroups.add(reduceUnaryBlock(accumulator.reversed()))
	}

	return newGroups
}

fun reduceUnaryBlock(blocks: List<Group>): Group {
	var finalOperation = blocks.first()
	for (index in 1..<blocks.size) {
		val nextOperation = blocks[index] as UnaryOperator
		nextOperation.expression = finalOperation
		finalOperation = nextOperation
	}

	return finalOperation
}

private fun replaceBinaryOperator(groups: List<Group>): List<Group> =
	when {
		groups.isEmpty() -> listOf()
		groups.size == 1 -> groups

		else -> {
			var winner = 1
			var winnerPriority = -1
			var index = 1
			while (index < groups.size) {
				val group = groups[index]
				if (group is BinaryOperator) {
					if (group.priority() > winnerPriority) {
						winner = index
						winnerPriority = group.priority()
					}
				} else {
					throw Error("Missing Binary operator: $groups")
				}
				index += 2
			}

			val operator = groups[winner] as BinaryOperator
			operator.expression0 = groups[winner - 1]
			operator.expression1 = groups[winner + 1]
			replaceBinaryOperator(
				groups.take(winner - 1) +
				operator +
				groups.subList(winner + 2, groups.size)
			)
		}
	}

abstract class Group {
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

class UnaryOperator(private val operator: Type, var expression: Group? = null) : Group() {
	override fun unravel(): List<Token> =
		if (expression == null) {
			listOf()
		} else {
			listOf(Token(NAME, operator.getNameOfMethod()), Token(OPEN)) +
			expression!!.unravel() +
			listOf(Token(CLOSE))
		}

	override fun toString(): String {
		return "(unary).{${operator.getNameOfMethod()}}.{$expression}"
	}
}

class BinaryOperator(
	val operator: Type,
	var expression0: Group? = null,
	var expression1: Group? = null
) : Group() {
	override fun unravel(): List<Token> =
		if (expression0 == null || expression1 == null) {
			listOf()
		} else {
			listOf(Token(NAME, operator.getNameOfMethod()), Token(OPEN)) +
			expression0!!.unravel() +
			listOf(Token(COMMA)) +
			expression1!!.unravel() +
			listOf(Token(CLOSE))
		}

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

private val unaryOperators = listOf(NOT, COPY)

private fun Token.isUnaryOperator(): Boolean =
	unaryOperators.contains(type)

private fun BinaryOperator.priority(): Int =
	when (operator) {
		POWER -> 11
		DIVIDE -> 10
		MULTIPLY -> 9
		MINUS -> 8
		PLUS -> 7
		MOD -> 6

		GREATER -> 3
		LESS -> 3
		GREATER_EQUALS -> 3
		LESS_EQUALS -> 3
		EQUALS -> 3
		NOT_EQUALS -> 3

		AND -> 2
		OR -> 1

		else -> -1
	}
