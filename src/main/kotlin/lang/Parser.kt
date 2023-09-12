package lang

import lang.Type.*
import java.math.BigDecimal
import java.math.BigInteger

fun parse(tokens: List<Token>): Pair<MainMethod, List<MethodDeclaration>> {
	var mainMethod = MainMethod(listOf())
	val methodDeclarations = mutableListOf<MethodDeclaration>()

	var index = 0
	while (index < tokens.size) {
		val token = tokens[index]

		if (index + 1 < tokens.size && token.type == FUN && tokens[index + 1].type == MAIN) {
			val section = with(collectSection(tokens, index + 6)) {
				index = second
				first
			}

			mainMethod = MainMethod(
				itemiseTokens(section).map {
					parseItem(it)
				}
			)
		} else if (token.type == FUN || token.type == STRUCT) {
			val name = tokens[index + 1].value
			index += 3

			val parameterNames = mutableListOf<String>()
			while (index < tokens.size && tokens[index].type != CLOSE) {
				if (tokens[index].type == NAME) {
					parameterNames.add(tokens[index].value)
				}

				index++
			}

			val section = with(collectSection(tokens, index + 2)) {
				index = second
				first
			}
			val items = itemiseTokens(section).toMutableList()

			if (token.type == FUN) {
				val returnStatement: Command = if (
					items.isNotEmpty() &&
					items.last().isNotEmpty() &&
					items.last().first().type == RETURN
				) {
					parseItem(items.last()).also { items.removeLast() }
				} else {
					0.toValue()
				}

				methodDeclarations.add(
					MethodDeclaration(
						name,
						parameterNames.toList(),
						items.map { parseItem(it) },
						returnStatement
					)
				)
			} else {
				val fields = mutableMapOf<String, Command>()
				items.forEach {
					fields[it.first().value] = parseExpression(if (it.size == 1) it else it.drop(2))
				}

				methodDeclarations.add(
					StructDeclaration(
						name,
						parameterNames.toList(),
						fields.toMap()
					)
				)
			}
		}

		index++
	}

	return mainMethod to methodDeclarations
}

private fun parseItem(unsimplifiedTokens: List<Token>): Command {
	val tokens = expandSyntacticSugar(unsimplifiedTokens)
	val first = tokens.first()
	return when (first.type) {
		IF, WHILE -> {
			var index = 2
			val booleanTokens = with(collectSection(tokens, index, OPEN)) {
				index = second + 1
				this.first
			}
			val booleanExpression = parseExpression(booleanTokens.toList())

			val firstSectionTokens = with(collectSection(tokens, index + 1)) {
				index = this.second
				this.first
			}
			val firstSectionItems = itemiseTokens(firstSectionTokens)
			val firstSection = firstSectionItems.map { parseItem(it) }

			if (first.type == IF) {
				val secondSection = if (index + 1 < tokens.size && tokens[index + 1].type == ELSE) {
					val secondSectionTokens = tokens.dropLast(1).drop(index + 3)
					val secondSectionItems = itemiseTokens(secondSectionTokens)
					secondSectionItems.map { parseItem(it) }
				} else {
					listOf()
				}

				If(booleanExpression, firstSection, secondSection)
			} else {
				While(booleanExpression, firstSection)
			}
		}

		GLOBAL -> parseAssignment(tokens)

		NAME -> when {
			tokens.size > 2 -> {
				when {
					tokens.contains(Token(ASSIGN)) -> {
						parseAssignment(tokens)
					}

					tokens[1].type == OPEN || tokens.last().type == CLOSE -> {
						parseExpression(tokens)
					}

					else -> throw Error(
						"Unrecognised Item: ${reproduceSourceCode(tokens)}" +
						"\tInstance of Type.NAME not followed by appropriate tokens"
					)
				}
			}

			else -> throw Error("Unrecognised Item: ${reproduceSourceCode(tokens)}")
		}

		RETURN -> parseExpression(tokens.drop(1))

		else -> throw Error("Unrecognised Item: ${reproduceSourceCode(tokens)}")
	}
}

