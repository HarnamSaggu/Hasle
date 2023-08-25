package lang

import lang.Type.*

fun lex(sourceCode: String): List<Token> {
	val tokens = mutableListOf<Token>()
	val length = sourceCode.length

	var index = 0
	while (index < length) {
		val currentChar = sourceCode[index]
		var value = ""

		var type = when (currentChar) {
			'$' -> GLOBAL
			'!' -> NOT
			'~' -> COPY
			'@' -> AT
			'.' -> GET
			'(' -> OPEN
			')' -> CLOSE
			'{' -> OPEN_C
			'}' -> CLOSE_C
			'[' -> OPEN_S
			']' -> CLOSE_S
			',' -> COMMA
			'\n' -> LINE_SEPARATOR

			else -> UNIDENTIFIED
		}

		if (index + 1 < length && currentChar == '&' && sourceCode[index + 1] == '&') {
			index++
			type = AND
		}

		if (index + 1 < length && currentChar == '|' && sourceCode[index + 1] == '|') {
			index++
			type = OR
		}

		if (currentChar == '=') {
			type = if (index + 1 < length && sourceCode[index + 1] == '=') {
				index++
				EQUALS
			} else {
				ASSIGN
			}
		}

		if (index + 1 < length && currentChar == '!' && sourceCode[index + 1] == '=') {
			index++
			type = NOT_EQUALS
		}

		if (currentChar == '>') {
			type = if (index + 1 < length && sourceCode[index + 1] == '=') {
				index++
				GREATER_EQUALS
			} else {
				GREATER
			}
		}

		if (currentChar == '<') {
			type = if (index + 1 < length && sourceCode[index + 1] == '=') {
				index++
				LESS_EQUALS
			} else {
				LESS
			}
		}

		if (type != LINE_SEPARATOR && currentChar.isWhitespace()) {
			index++
			continue
		}

		if (
			index + 1 < length &&
			currentChar == '<' &&
			sourceCode[index + 1] == '-'
		) {
			type = RETURN
			index++
		}

		if (currentChar == '+') {
			type = if (index + 1 < length && sourceCode[index + 1] == '+') {
				index++
				INCREMENT
			} else if (index + 1 < length && sourceCode[index + 1] == '=') {
				index++
				PLUS_ASSIGN
			} else {
				PLUS
			}
		}

		if (currentChar == '-') {
			type = when {
				index + 1 < length && sourceCode[index + 1].isDigit() -> {
					index++
					continue
				}

				index + 1 < length && sourceCode[index + 1] == '-' -> {
					index++
					DECREMENT
				}

				index + 1 < length && sourceCode[index + 1] == '=' -> {
					index++
					MINUS_ASSIGN
				}

				else -> MINUS
			}
		}

		if (currentChar == '*') {
			type = if (index + 1 < length && sourceCode[index + 1] == '=') {
				index++
				MULTIPLY_ASSIGN
			} else {
				MULTIPLY
			}
		}

		if (currentChar == '/') {
			type = if (index + 1 < length && sourceCode[index + 1] == '=') {
				index++
				DIVIDE_ASSIGN
			} else {
				DIVIDE
			}
		}

		if (currentChar == '%') {
			type = if (index + 1 < length && sourceCode[index + 1] == '=') {
				index++
				MOD_ASSIGN
			} else {
				MOD
			}
		}

		if (currentChar == '^') {
			type = if (index + 1 < length && sourceCode[index + 1] == '=') {
				index++
				POWER_ASSIGN
			} else {
				POWER
			}
		}

		if (currentChar == '#') {
			while (index < length && sourceCode[index] != '\n') {
				index++
			}
			continue
		}

		if (currentChar.isLetter()) {
			while (index < length && sourceCode[index].isLetterOrDigit()) {
				value += sourceCode[index]
				index++
			}
			index--

			type = when (value) {
				"main" -> MAIN
				"while" -> WHILE
				"if" -> IF
				"else" -> ELSE
				"struct" -> STRUCT

				"true" -> TRUE
				"false" -> FALSE

				else -> NAME
			}
		}

		if (index + 1 < length && currentChar == '"') {
			type = STRING

			index++
			while (index < length && sourceCode[index] != '"') {
				if (index + 2 < length && sourceCode[index] == '\\') {
					value += when (sourceCode[index + 1]) {
						't' -> "\t"
						'b' -> "\b"
						'n' -> "\n"
						'r' -> "\r"
						'\'' -> "\'"
						'"' -> "\""
						'\\' -> "\\"
						else -> {
							index--
							"\\"
						}
					}
					index++
				} else {
					value += sourceCode[index]
				}

				index++
			}
		}

		if (index + 2 < length && currentChar == '\'') {
			type = CHAR

			val character = sourceCode[index + 1]
			value = if (character == '\\' && index + 3 < length) {
				index++

				when (sourceCode[index + 2]) {
					't' -> "\t"
					'b' -> "\b"
					'n' -> "\n"
					'r' -> "\r"
					'\'' -> "\'"
					'"' -> "\""
					else -> "\\"
				}
			} else {
				character.toString()
			}

			index += 2
		}

		if (currentChar.isDigit()) {
			if (index > 0 && sourceCode[index - 1] == '-') {
				value = "-"
			}
			while (index < length && "${sourceCode[index]}".matches(Regex("[0-9._]"))) {
				value += sourceCode[index]
				index++
			}
			index--

			if (value.matches(Regex("-?[0-9][0-9_]*"))) {
				type = INTEGER
			} else if (value.matches(Regex("-?[0-9][0-9_]*\\.[0-9][0-9_]*"))) {
				type = DECIMAL
			}
		}

		if (type == UNIDENTIFIED) {
			throw Error("Unidentified Token at: $index ($currentChar)")
		}

		tokens.add(
			Token(type, value)
		)

		index++
	}

	return tokens.toList()
}
