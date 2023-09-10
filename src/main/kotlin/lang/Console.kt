package lang

import java.io.File

fun main(args: Array<String>) {
	if (args[0] == "f") {
		run(File(args[1]).readText(), args.toList().drop(2))
	} else if (args[0] == "d") {
		run(File(args[1]), args.toList().drop(2))
	} else {
		throw Error(
			"Code scope not specified properly," +
			" 'f' - run only the given file," +
			" 'd' run the file using resources from parent directory"
		)
	}
}
