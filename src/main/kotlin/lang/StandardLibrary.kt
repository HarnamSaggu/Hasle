package lang

import org.nevec.rjm.BigDecimalMath.*
import java.io.File
import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.math.RoundingMode
import java.time.LocalDateTime
import kotlin.system.exitProcess

val zero = IntData(0)

open class StandardLibrary {
	val methods: Map<String, (List<Data>) -> Data> = mapOf(
		// MATHS
		"add" to ::add,
		"sub" to ::sub,
		"mult" to ::mult,
		"div" to ::div,
		"pow" to ::pow,
		"mod" to ::mod,
		"ln" to ::ln,
		"log" to ::log,
		"sqrt" to ::sqrt,
		"nthrt" to ::nthrt,
		"sin" to ::sin,
		"cos" to ::cos,
		"tan" to ::tan,
		"asin" to ::asin,
		"acos" to ::acos,
		"atan" to ::atan,
		"pi" to ::pi,
		"e" to ::e,
		"rand" to ::rand,
		"ceil" to ::ceil,
		"round" to ::roundData,
		"floor" to ::floor,
		"min" to ::min,
		"max" to ::max,

		// CONVERSIONS
		"dec" to ::dec,
		"int" to ::int,
		"char" to ::char,
		"bool" to ::bool,

		// LOGIC
		"equals" to ::equalsData,
		"notEquals" to ::notEquals,
		"greater" to ::greater,
		"less" to ::less,
		"greaterEquals" to ::greaterEquals,
		"lessEquals" to ::lessEquals,
		"not" to ::not,
		"and" to ::and,
		"or" to ::or,

		// STRING, LISTS
		"len" to ::len,
		"contains" to ::contains,
		"matches" to ::matches,
		"replaceFirst" to ::replaceFirst,
		"replace" to ::replace,
		"split" to ::split,
		"substring" to ::substring,
		"string" to ::string,
		"uppercase" to ::uppercase,
		"lowercase" to ::lowercase,
		"addAll" to ::addAll,
		"remove" to ::remove,
		"removeAt" to ::removeAt,
		"subList" to ::subList,
		"indexOf" to ::indexOf,

		// OBJECTS
		"class" to ::classData,
		"copy" to ::copyData,
		"set" to ::set,
		"get" to ::get,
		"at" to ::at,
		"fields" to ::fields,

		// I/O
		"readFile" to ::readFile,
		"writeFile" to ::writeFile,
		"readBytes" to ::readBytes,
		"writeBytes" to ::writeBytes,
		"time" to ::time,
		"dateTime" to ::dateTime,
		"readln" to ::input,
		"print" to ::printData,
		"println" to ::printlnData,
		"sleep" to ::sleep,
		"exit" to ::exit,
	)

	open fun add(args: List<Data>): Data {
		return if (args.size == 2) {
			val a = args[0]
			val b = args[1]

			when {
				a is IntData && b is IntData -> IntData(a.value + b.value)
				a is DecimalData && b is DecimalData -> DecimalData(a.value + b.value)
				a is ListData -> ListData((a.value + listOf(b)).toMutableList())
				a is StringData && b is StringData -> StringData(a.value + b.value)
				else -> throw ArgumentTypeError("add", args)
			}
		} else {
			throw ArgumentSizeError("add", args)
		}
	}

	open fun sub(args: List<Data>): Data {
		return if (args.size == 2) {
			val a = args[0]
			val b = args[1]
			if (a is IntData && b is IntData) {
				IntData(a.value - b.value)
			} else if (a is DecimalData && b is DecimalData) {
				DecimalData(a.value - b.value)
			} else {
				throw throw ArgumentTypeError("sub", args)
			}
		} else {
			throw ArgumentSizeError("sub", args)
		}
	}

