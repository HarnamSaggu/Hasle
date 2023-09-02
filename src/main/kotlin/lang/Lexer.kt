package lang

import lang.Type.*

fun lex(sourceCode: String): List<Token> {
    val symbolMap = entries.filter { it.symbol != null }.associateBy { it.symbol }
    val tokens = mutableListOf<Token>()
    val length = sourceCode.length

    var index = 0
    while (index < length) {
        val currentChar = sourceCode[index]
        var value = ""
        var type = UNIDENTIFIED

        when {
            (currentChar != '\n' && currentChar.isWhitespace())
                    || (currentChar == '-' && index + 1 < length && sourceCode[index + 1].isDigit()) -> {
                index++
                continue
            }
            currentChar == '#' -> {
                while (index < length && sourceCode[index] != '\n') {
                    index++
                }
                continue
            }
        }

        val singleCharacterCheck = symbolMap[currentChar.toString()]
        if (index + 1 < length) {
            val nextChar = sourceCode[index + 1]
            val doubleCharacterCheck = symbolMap["$currentChar$nextChar"]
            type = if (doubleCharacterCheck != null) {
                index++
                doubleCharacterCheck
            } else singleCharacterCheck ?: type
        } else if (singleCharacterCheck != null) {
            type = singleCharacterCheck
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
            value = value.replace("_", "")
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
