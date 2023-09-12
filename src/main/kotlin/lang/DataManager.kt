package lang

import java.math.BigDecimal
import java.math.BigInteger

class DataManager {
	private val heap: MutableMap<Pair<Int, Int>, MutableMap<String, Data>> = mutableMapOf()
	private var methodIndex: Int = 0

	init {
		addMethodLevel()
		addMethodLevel()
	}

	fun getVariable(variableName: String, accessors: List<Accessor>, stackLevel: Pair<Int, Int>): Data {
		val firstLevel = lowestLevel(variableName, stackLevel)
		var value = heap[firstLevel]?.get(variableName)
		            ?: throw Error("Cannot locate variable: $variableName $accessors")
		for (accessor in accessors) {
			if (accessor is Index) {
				if (value is ListData) {
					value = value.getIndex(accessor.int)
				} else if (value is StringData) {
					value = CharData(value.value[accessor.int])
				}
			} else if (accessor is Property) {
				value = (value as StructData).getProperty(accessor.property)
			}
		}
		return value
	}

	fun setVariable(variableName: String, accessors: List<Accessor>, value: Data, stackLevel: Pair<Int, Int>) {
		val firstLevel = lowestLevel(variableName, stackLevel)
		if (accessors.isNotEmpty()) {
			var variable = heap[firstLevel]?.get(variableName)
			               ?: throw Error("Cannot locate variable: $variableName $accessors")
			for (accessor in accessors.dropLast(1)) {
				if (accessor is Index) {
					variable = (variable as ListData).getIndex(accessor.int)
				} else if (accessor is Property) {
					variable = (variable as StructData).getProperty(accessor.property)
				}
			}
			val last = accessors.last()
			if (last is Index) {
				val list = variable as ListData
				list.setIndex(last.int, value)
			} else if (last is Property) {
				val struct = variable as StructData
				if (last.property == "class" || last.property == "fields") {
					throw Error("Cannot modify ${last.property} of $struct (reference: $variableName $accessors)")
				} else {
					struct.setProperty(last.property, value)
				}
			}
		} else {
			heap[firstLevel]?.let {
				it[variableName] = value
			}
		}
	}

	fun nextStack(stackLevel: Pair<Int, Int>): Pair<Int, Int> {
		val next = Pair(stackLevel.first, stackLevel.second + 1)
		heap[next] = mutableMapOf()
		return next
	}

	fun popStack(stackLevel: Pair<Int, Int>) {
		heap.remove(stackLevel)
	}

	fun addMethodLevel(): Pair<Int, Int> {
		val next = Pair(methodIndex++, 0)
		heap[next] = mutableMapOf()
		return next
	}

	fun removeMethodLevel() {
		popStack(Pair(--methodIndex, 0))
	}

	private fun lowestLevel(variableName: String, stackLevel: Pair<Int, Int>): Pair<Int, Int> {
		if (heap[Pair(0, 0)]?.containsKey(variableName) == true) {
			return Pair(0, 0)
		}

		for (count in 0..stackLevel.second) {
			if (heap[Pair(stackLevel.first, count)]?.containsKey(variableName) == true) {
				return Pair(stackLevel.first, count)
			}
		}

		return stackLevel
	}

	override fun toString(): String {
		var string = ""
		for (level in heap.keys) {
			string += level.toString() + "-".repeat(30) + "\n"
			heap[level]?.let { subLevel ->
				subLevel.forEach { (variable, value) -> string += "\t$variable=$value\n" }
			}
			string += "\n\n"
		}
		return string
	}
}

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
	fun getProperty(property: String): Data =
		value[property] ?: throw Error("Unrecognised property: $property in $type")

	fun setProperty(property: String, fieldValue: Data) {
		value[property] = fieldValue
	}

	override fun toString(): String {
		return "$type$value"
	}
}
