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

abstract class Data(open val value: Any) {
	open val type: String = "NaN"

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

class IntData(override val value: BigInteger) : Data(value) {
	constructor(int: Int) : this(int.toBigInteger())

	override val type: String = "int"
}

class DecimalData(override val value: BigDecimal) : Data(value) {
	override val type: String = "decimal"
}

class StringData(override val value: String) : Data(value) {
	override val type: String = "string"
}

class CharData(override val value: Char) : Data(value) {
	override val type: String = "char"
}

class ListData(override val value: MutableList<Data>) : Data(value) {
	override val type: String = "list"

	fun getIndex(index: Int): Data = value[index]

	fun setIndex(index: Int, element: Data) {
		value[index] = element
	}
}

class StructData(override val type: String, override val value: MutableMap<String, Data>) : Data(value) {
	fun getProperty(property: String): Data = value[property] ?: throw Error("Unrecognised property: $property in $type")

	fun setProperty(property: String, fieldValue: Data) {
		value[property] = fieldValue
	}
}