	open fun mult(args: List<Data>): Data {
		return if (args.size == 2) {
			val a = args[0]
			val b = args[1]
			if (a is IntData && b is IntData) {
				IntData(a.value * b.value)
			} else if (a is DecimalData && b is DecimalData) {
				DecimalData(a.value * b.value)
			} else {
				throw throw ArgumentTypeError("mult", args)
			}
		} else {
			throw ArgumentSizeError("mult", args)
		}
	}

	open fun div(args: List<Data>): Data {
		return if (args.size == 2) {
			val a = args[0]
			val b = args[1]
			if (a is IntData && b is IntData) {
				IntData(a.value / b.value)
			} else if (a is DecimalData && b is DecimalData) {
				DecimalData(a.value.divide(b.value, 16, RoundingMode.HALF_UP))
			} else {
				throw throw ArgumentTypeError("div", args)
			}
		} else if (args.size == 3) {
			val a = args[0]
			val b = args[1]
			val c = args[2]
			if (a is DecimalData && b is DecimalData && c is IntData) {
				DecimalData(a.value.divide(b.value, c.value.toInt(), RoundingMode.HALF_UP))
			} else {
				throw ArgumentTypeError("div", args)
			}
		} else {
			throw ArgumentSizeError("div", args)
		}
	}

	open fun pow(args: List<Data>): Data {
		return if (args.size == 2) {
			val a = args[0]
			val b = args[1]
			if (a is IntData && b is IntData) {
				IntData(a.value.pow(b.value.toInt()))
			} else if (a is DecimalData && b is DecimalData) {
				DecimalData(pow(a.value, b.value))
			} else {
				throw ArgumentTypeError("pow", args)
			}
		} else {
			throw ArgumentSizeError("pow", args)
		}
	}

	open fun mod(args: List<Data>): Data {
		return if (args.size == 2) {
			val a = args[0]
			val b = args[1]
			if (a is IntData && b is IntData) {
				IntData(a.value.mod(b.value))
			} else {
				throw ArgumentTypeError("mod", args)
			}
		} else {
			throw ArgumentSizeError("mod", args)
		}
	}

	open fun ln(args: List<Data>): Data {
		return if (args.size == 1) {
			val a = args[0]
			if (a is DecimalData) {
				DecimalData(log(a.value))
			} else {
				throw ArgumentTypeError("ln", args)
			}
		} else {
			throw ArgumentSizeError("ln", args)
		}
	}

	open fun log(args: List<Data>): Data {
		return if (args.size == 2) {
			val a = args[0]
			val b = args[1]
			if (a is DecimalData && b is DecimalData) {
				DecimalData(log(a.value).divide(log(b.value), 16, RoundingMode.HALF_UP))
			} else {
				throw ArgumentTypeError("log", args)
			}
		} else {
			throw ArgumentSizeError("log", args)
		}
	}

	open fun sqrt(args: List<Data>): Data {
		return if (args.size == 1) {
			val a = args[0]
			if (a is DecimalData) {
				DecimalData(sqrt(a.value))
			} else {
				throw ArgumentTypeError("sqrt", args)
			}
		} else {
			throw ArgumentSizeError("sqrt", args)
		}
	}

	open fun nthrt(args: List<Data>): Data {
		return if (args.size == 2) {
			val a = args[0]
			val b = args[1]
			if (a is DecimalData && b is DecimalData) {
				DecimalData(root(b.value.toInt(), a.value))
			} else {
				throw ArgumentTypeError("nthrt", args)
			}
		} else {
			throw ArgumentSizeError("nthrt", args)
		}
	}

	open fun sin(args: List<Data>): Data {
		return if (args.size == 1) {
			val a = args[0]
			if (a is DecimalData) {
				DecimalData(sin(a.value))
			} else {
				throw ArgumentTypeError("sin", args)
			}
		} else {
			throw ArgumentSizeError("sin", args)
		}
	}

