package lang

import java.math.BigDecimal
import java.math.BigInteger

fun wrap(value: Any): Data =
	when (value) {
		is BigInteger -> IntData(value)
		is BigDecimal -> DecimalData(value)
		is String -> StringData(value)
		is Char -> CharData(value)

		else -> throw Error("Cannot interpret value: (${value::class.java}) $value")
	}

abstract class Data(open val type: String, open val value: Any) {
	override fun equals(other: Any?): Boolean {
		return other is Data && other.type == type && other.value == value
	}

	override fun toString(): String {
		return value.toString()
	}

	override fun hashCode(): Int {
		var result = value.hashCode()
		result = 31 * result + type.hashCode()
		return result
	}
}

class IntData(override val value: BigInteger) : Data("int", value) {
	constructor(int: Int) : this(int.toBigInteger())
}

class DecimalData(override val value: BigDecimal) : Data("decimal", value)

class StringData(override val value: String) : Data("string", value)

class CharData(override val value: Char) : Data("char", value)

class ListData(override val value: MutableList<Data>) : Data("list", value) {
	fun getIndex(index: Int): Data = value[index]

	fun setIndex(index: Int, element: Data) {
		value[index] = element
	}
}

class StructData(override val type: String, override val value: MutableMap<String, Data>) : Data(type, value) {
	fun getProperty(property: String): Data = value[property] ?: throw Error("Unrecognised property: $property in $type")

	fun setProperty(property: String, fieldValue: Data) {
		value[property] = fieldValue
	}

	override fun toString(): String {
		return "$type$value"
	}
}
