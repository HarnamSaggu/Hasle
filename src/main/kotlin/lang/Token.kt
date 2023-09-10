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
}

fun reproduceSourceCode(tokens: List<Token>): String =
	tokens.joinToString("") {
		it.type.symbol ?: when (it.type) {
			NAME -> it.value
			MAIN -> "main"
			WHILE -> "while"
			IF -> "if"
			ELSE -> "else"
			STRUCT -> "struct "
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