	open fun cos(args: List<Data>): Data {
		return if (args.size == 1) {
			val a = args[0]
			if (a is DecimalData) {
				DecimalData(cos(a.value))
			} else {
				throw ArgumentTypeError("cos", args)
			}
		} else {
			throw ArgumentSizeError("cos", args)
		}
	}

	open fun tan(args: List<Data>): Data {
		return if (args.size == 1) {
			val a = args[0]
			if (a is DecimalData) {
				DecimalData(tan(a.value))
			} else {
				throw ArgumentTypeError("tan", args)
			}
		} else {
			throw ArgumentSizeError("tan", args)
		}
	}

	open fun asin(args: List<Data>): Data {
		return if (args.size == 1) {
			val a = args[0]
			if (a is DecimalData) {
				DecimalData(asin(a.value))
			} else {
				throw ArgumentTypeError("asin", args)
			}
		} else {
			throw ArgumentSizeError("asin", args)
		}
	}

	open fun acos(args: List<Data>): Data {
		return if (args.size == 1) {
			val a = args[0]
			if (a is DecimalData) {
				DecimalData(acos(a.value))
			} else {
				throw ArgumentTypeError("acos", args)
			}
		} else {
			throw ArgumentSizeError("acos", args)
		}
	}

	open fun atan(args: List<Data>): Data {
		return if (args.size == 1) {
			val a = args[0]
			if (a is DecimalData) {
				DecimalData(atan(a.value))
			} else {
				throw ArgumentTypeError("atan", args)
			}
		} else {
			throw ArgumentSizeError("atan", args)
		}
	}

	open fun pi(args: List<Data>): Data {
		val length = if (args.size == 1 && args[0] is IntData) {
			(args[0] as IntData).value.toInt()
		} else {
			16
		}

		return DecimalData(pi(MathContext(length)))
	}

	open fun e(args: List<Data>): Data {
		val length = if (args.size == 1 && args[0] is IntData) {
			(args[0] as IntData).value.toInt()
		} else {
			16
		}

		return DecimalData(exp(MathContext(length)))
	}

	open fun rand(args: List<Data>): Data {
		if (args.isEmpty()) {
			return DecimalData(BigDecimal(Math.random()))
		} else {
			throw ArgumentSizeError("rand", args)
		}
	}

	open fun ceil(args: List<Data>): Data {
		return if (args.size == 1) {
			val a = args[0]
			if (a is DecimalData) {
				DecimalData(a.value.add(BigDecimal("0.5")).setScale(0, RoundingMode.HALF_UP))
			} else {
				throw ArgumentTypeError("ceil", args)
			}
		} else {
			throw ArgumentSizeError("ceil", args)
		}
	}

	open fun roundData(args: List<Data>): Data {
		return if (args.size == 1) {
			val a = args[0]
			if (a is DecimalData) {
				DecimalData(a.value.setScale(0, RoundingMode.HALF_UP))
			} else {
				throw ArgumentTypeError("round", args)
			}
		} else {
			throw ArgumentSizeError("round", args)
		}
	}

	open fun floor(args: List<Data>): Data {
		return if (args.size == 1) {
			val a = args[0]
			if (a is DecimalData) {
				DecimalData(a.value.subtract(BigDecimal("0.5")).setScale(0, RoundingMode.HALF_UP))
			} else {
				throw ArgumentTypeError("floor", args)
			}
		} else {
			throw ArgumentSizeError("floor", args)
		}
	}

	open fun min(args: List<Data>): Data {
		return if (args.size == 2) {
			val a = args[0]
			val b = args[1]
			if (a is IntData && b is IntData) {
				IntData(a.value.min(b.value))
			} else if (a is DecimalData && b is DecimalData) {
				DecimalData(a.value.min(b.value))
			} else {
				throw ArgumentTypeError("min", args)
			}
		} else {
			throw ArgumentSizeError("min", args)
		}
	}