private val assign = listOf(Token(ASSIGN))
private val open = listOf(Token(OPEN))
private val close = listOf(Token(CLOSE))

private fun expandSyntacticSugar(tokens: List<Token>): List<Token> {
	val types = tokens.map { it.type }
	var indexOfComplexAssignment = -1
	Type.entries.filter { Token(it).isBinaryOperator() }.forEach {
		val indexOfOperator = types.indexOf(it)
		if (indexOfOperator != -1 && indexOfOperator + 1 < types.size && types[indexOfOperator + 1] == ASSIGN) {
			indexOfComplexAssignment = indexOfOperator
		}
	}

	var simplifiedTokens = tokens
	val first = tokens.first()
	if (first.type == NAME) {
		if (indexOfComplexAssignment != -1) {
			val variableReferenceTokens = tokens.take(indexOfComplexAssignment)
			val valueTokens = tokens.subList(indexOfComplexAssignment + 2, tokens.size)
			val assignmentOperator = tokens[indexOfComplexAssignment]

			simplifiedTokens = variableReferenceTokens +
			                   assign +
			                   variableReferenceTokens +
			                   listOf(assignmentOperator) +
			                   open +
			                   valueTokens +
			                   close
		} else if (tokens.last().type == INCREMENT || tokens.last().type == DECREMENT) {
			val operator = listOf(Token(if (tokens.last().type == INCREMENT) PLUS else MINUS))
			simplifiedTokens = tokens.dropLast(1) +
			                   assign +
			                   tokens.dropLast(1) +
			                   operator +
			                   listOf(Token(INTEGER, "1"))
		}
	} else if (first.type == GLOBAL && indexOfComplexAssignment != -1) {
		val variableReferenceTokens = tokens.subList(1, indexOfComplexAssignment)
		val valueTokens = tokens.subList(indexOfComplexAssignment + 2, tokens.size)
		val assignmentOperator = tokens[indexOfComplexAssignment]

		simplifiedTokens = listOf(first) +
		                   variableReferenceTokens +
		                   assign +
		                   variableReferenceTokens +
		                   listOf(assignmentOperator) +
		                   open +
		                   valueTokens +
		                   close
	}

	return simplifiedTokens
}

private fun parseAssignment(tokens: List<Token>): Assignment {
	val isGlobal = tokens.first().type == GLOBAL
	val index = if (isGlobal) 1 else 0
	val assignIndex = tokens.map { it.type }.indexOf(ASSIGN)
	val variableReference = parseVariableReference(tokens.subList(index, assignIndex))
	val value = parseExpression(tokens.drop(assignIndex + 1))

	return Assignment(
		variableReference,
		value,
		isGlobal
	)
}

private fun parseVariableReference(tokens: List<Token>): Reference {
	val name = tokens.first().value

	var index = 1
	val accessors = mutableListOf<Accessor>()
	while (index < tokens.size && tokens[index].type != ASSIGN) {
		val token = tokens[index]

		if (token.type == GET && index + 1 < tokens.size) {
			accessors.add(
				Property(tokens[index + 1].value)
			)
		} else if (token.type == OPEN_S) {
			val indexTokens = with(collectSection(tokens, index + 1, OPEN_S)) {
				index = second
				first
			}
			accessors.add(
				Index(parseExpression(indexTokens))
			)
		}

		index++
	}

	return Reference(name, accessors.toList())
}

