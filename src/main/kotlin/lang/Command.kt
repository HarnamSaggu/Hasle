package lang

open class Command {
	override fun toString(): String {
		return "(UNIDENTIFIED)"
	}
}

class MainMethod(val body: List<Command>) : Command() {
	override fun toString(): String {
		return "(main).{\n${body.joinToString("\n\t")}\n}"
	}
}

open class MethodDeclaration(
	val methodName: String,
	open val parameters: List<String>,
	val body: List<Command>,
	val returnStatement: Command
) : Command() {
	override fun toString(): String {
		return "(method).{$methodName}.{$parameters}.{\n${body.joinToString("\n\t")}\n}"
	}
}

class StructDeclaration(
	val structName: String,
	override val parameters: List<String>,
	val fields: Map<String, Command>
) : MethodDeclaration(structName, parameters, listOf(), Command()) {
	override fun toString(): String {
		return "(struct).{$structName}.{$parameters}.{\n${fields.keys.joinToString("\n") {
			"\t$it=\t${fields[it]}"
		}}\n}"
	}
}

class VariableReference(
	val variableName: String,
	val accessors: List<Access>
) : Command() {
	override fun toString(): String {
		return "(variable).{$variableName}.{$accessors}"
	}
}

class Assignment(
	val reference: VariableReference,
	val value: Command,
	val isGlobal: Boolean
) : Command() {
	override fun toString(): String {
		return "(assignment).{$reference}.{$isGlobal}.{$value}"
	}
}

class MethodCall(val methodName: String, val arguments: List<Command>) : Command() {
	override fun toString(): String {
		return "(method call).{$methodName}.{$arguments}"
	}
}

class If(val booleanExpression: Command, val firstBody: List<Command>, val secondBody: List<Command>) : Command() {
	override fun toString(): String {
		return "(if).{$booleanExpression}.{\n${firstBody.joinToString("\n\t")}.{\n${secondBody.joinToString("\n\t")}"
	}
}

class While(val booleanExpression: Command, val body: List<Command>) : Command() {
	override fun toString(): String {
		return "(while).{$booleanExpression}.{\n${body.joinToString("\n\t")}"
	}
}

class Value<T>(val value: T) : Command() {
	override fun toString(): String {
		return "(value).{$value}"
	}
}

class DefinedArray(val elements: List<Command>) : Command() {
	override fun toString(): String {
		return "(array).{$elements}"
	}
}

class SizedArray(val size: Command) : Command() {
	override fun toString(): String {
		return "(sized array).{$size}"
	}
}

open class Access

class Index(val index: Command) : Access() {
	var int = -1

	override fun toString(): String {
		return "(index).{$index}"
	}
}

class Property(val property: String) : Access() {
	override fun toString(): String {
		return "(property).{$property}"
	}
}
