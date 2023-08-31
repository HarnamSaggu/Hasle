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

class StandardLibrary(
    val input: (() -> String) = {
        readln()
    },

    val output: ((Any) -> Unit) = {
        print(it)
    },

    val exit: ((Int) -> Any) = { x ->
        exitProcess(x)
    },

    val sleep: ((Long) -> Any) = { x ->
        Thread.sleep(x)
    }
) {

    val methods: Map<String, (List<Data>) -> Data> = mapOf(

        // MATHS

        "add" to {
            if (it.size == 2) {
                val a = it[0]
                val b = it[1]

                when {
                    a is IntData && b is IntData -> IntData(a.value + b.value)
                    a is DecimalData && b is DecimalData -> DecimalData(a.value + b.value)
                    a is ListData -> ListData((a.value + listOf(b)).toMutableList())
                    a is StringData && b is StringData -> StringData(a.value + b.value)
                    else -> zero
                }
            } else {
                zero
            }
        },

        "sub" to {
            if (it.size == 2) {
                val a = it[0]
                val b = it[1]
                if (a is IntData && b is IntData) {
                    IntData(a.value - b.value)
                } else if (a is DecimalData && b is DecimalData) {
                    DecimalData(a.value - b.value)
                } else {
                    zero
                }
            } else {
                zero
            }
        },

        "mult" to {
            if (it.size == 2) {
                val a = it[0]
                val b = it[1]
                if (a is IntData && b is IntData) {
                    IntData(a.value * b.value)
                } else if (a is DecimalData && b is DecimalData) {
                    DecimalData(a.value * b.value)
                } else {
                    zero
                }
            } else {
                zero
            }
        },

        "div" to {
            if (it.size == 2) {
                val a = it[0]
                val b = it[1]
                if (a is IntData && b is IntData) {
                    IntData(a.value / b.value)
                } else if (a is DecimalData && b is DecimalData) {
                    DecimalData(a.value.divide(b.value, 16, RoundingMode.HALF_UP))
                } else {
                    zero
                }
            } else if (it.size == 3) {
                val a = it[0]
                val b = it[1]
                val c = it[0]
                if (a is DecimalData && b is DecimalData && c is IntData) {
                    DecimalData(a.value.divide(b.value, c.value.toInt(), RoundingMode.HALF_UP))
                } else {
                    zero
                }
            } else {
                zero
            }
        },

        "pow" to {
            if (it.size == 2) {
                val a = it[0]
                val b = it[1]
                if (a is IntData && b is IntData) {
                    IntData(a.value.pow(b.value.toInt()))
                } else if (a is DecimalData && b is DecimalData) {
                    DecimalData(pow(a.value, b.value))
                } else {
                    zero
                }
            } else {
                zero
            }
        },

        "mod" to {
            if (it.size == 2) {
                val a = it[0]
                val b = it[1]
                if (a is IntData && b is IntData) {
                    IntData(a.value.mod(b.value))
                } else {
                    zero
                }
            } else {
                zero
            }
        },

        "ln" to {
            if (it.size == 1) {
                val a = it[0]
                if (a is DecimalData) {
                    DecimalData(log(a.value))
                } else {
                    zero
                }
            } else {
                zero
            }
        },

        "log" to {
            if (it.size == 2) {
                val a = it[0]
                val b = it[1]
                if (a is DecimalData && b is DecimalData) {
                    DecimalData(log(a.value).divide(log(b.value), 16, RoundingMode.HALF_UP))
                } else {
                    zero
                }
            } else {
                zero
            }
        },

        "sqrt" to {
            if (it.size == 1) {
                val a = it[0]
                if (a is DecimalData) {
                    DecimalData(sqrt(a.value))
                } else {
                    zero
                }
            } else {
                zero
            }
        },

        "nthrt" to {
            if (it.size == 2) {
                val a = it[0]
                val b = it[1]
                if (a is DecimalData && b is DecimalData) {
                    DecimalData(root(b.value.toInt(), a.value))
                } else {
                    zero
                }
            } else {
                zero
            }
        },

        "sin" to {
            if (it.size == 1) {
                val a = it[0]
                if (a is DecimalData) {
                    DecimalData(sin(a.value))
                } else {
                    zero
                }
            } else {
                zero
            }
        },

        "cos" to {
            if (it.size == 1) {
                val a = it[0]
                if (a is DecimalData) {
                    DecimalData(cos(a.value))
                } else {
                    zero
                }
            } else {
                zero
            }
        },

        "tan" to {
            if (it.size == 1) {
                val a = it[0]
                if (a is DecimalData) {
                    DecimalData(tan(a.value))
                } else {
                    zero
                }
            } else {
                zero
            }
        },

        "asin" to {
            if (it.size == 1) {
                val a = it[0]
                if (a is DecimalData) {
                    DecimalData(asin(a.value))
                } else {
                    zero
                }
            } else {
                zero
            }
        },

        "acos" to {
            if (it.size == 1) {
                val a = it[0]
                if (a is DecimalData) {
                    DecimalData(acos(a.value))
                } else {
                    zero
                }
            } else {
                zero
            }
        },

        "atan" to {
            if (it.size == 1) {
                val a = it[0]
                if (a is DecimalData) {
                    DecimalData(atan(a.value))
                } else {
                    zero
                }
            } else {
                zero
            }
        },

        "pi" to {
            val length = if (it.size == 1 && it[0] is IntData) {
                (it[0] as IntData).value.toInt()
            } else {
                16
            }

            DecimalData(pi(MathContext(length)))
        },

        "e" to {
            val length = if (it.size == 1 && it[0] is IntData) {
                (it[0] as IntData).value.toInt()
            } else {
                16
            }

            DecimalData(exp(MathContext(length)))
        },

        "rand" to {
            DecimalData(BigDecimal(Math.random()))
        },

        "ceil" to {
            if (it.size == 1) {
                val a = it[0]
                if (a is DecimalData) {
                    DecimalData(a.value.add(BigDecimal("0.5")).setScale(0, RoundingMode.HALF_UP))
                } else {
                    zero
                }
            } else {
                zero
            }
        },

        "round" to {
            if (it.size == 1) {
                val a = it[0]
                if (a is DecimalData) {
                    DecimalData(a.value.setScale(0, RoundingMode.HALF_UP))
                } else {
                    zero
                }
            } else {
                zero
            }
        },

        "floor" to {
            if (it.size == 1) {
                val a = it[0]
                if (a is DecimalData) {
                    DecimalData(a.value.subtract(BigDecimal("0.5")).setScale(0, RoundingMode.HALF_UP))
                } else {
                    zero
                }
            } else {
                zero
            }
        },

        "min" to {
            if (it.size == 2) {
                val a = it[0]
                val b = it[1]
                if (a is IntData && b is IntData) {
                    IntData(a.value.min(b.value))
                } else if (a is DecimalData && b is DecimalData) {
                    DecimalData(a.value.min(b.value))
                } else {
                    zero
                }
            } else {
                zero
            }
        },

        "max" to {
            if (it.size == 2) {
                val a = it[0]
                val b = it[1]
                if (a is IntData && b is IntData) {
                    IntData(a.value.max(b.value))
                } else if (a is DecimalData && b is DecimalData) {
                    DecimalData(a.value.max(b.value))
                } else {
                    zero
                }
            } else {
                zero
            }
        },

        // CONVERSIONS

        "dec" to {
            DecimalData(
                when {
                    it.size == 2 && it[0] is DecimalData && it[1] is IntData -> {
                        val a = it[0] as DecimalData
                        val b = it[1] as IntData
                        BigDecimal(a.value.toString(), MathContext(b.value.toInt(), RoundingMode.HALF_UP))
                    }

                    it.size == 1 && it[0] is IntData -> {
                        val a = it[0] as IntData
                        a.value.toBigDecimal()
                    }

                    it.size == 1 && it[0] is StringData -> {
                        val a = it[0] as StringData
                        BigDecimal(a.value)
                    }

                    else -> {
                        BigDecimal.ZERO
                    }
                }
            )
        },

        "int" to {
            if (it.size == 1) {
                IntData(
                    when (val a = it[0]) {
                        is DecimalData -> a.value.toBigInteger()
                        is CharData -> a.value.code.toBigInteger()
                        is StringData -> BigInteger(a.value)
                        else -> BigInteger("0")
                    }
                )
            } else {
                zero
            }
        },

        "char" to {
            if (it.size == 1 && it[0] is IntData) {
                val a = it[0] as IntData
                CharData(a.value.toInt().toChar())
            } else {
                zero
            }
        },

        "bool" to {
            if (it.size == 1 && it[0] is IntData) {
                val a = it[0] as IntData
                StringData(if (a.value <= zero.value) "false" else "true")
            } else {
                zero
            }
        },

        // I/O

        "readln" to {
            StringData(input())
        },

        "print" to {
            output(it.joinToString(""))
            zero
        },

        "println" to {
            output(it.joinToString("") + "\n")
            zero
        },

        "sleep" to {
            if (it.size == 1 && it[0] is IntData) {
                val a = it[0] as IntData
                sleep(a.value.toLong())
            }

            zero
        },

        "readFile" to {
            if (it.size == 1 && it[0] is StringData) {
                val a = it[0] as StringData
                StringData(File(a.value).readText())
            } else {
                zero
            }
        },

        "writeFile" to {
            if (it.size == 2 && it[0] is StringData && it[1] is StringData) {
                val a = it[0] as StringData
                val b = it[1] as StringData
                File(a.value).writeText(b.value)
            }

            zero
        },

        "readBytes" to {
            if (it.size == 1 && it[0] is StringData) {
                val a = it[0] as StringData
                ListData(File(a.value).readBytes().map { byte ->
                    IntData(byte.toInt())
                }.toMutableList())
            } else {
                zero
            }
        },

        "writeFile" to {
            if (it.size == 2 && it[0] is StringData && it[1] is ListData) {
                val a = it[0] as StringData
                val b = it[1] as ListData
                File(a.value).writeBytes(
                    b.value.map { byte ->
                        (byte as IntData).value.toString().toByte()
                    }.toByteArray()
                )
            }

            zero
        },

        "exit" to {
            if (it.size == 1 && it[0] is IntData) {
                val a = it[0] as IntData
                exit(a.value.toInt())
            } else {
                exit(0)
            }
            zero
        },

        "time" to {
            IntData(System.currentTimeMillis().toBigInteger())
        },

        "dateTime" to {
            StringData(LocalDateTime.now().toString())
        },

        // LOGIC

        "equals" to {
            (it.size == 2 && it[0] == it[1]).toIntData()
        },

        "notEquals" to {
            (it.size == 2 && it[0] != it[1]).toIntData()
        },

        "greater" to {
            if (it.size == 2) {
                val a = it[0]
                val b = it[1]
                if (a is IntData && b is IntData) {
                    (a.value > b.value).toIntData()
                } else if (a is DecimalData && b is DecimalData) {
                    (a.value > b.value).toIntData()
                } else {
                    zero
                }
            } else {
                zero
            }
        },

        "less" to {
            if (it.size == 2) {
                val a = it[0]
                val b = it[1]
                if (a is IntData && b is IntData) {
                    (a.value < b.value).toIntData()
                } else if (a is DecimalData && b is DecimalData) {
                    (a.value < b.value).toIntData()
                } else {
                    zero
                }
            } else {
                zero
            }
        },

        "greaterEquals" to {
            if (it.size == 2) {
                val a = it[0]
                val b = it[1]
                if (a is IntData && b is IntData) {
                    (a.value > b.value || a.value == b.value).toIntData()
                } else if (a is DecimalData && b is DecimalData) {
                    (a.value > b.value || a.value == b.value).toIntData()
                } else {
                    zero
                }
            } else {
                zero
            }
        },

        "lessEquals" to {
            if (it.size == 2) {
                val a = it[0]
                val b = it[1]
                if (a is IntData && b is IntData) {
                    (a.value < b.value || a.value == b.value).toIntData()
                } else if (a is DecimalData && b is DecimalData) {
                    (a.value < b.value || a.value == b.value).toIntData()
                } else {
                    zero
                }
            } else {
                zero
            }
        },

        "not" to {
            if (it.size == 1 && it[0] is IntData) {
                (it[0] == zero).toIntData()
            } else {
                zero
            }
        },

        "and" to {
            if (it.size == 2) {
                val a = it[0]
                val b = it[1]
                if (a is IntData && b is IntData) {
                    (a.value.toBoolean() && b.value.toBoolean()).toIntData()
                } else {
                    zero
                }
            } else {
                zero
            }
        },

        "or" to {
            if (it.size == 2) {
                val a = it[0]
                val b = it[1]
                if (a is IntData && b is IntData) {
                    (a.value.toBoolean() || b.value.toBoolean()).toIntData()
                } else {
                    zero
                }
            } else {
                zero
            }
        },

        // STRING, LISTS

        "len" to {
            if (it.size == 1) {
                IntData(
                    when (val a = it[0]) {
                        is StringData -> a.value.length
                        is ListData -> a.value.size
                        else -> 0
                    }
                )
            } else {
                zero
            }
        },

        "contains" to {
            if (it.size == 2) {
                val a = it[0]
                val b = it[1]
                if (a is StringData && b is StringData) {
                    a.value.contains(b.value).toIntData()
                } else if (a is ListData) {
                    a.value.contains(b.value).toIntData()
                } else {
                    zero
                }
            } else {
                zero
            }
        },

        "replaceFirst" to {
            if (it.size == 3 && it[0] is StringData && it[1] is StringData && it[2] is StringData) {
                val a = it[0] as StringData
                val b = it[1] as StringData
                val c = it[2] as StringData
                StringData(a.value.replaceFirst(Regex(b.value), c.value))
            } else {
                zero
            }
        },

        "replace" to {
            if (it.size == 3 && it[0] is StringData && it[1] is StringData && it[2] is StringData) {
                val a = it[0] as StringData
                val b = it[1] as StringData
                val c = it[2] as StringData
                StringData(a.value.replace(Regex(b.value), c.value))
            } else {
                zero
            }
        },

        "split" to {
            if (it.size == 2 && it[0] is StringData && it[1] is StringData) {
                val a = it[0] as StringData
                val b = it[1] as StringData
                ListData(a.value.split(Regex(b.value)).map { x -> StringData(x) }.toMutableList())
            } else {
                zero
            }
        },

        "substring" to {
            if (it.size == 3 && it[0] is StringData && it[1] is IntData && it[2] is IntData) {
                val a = it[0] as StringData
                val b = it[1] as IntData
                val c = it[2] as IntData
                StringData(a.value.substring(b.value.toInt(), c.value.toInt()))
            } else {
                zero
            }
        },

        "string" to {
            if (it.size == 1) {
                StringData(it[0].toString())
            } else {
                zero
            }
        },

        "uppercase" to {
            if (it.size == 1 && it[0] is StringData) {
                val a = it[0] as StringData
                StringData(a.value.uppercase())
            } else {
                zero
            }
        },

        "lowercase" to {
            if (it.size == 1 && it[0] is StringData) {
                val a = it[0] as StringData
                StringData(a.value.lowercase())
            } else {
                zero
            }
        },

        "addAll" to {
            if (it.size == 2 && it[0] is ListData && it[1] is ListData) {
                val a = it[0] as ListData
                val b = it[1] as ListData
                ListData((a.value + b.value).toMutableList())
            } else {
                zero
            }
        },

        "removeAt" to {
            if (it.size == 2 && it[0] is ListData && it[1] is IntData) {
                val a = (it[0] as ListData).value.toMutableList()
                val b = it[1] as IntData
                a.removeAt(b.value.toInt())
                ListData(a)
            } else {
                zero
            }
        },

        "subList" to {
            if (it.size == 3 && it[0] is ListData && it[1] is IntData && it[2] is IntData) {
                val a = it[0] as ListData
                val b = it[1] as IntData
                val c = it[2] as IntData
                ListData(a.value.subList(b.value.toInt(), c.value.toInt()))
            } else {
                zero
            }
        },

        "indexOf" to {
            if (it.size == 2 && it[0] is ListData) {
                val a = it[0] as ListData
                IntData(a.value.indexOf(it[1].value))
            } else {
                zero
            }
        },

        // OBJECTS

        "class" to {
            if (it.size == 1) {
                StringData(it[0].type)
            } else {
                zero
            }
        },

        "copy" to {
            if (it.size == 1) {
                copy(it[0])
            } else {
                zero
            }
        },

        )
}

fun Boolean.toIntData(): IntData = if (this) IntData(1) else IntData(0)

fun BigInteger.toBoolean() = this > zero.value

fun copy(x: Data): Data =
    when (x) {
        is IntData -> IntData(x.value)
        is DecimalData -> DecimalData(x.value)
        is StringData -> StringData(x.value)
        is CharData -> CharData(x.value)

        is ListData -> ListData(x.value.map { copy(it) }.toMutableList())
        is StructData -> StructData(
            x.type,
            x.value.mapValues { (_, value) -> copy(value) }.toMutableMap()
        )

        else -> throw Error("Cannot copy unexpected value: $x (${x::class.java}")
    }