private fun parseExpression(unsimplifiedTokens: List<Token>): Command {
	val tokens = simplifyExpressionTokens(unsimplifiedTokens)

	val first = tokens[0]
	return if (first.type == NAME) {
		if (tokens.size >= 3 && tokens[1].type == OPEN) {
			MethodCall(
				first.value,
				collectArguments(tokens, 2).first.map {
					parseExpression(it)
				}
			)
		} else {
			parseVariableReference(tokens)
		}
	} else {
		when (first.type) {
			STRING -> Value(first.value)
			CHAR -> Value(first.value[0])
			INTEGER -> Value(BigInteger(first.value))
			DECIMAL -> Value(BigDecimal(first.value))
			TRUE -> 1.toValue()
			FALSE -> 0.toValue()

			else -> {
				if (first.type == OPEN_C) {
					if (tokens.last().type == CLOSE_C) {
						val elements = collectArguments(tokens, 1).first
						DefinedList(
							if (elements.isEmpty()) {
								listOf()
							} else {
								elements.map { parseExpression(it) }
							}
						)
					} else if (tokens[1].type == CLOSE_C && tokens[2].type == OPEN_S && tokens.last().type == CLOSE_S) {
						val indexTokens = tokens.drop(3).dropLast(1)

						val index = parseExpression(indexTokens)

						return SizedList(index)
					} else {
						throw Error("Unrecognised Expression: ${reproduceSourceCode(tokens)}")
					}
				} else if (first.type == OPEN && tokens.last().type == CLOSE) {
					return parseExpression(tokens.drop(1).dropLast(1))
				} else {
					throw Error("Unrecognised Expression: ${reproduceSourceCode(tokens)}")
				}
			}
		}
	}
}

private fun simplifyExpressionTokens(unsimplifiedTokens: List<Token>): List<Token> {
	val (head, tails) = itemiseExpressionTokens(simplifyOperators(unsimplifiedTokens))
	var tokens = head
	tails.forEach {
		when {
			it.size >= 3 && it[0].type == NAME && it[1].type == OPEN && it.last().type == CLOSE -> {
				val comma = if (it.size - 3 > 0) listOf(Token(COMMA)) else listOf()
				tokens = listOf(it[0], it[1]) +
				         tokens +
				         comma +
				         it.drop(2)
			}

			it.size == 1 && it[0].type == NAME -> {
				tokens = listOf(Token(NAME, "get"), Token(OPEN)) +
				         tokens +
				         listOf(Token(COMMA), Token(STRING, it[0].value), Token(CLOSE))
			}

			it.size >= 3 && it[0].type == OPEN_S && it.last().type == CLOSE_S -> {
				tokens = listOf(Token(NAME, "at"), Token(OPEN)) +
				         tokens +
				         listOf(Token(COMMA)) +
				         it.drop(1).dropLast(1) +
				         listOf(Token(CLOSE))
			}
		}
	}

	return tokens.toList()
}

private fun itemiseExpressionTokens(tokens: List<Token>): Pair<List<Token>, List<List<Token>>> {
	val head = mutableListOf<Token>()
	val tails = mutableListOf<List<Token>>()
	val accumulator = mutableListOf<Token>()

	fun appendAccumulator() {
		if (head.isEmpty()) {
			head.addAll(accumulator.toList())
		} else {
			if (
				head[0].type == NAME &&
				tails.isEmpty() &&
				!(accumulator.size > 1 && accumulator[1].type == OPEN)
			) {
				if (accumulator[0].type == NAME) {
					head.add(Token(GET))
				}
				head.addAll(accumulator.toList())
			} else {
				tails.add(accumulator.toList())
			}
		}
		accumulator.clear()
	}

	var index = 0
	while (index < tokens.size) {
		val token = tokens[index]

		when (token.type) {
			GET -> appendAccumulator()

			OPEN, OPEN_C, OPEN_S -> {
				if (token.type == OPEN_S && index > 0 && tokens[index - 1].type != CLOSE_C) {
					appendAccumulator()
				}

				accumulator.add(token)
				accumulator.addAll(
					with(collectSection(tokens, index + 1, token.type)) {
						index = second
						first
					}
				)
				accumulator.add(tokens[index])
			}

			else -> {
				accumulator.add(token)
			}
		}

		index++
	}
	appendAccumulator()

	return head.toList() to tails.toList()
}

