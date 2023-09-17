package lang

import lang.Type.*

class Token(val type: Type, val value: String = "") {
	override fun toString(): String {
		return "[$type]${
			if (value.isNotBlank())
				" {$value}"
			else if (type == STRING) " {}"
			else ""
		}"
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as Token

		if (type != other.type) return false
		if (value != other.value) return false

		return true
	}

	override fun hashCode(): Int {
		var result = type.hashCode()
		result = 31 * result + value.hashCode()
		return result
	}
}

fun reproduceSourceCode(tokens: List<Token>): String =
	tokens.joinToString("") {
		it.type.symbol ?: when (it.type) {
			NAME -> it.value
			MAIN -> "main "
			WHILE -> "while "
			IF -> "if "
			ELSE -> "else "
			CLASS -> "class "
			FUN -> "fun "

			STRING -> "\"${it.value}\""
			CHAR -> "'${it.value}'"
			INTEGER -> it.value
			DECIMAL -> it.value
			TRUE -> "tue"
			FALSE -> "false"

			else -> "???"
		}
	}

private val openBrackets = listOf(OPEN, OPEN_C, OPEN_S)
private val closeBrackets = listOf(CLOSE, CLOSE_C, CLOSE_S)
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

fun Token.isOpenBracket(): Boolean =
	openBrackets.contains(type)

fun Token.isCloseBracket(): Boolean =
	closeBrackets.contains(type)

fun Token.isBinaryOperator(): Boolean =
	binaryOperators.contains(type)