	open fun max(args: List<Data>): Data {
		return if (args.size == 2) {
			val a = args[0]
			val b = args[1]
			if (a is IntData && b is IntData) {
				IntData(a.value.max(b.value))
			} else if (a is DecimalData && b is DecimalData) {
				DecimalData(a.value.max(b.value))
			} else {
				throw ArgumentTypeError("max", args)
			}
		} else {
			throw ArgumentSizeError("max", args)
		}
	}

	open fun dec(args: List<Data>): Data {
		return DecimalData(
			when {
				args.size == 2 && args[0] is DecimalData && args[1] is IntData -> {
					val a = args[0] as DecimalData
					val b = args[1] as IntData
					BigDecimal(a.value.toString(), MathContext(b.value.toInt(), RoundingMode.HALF_UP))
				}

				args.size == 1 && args[0] is IntData -> {
					val a = args[0] as IntData
					a.value.toBigDecimal()
				}

				args.size == 1 && args[0] is StringData -> {
					val a = args[0] as StringData
					BigDecimal(a.value)
				}

				else -> {
					throw ArgumentTypeError("dec", args)
				}
			}
		)
	}

	open fun int(args: List<Data>): Data {
		return if (args.size == 1) {
			IntData(
				when (val a = args[0]) {
					is DecimalData -> a.value.toBigInteger()
					is CharData -> a.value.code.toBigInteger()
					is StringData -> BigInteger(a.value)
					else -> throw ArgumentTypeError("int", args)
				}
			)
		} else {
			throw ArgumentSizeError("int", args)
		}
	}

	open fun char(args: List<Data>): Data {
		return if (args.size == 1 && args[0] is IntData) {
			val a = args[0] as IntData
			CharData(a.value.toInt().toChar())
		} else {
			throw ArgumentTypeError("char", args)
		}
	}

	open fun bool(args: List<Data>): Data {
		return if (args.size == 1 && args[0] is IntData) {
			val a = args[0] as IntData
			StringData(if (a.value <= zero.value) "false" else "true")
		} else {
			throw ArgumentTypeError("bool", args)
		}
	}

	open fun readFile(args: List<Data>): Data {
		return if (args.size == 1 && args[0] is StringData) {
			val a = args[0] as StringData
			StringData(File(a.value).readText())
		} else {
			throw ArgumentTypeError("readFile", args)
		}
	}

	open fun writeFile(args: List<Data>): Data {
		if (args.size == 2 && args[0] is StringData && args[1] is StringData) {
			val a = args[0] as StringData
			val b = args[1] as StringData
			File(a.value).writeText(b.value)
		} else {
			throw ArgumentTypeError("writeFile", args)
		}

		return zero
	}

	open fun readBytes(args: List<Data>): Data {
		return if (args.size == 1 && args[0] is StringData) {
			val a = args[0] as StringData
			ListData(File(a.value).readBytes().map { byte ->
				IntData(byte.toInt())
			}.toMutableList())
		} else {
			throw ArgumentTypeError("readBytes", args)
		}
	}

	open fun writeBytes(args: List<Data>): Data {
		if (args.size == 2 && args[0] is StringData && args[1] is ListData) {
			val a = args[0] as StringData
			val b = args[1] as ListData
			File(a.value).writeBytes(
				b.value.map { byte ->
					(byte as IntData).value.toString().toByte()
				}.toByteArray()
			)
		} else {
			throw ArgumentTypeError("writeBytes", args)
		}

		return zero
	}

	open fun time(args: List<Data>): Data {
		if (args.isEmpty()) {
			return IntData(System.currentTimeMillis().toBigInteger())
		} else {
			throw ArgumentSizeError("time", args)
		}
	}

	open fun dateTime(args: List<Data>): Data {
		if (args.isEmpty()) {
			return StringData(LocalDateTime.now().toString())
		} else {
			throw ArgumentSizeError("dateTime", args)
		}
	}

	open fun input(args: List<Data>): Data {
		if (args.isNotEmpty()) {
			printData(args)
		}
		return StringData(readln())
	}

