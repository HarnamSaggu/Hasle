package lang

import java.io.File

fun main(args: Array<String>) {
	val sourceCode = File("programs/testCode.txt").readText()
	run(sourceCode, args.toList())
}
