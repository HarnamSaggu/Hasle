package lang

enum class Type(val symbol: String?) {
	NAME(null),	// variable name, method name etc.
	MAIN(null),	// main
	WHILE(null),	// while
	IF(null),	// if
	ELSE(null),	// else
	CLASS(null),	// class
	FUN(null),  // fun

	CHAR(null),	// 'a', 'b', 'c'
	STRING(null),	// "hello world"
	INTEGER(null),	// 3_141_592
	DECIMAL(null),	// 3.141_592
	TRUE(null),	// true
	FALSE(null),	// false

	ASSIGN("="),
	RETURN("<-"),
	GLOBAL("$"),

	PLUS("+"),
	MINUS("-"),
	MULTIPLY("*"),
	DIVIDE("/"),
	MOD("%"),
	POWER("^"),
	INCREMENT("++"),
	DECREMENT("--"),

	EQUALS("=="),
	NOT_EQUALS("!="),
	GREATER(">"),
	LESS("<"),
	GREATER_EQUALS(">="),
	LESS_EQUALS("<="),
	AND("&&"),
	OR("||"),

	NOT("!"),
	COPY("~"),

	GET("."),

	OPEN("("),
	CLOSE(")"),
	OPEN_C("{"),
	CLOSE_C("}"),
	OPEN_S("["),
	CLOSE_S("]"),

	COMMA(","),

	LINE_SEPARATOR("\n"),

	UNIDENTIFIED(null),	// anything not captured by types above
}
