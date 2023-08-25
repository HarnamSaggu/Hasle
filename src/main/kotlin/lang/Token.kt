package lang

import lang.Type.*

class Token(val type: Type, val value: String = "") {
	override fun toString(): String {
		return "[$type]${
			if (value.isNotBlank())
				" {$value}"
			else
				if (type == STRING) " {}" else ""
		}"
	}
}

fun reproduceSourceCode(tokens: List<Token>): String =
	tokens.joinToString("") {
		when (it.type) {
			NAME -> it.value
			MAIN -> "main"
			WHILE -> "while"
			IF -> "if"
			ELSE -> "else"
			STRUCT -> "struct"

			STRING -> "\"${it.value}\""
			CHAR -> "'${it.value}'"
			INTEGER -> it.value
			DECIMAL -> it.value
			TRUE -> "tue"
			FALSE -> "false"

			ASSIGN -> "="
			RETURN -> "<-"
			GLOBAL -> "$"
			PLUS_ASSIGN -> "+="
			MINUS_ASSIGN -> "-="
			MULTIPLY_ASSIGN -> "*="
			DIVIDE_ASSIGN -> "/="
			MOD_ASSIGN -> "%="
			POWER_ASSIGN -> "^="

			PLUS -> "+"
			MINUS -> "-"
			MULTIPLY -> "*"
			DIVIDE -> "/"
			MOD -> "%"
			POWER -> "^"
			INCREMENT -> "++"
			DECREMENT -> "--"

			EQUALS -> "=="
			NOT_EQUALS -> "!="
			GREATER -> ">"
			LESS -> "<"
			GREATER_EQUALS -> ">="
			LESS_EQUALS -> "<="
			AND -> "&"
			OR -> "|"

			NOT -> "!"
			COPY -> "~"

			AT -> "@"
			GET -> "."

			OPEN -> "("
			CLOSE -> ")"
			OPEN_C -> "{"
			CLOSE_C -> "}"
			OPEN_S -> "["
			CLOSE_S -> "]"

			COMMA -> ","

			LINE_SEPARATOR -> "\n"

			UNIDENTIFIED -> "???"
		}
	}