	open fun printData(args: List<Data>): Data {
		print(args.joinToString(""))
		return zero
	}

	open fun printlnData(args: List<Data>): Data {
		println(args.joinToString(""))
		return zero
	}

	open fun sleep(args: List<Data>): Data {
		if (args.size == 1 && args[0] is IntData) {
			val a = args[0] as IntData
			Thread.sleep(a.value.toLong())
		} else {
			throw ArgumentTypeError("sleep", args)
		}

		return zero
	}

	open fun exit(args: List<Data>): Data {
		if (args.size == 1 && args[0] is IntData) {
			val a = args[0] as IntData
			exitProcess(a.value.toInt())
		} else {
			exitProcess(0)
		}
	}

	open fun equalsData(args: List<Data>): Data {
		return (args.size == 2 && args[0] == args[1]).toIntData()
	}

	open fun notEquals(args: List<Data>): Data {
		return (args.size == 2 && args[0] != args[1]).toIntData()
	}

	open fun greater(args: List<Data>): Data {
		return if (args.size == 2) {
			val a = args[0]
			val b = args[1]
			if (a is IntData && b is IntData) {
				(a.value > b.value).toIntData()
			} else if (a is DecimalData && b is DecimalData) {
				(a.value > b.value).toIntData()
			} else {
				throw ArgumentTypeError("greater", args)
			}
		} else {
			throw ArgumentSizeError("greater", args)
		}
	}

	open fun less(args: List<Data>): Data {
		return if (args.size == 2) {
			val a = args[0]
			val b = args[1]
			if (a is IntData && b is IntData) {
				(a.value < b.value).toIntData()
			} else if (a is DecimalData && b is DecimalData) {
				(a.value < b.value).toIntData()
			} else {
				throw ArgumentTypeError("less", args)
			}
		} else {
			throw ArgumentSizeError("less", args)
		}
	}

	open fun greaterEquals(args: List<Data>): Data {
		return if (args.size == 2) {
			val a = args[0]
			val b = args[1]
			if (a is IntData && b is IntData) {
				(a.value > b.value || a.value == b.value).toIntData()
			} else if (a is DecimalData && b is DecimalData) {
				(a.value > b.value || a.value == b.value).toIntData()
			} else {
				throw ArgumentTypeError("greaterEquals", args)
			}
		} else {
			throw ArgumentSizeError("greaterEquals", args)
		}
	}

	open fun lessEquals(args: List<Data>): Data {
		return if (args.size == 2) {
			val a = args[0]
			val b = args[1]
			if (a is IntData && b is IntData) {
				(a.value < b.value || a.value == b.value).toIntData()
			} else if (a is DecimalData && b is DecimalData) {
				(a.value < b.value || a.value == b.value).toIntData()
			} else {
				throw ArgumentTypeError("lessEquals", args)
			}
		} else {
			throw ArgumentSizeError("lessEquals", args)
		}
	}

	open fun not(args: List<Data>): Data {
		return if (args.size == 1 && args[0] is IntData) {
			(args[0] == zero).toIntData()
		} else {
			throw ArgumentTypeError("not", args)
		}
	}

	open fun and(args: List<Data>): Data {
		return if (args.size == 2) {
			val a = args[0]
			val b = args[1]
			if (a is IntData && b is IntData) {
				(a.value.toBoolean() && b.value.toBoolean()).toIntData()
			} else {
				throw ArgumentTypeError("and", args)
			}
		} else {
			throw ArgumentSizeError("and", args)
		}
	}

	open fun or(args: List<Data>): Data {
		return if (args.size == 2) {
			val a = args[0]
			val b = args[1]
			if (a is IntData && b is IntData) {
				(a.value.toBoolean() || b.value.toBoolean()).toIntData()
			} else {
				throw ArgumentTypeError("or", args)
			}
		} else {
			throw ArgumentSizeError("or", args)
		}
	}