private fun simplifyOperators(tokens: List<Token>): List<Token> {
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

private fun collectSection(
	tokens: List<Token>,
	startIndex: Int,
	openType: Type = OPEN_C,
): Pair<List<Token>, Int> {
	val closeType = when (openType) {
		OPEN -> CLOSE
		OPEN_S -> CLOSE_S
		else -> CLOSE_C
	}
	val section = mutableListOf<Token>()

	var index = startIndex
	var count = 1
	while (index < tokens.size && count > 0) {
		val token = tokens[index]

		if (token.type == openType) {
			count++
		} else if (token.type == closeType) {
			count--
		}

		if (count > 0) {
			section.add(token)
			index++
		} else if (count == 0) {
			return section.toList() to index
		}
	}

	throw Error("Missing curly bracket for section end: ${reproduceSourceCode(tokens)}")
}

private fun collectArguments(
	tokens: List<Token>,
	startIndex: Int
): Pair<List<List<Token>>, Int> {
	val arguments = mutableListOf<List<Token>>()

	var index = startIndex
	var count = 1
	val accumulator = mutableListOf<Token>()
	while (index < tokens.size) {
		val token = tokens[index]

		if (count == 1 && token.type == COMMA) {
			arguments.add(accumulator.toList())
			accumulator.clear()
		} else {
			if (token.isOpenBracket()) {
				count++
			} else if (token.isCloseBracket()) {
				count--

				if (count == 0) {
					if (accumulator.isNotEmpty()) {
						arguments.add(accumulator.toList())
					}
					return arguments.toList() to index
				}
			}

			accumulator.add(token)
		}

		index++
	}

	throw Error("Missing bracket for argument list end: ${reproduceSourceCode(tokens)}")
}

private fun itemiseTokens(tokens: List<Token>): List<List<Token>> {
	val items = mutableListOf<List<Token>>()

	var index = 0
	val accumulator = mutableListOf<Token>()
	while (index < tokens.size) {
		val token = tokens[index]

		when {
			accumulator.isEmpty() && (token.type == IF || token.type == WHILE) -> {
				accumulator.add(token)
				index++
				accumulator.add(tokens[index])
				val booleanTokens = with(collectSection(tokens, index + 1, OPEN)) {
					index = second
					first
				}
				accumulator.addAll(booleanTokens)
				accumulator.add(tokens[index])
				index++
				accumulator.add(tokens[index])
				val mainSection = with(collectSection(tokens, index + 1)) {
					index = second
					first
				}
				accumulator.addAll(mainSection)
				accumulator.add(tokens[index])

				if (
					index + 1 < tokens.size &&
					token.type == IF &&
					tokens[index + 1].type == ELSE
				) {
					accumulator.add(tokens[index + 1])
					accumulator.add(tokens[index + 2])

					accumulator.addAll(
						with(collectSection(tokens, index + 3)) {
							index = this.second
							this.first
						}
					)
					accumulator.add(tokens[index])
				}
			}

			accumulator.isNotEmpty() && token.type == LINE_SEPARATOR -> {
				items.add(accumulator.toList())
				accumulator.clear()
			}

			token.type != LINE_SEPARATOR -> {
				accumulator.add(token)
			}
		}

		index++
	}

	return items.toList()
}

private val openBrackets = listOf(OPEN, OPEN_C, OPEN_S)
private val closeBrackets = listOf(CLOSE, CLOSE_C, CLOSE_S)
private val unaryOperators = listOf(NOT, COPY)
private val binaryOperators = listOf(
	PLUS,
	MINUS,
	MULTIPLY,
	DIVIDE,
	MOD,
	POWER,
	EQUALS,
	NOT_EQUALS,
	GREATER,
	LESS,
	GREATER_EQUALS,
	LESS_EQUALS,
	AND,
	OR
)

private fun Token.isOpenBracket(): Boolean =
	openBrackets.contains(type)

private fun Token.isCloseBracket(): Boolean =
	closeBrackets.contains(type)

private fun Token.isBinaryOperator(): Boolean =
	binaryOperators.contains(type)

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
