package lang

enum class Type {
	NAME,               // variable name, method name etc.
	MAIN,               // main
	WHILE,              // while
	IF,                 // if
	ELSE,               // else
	STRUCT,             // struct

	CHAR,               // 'a', 'b', 'c'
	STRING,             // "hello world"
	INTEGER,            // 3_141_592
	DECIMAL,            // 3.141_592
	TRUE,               // true
	FALSE,              // false

	ASSIGN,             // =
	RETURN,             // <-
	GLOBAL,             // $
	PLUS_ASSIGN,        // +-
	MINUS_ASSIGN,       // -=
	MULTIPLY_ASSIGN,    // *=
	DIVIDE_ASSIGN,      // /=
	MOD_ASSIGN,         // %=
	POWER_ASSIGN,       // ^=

	PLUS,               // +
	MINUS,              // -
	MULTIPLY,           // *
	DIVIDE,             // /
	MOD,                // %
	POWER,              // ^
	INCREMENT,          // ++
	DECREMENT,          // --

	EQUALS,             // ==
	NOT_EQUALS,         // !=
	GREATER,            // >
	LESS,               // <
	GREATER_EQUALS,     // >=
	LESS_EQUALS,        // <=
	AND,                // &
	OR,                 // |

	NOT,                // !
	COPY,               // ~

	AT,                 // @
	GET,                // .

	OPEN,               // (
	CLOSE,              // )
	OPEN_C,             // {
	CLOSE_C,            // }
	OPEN_S,             // [
	CLOSE_S,            // ]

	COMMA,              // ,

	LINE_SEPARATOR,     // line break

	UNIDENTIFIED,       // anything not captured by types above
}