	open fun len(args: List<Data>): Data {
		return if (args.size == 1) {
			IntData(
				when (val a = args[0]) {
					is StringData -> a.value.length
					is ListData -> a.value.size
					else -> throw ArgumentTypeError("len", args)
				}
			)
		} else {
			throw ArgumentSizeError("len", args)
		}
	}

	open fun contains(args: List<Data>): Data {
		return if (args.size == 2) {
			val a = args[0]
			val b = args[1]
			if (a is StringData && b is StringData) {
				a.value.contains(b.value).toIntData()
			} else if (a is ListData) {
				a.value.contains(b).toIntData()
			} else {
				throw ArgumentTypeError("contains", args)
			}
		} else {
			throw ArgumentSizeError("contains", args)
		}
	}

	open fun matches(args: List<Data>): Data {
		return if (args.size == 2 && args[0] is StringData && args[1] is StringData) {
			val a = args[0] as StringData
			val b = args[1] as StringData
			a.value.matches(Regex(b.value)).toIntData()
		} else {
			throw ArgumentTypeError("replaceFirst", args)
		}
	}

	open fun replaceFirst(args: List<Data>): Data {
		return if (args.size == 3 && args[0] is StringData && args[1] is StringData && args[2] is StringData) {
			val a = args[0] as StringData
			val b = args[1] as StringData
			val c = args[2] as StringData
			StringData(a.value.replaceFirst(Regex(b.value), c.value))
		} else {
			throw ArgumentTypeError("replaceFirst", args)
		}
	}

	open fun replace(args: List<Data>): Data {
		return if (args.size == 3 && args[0] is StringData && args[1] is StringData && args[2] is StringData) {
			val a = args[0] as StringData
			val b = args[1] as StringData
			val c = args[2] as StringData
			StringData(a.value.replace(Regex(b.value), c.value))
		} else {
			throw ArgumentTypeError("replace", args)
		}
	}

	open fun split(args: List<Data>): Data {
		return if (args.size == 2 && args[0] is StringData && args[1] is StringData) {
			val a = args[0] as StringData
			val b = args[1] as StringData
			ListData(a.value.split(Regex(b.value)).map { x -> StringData(x) }.toMutableList())
		} else {
			throw ArgumentTypeError("split", args)
		}
	}

	open fun substring(args: List<Data>): Data {
		return if (args.size == 3 && args[0] is StringData && args[1] is IntData && args[2] is IntData) {
			val a = args[0] as StringData
			val b = args[1] as IntData
			val c = args[2] as IntData
			StringData(a.value.substring(b.value.toInt(), c.value.toInt()))
		} else {
			throw ArgumentTypeError("substring", args)
		}
	}

	open fun string(args: List<Data>): Data {
		return if (args.size == 1) {
			StringData(args[0].toString())
		} else {
			throw ArgumentSizeError("string", args)
		}
	}

	open fun uppercase(args: List<Data>): Data {
		return if (args.size == 1 && args[0] is StringData) {
			val a = args[0] as StringData
			StringData(a.value.uppercase())
		} else {
			throw ArgumentTypeError("uppercase", args)
		}
	}

	open fun lowercase(args: List<Data>): Data {
		return if (args.size == 1 && args[0] is StringData) {
			val a = args[0] as StringData
			StringData(a.value.lowercase())
		} else {
			throw ArgumentTypeError("lowercase", args)
		}
	}

	open fun addAll(args: List<Data>): Data {
		return if (args.size == 2 && args[0] is ListData && args[1] is ListData) {
			val a = args[0] as ListData
			val b = args[1] as ListData
			ListData((a.value + b.value).toMutableList())
		} else {
			throw ArgumentTypeError("addAll", args)
		}
	}

	open fun remove(args: List<Data>): Data {
		return if (args.size == 2 && args[0] is ListData) {
			val a = (args[0] as ListData).value.toMutableList()
			val b = args[1]
			a.remove(b.value)
			ListData(a)
		} else {
			throw ArgumentTypeError("remove", args)
		}
	}

