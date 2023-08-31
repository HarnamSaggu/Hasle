package lang

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
				val property = accessor.property
				when {
					property == "class" -> {
						value =  StringData(value.type)
					}

					value is StringData && property == "len" -> {
						value = IntData(value.value.length)
					}

					value is ListData && property == "len" -> {
						value = IntData(value.value.size)
					}

					value is StructData && property == "fields" -> {
						value = ListData(value.value.keys.map(::wrap).toMutableList())
					}

					else -> {
						value = (value as StructData).getProperty(accessor.property)
					}
				}
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
				struct.setProperty(last.property, value)
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