	open fun removeAt(args: List<Data>): Data {
		return if (args.size == 2 && args[0] is ListData && args[1] is IntData) {
			val a = (args[0] as ListData).value.toMutableList()
			val b = args[1] as IntData
			a.removeAt(b.value.toInt())
			ListData(a)
		} else {
			throw ArgumentTypeError("removeAt", args)
		}
	}

	open fun subList(args: List<Data>): Data {
		return if (args.size == 3 && args[0] is ListData && args[1] is IntData && args[2] is IntData) {
			val a = args[0] as ListData
			val b = args[1] as IntData
			val c = args[2] as IntData
			ListData(a.value.subList(b.value.toInt(), c.value.toInt()))
		} else {
			throw ArgumentTypeError("subList", args)
		}
	}

	open fun indexOf(args: List<Data>): Data {
		return if (args.size == 2 && args[0] is ListData) {
			val a = args[0] as ListData
			IntData(a.value.indexOf(args[1].value))
		} else {
			throw ArgumentTypeError("indexOf", args)
		}
	}

	open fun classData(args: List<Data>): Data {
		return if (args.size == 1) {
			StringData(args[0].type)
		} else {
			throw ArgumentSizeError("class", args)
		}
	}

	open fun copyData(args: List<Data>): Data {
		return if (args.size == 1) {
			copy(args[0])
		} else {
			throw ArgumentSizeError("copy", args)
		}
	}

	open fun set(args: List<Data>): Data {
		if (args.size == 3) {
			val a = args[0]
			val b = args[1]
			val c = args[2]
			if (a is ClassData && b is StringData) {
				a.value[b.value] = c
			} else {
				throw ArgumentTypeError("set", args)
			}
		}
		return zero
	}

	open fun get(args: List<Data>): Data {
		return if (args.size == 2) {
			val a = args[0]
			val b = args[1]
			if (a is ClassData && b is StringData) {
				a.value[b.value] ?: zero
			} else {
				throw ArgumentTypeError("get", args)
			}
		} else {
			throw ArgumentSizeError("get", args)
		}
	}

	open fun at(args: List<Data>): Data {
		return if (args.size == 2) {
			val a = args[0]
			val b = args[1]
			if (a is StringData && b is IntData) {
				CharData(a.value[b.value.toInt()])
			} else if (a is ListData && b is IntData) {
				a.value[b.value.toInt()]
			} else {
				throw ArgumentTypeError("at", args)
			}
		} else {
			throw ArgumentSizeError("at", args)
		}
	}

	open fun fields(args: List<Data>): Data {
		return if (args.size == 1 && args[0] is ClassData) {
			ListData((args[0] as ClassData).value.keys.map { x -> StringData(x) }.toMutableList())
		} else {
			throw ArgumentTypeError("fields", args)
		}
	}

	open fun copy(x: Data): Data =
		when (x) {
			is IntData -> IntData(x.value)
			is DecimalData -> DecimalData(x.value)
			is StringData -> StringData(x.value)
			is CharData -> CharData(x.value)

			is ListData -> ListData(x.value.map { copy(it) }.toMutableList())
			is ClassData -> ClassData(
				x.type,
				x.value.mapValues { (_, value) -> copy(value) }.toMutableMap()
			)

			else -> throw Error("Cannot copy unexpected value: $x (${x::class.java}")
		}


	open fun Boolean.toIntData(): IntData = if (this) IntData(1) else IntData(0)

	open fun BigInteger.toBoolean() = this > zero.value

	class ArgumentSizeError(methodName: String, args: List<Data>) :
		Error("Wrong number of arguments for $methodName: $args")

	class ArgumentTypeError(methodName: String, args: List<Data>) :
		Error("Wrong argument types for $methodName: ${args.zip(args.map { it.type })}")

}
