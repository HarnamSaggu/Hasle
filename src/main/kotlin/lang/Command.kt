package lang

import java.math.BigInteger

abstract class Command

class MainMethod(val body: List<Command>) : Command() {
	override fun toString(): String {
		return "(main).{\n${body.joinToString("\n\t")}\n}"
	}
}

open class MethodDeclaration(
	val methodName: String,
	open val parameters: List<String>,
	val body: List<Command>,
	val returnStatement: Return
) : Command() {
	override fun toString(): String {
		return "(method).{$methodName}.{$parameters}.{\n${body.joinToString("\n\t")}\n}"
	}
}

class ClassDeclaration(
	val className: String,
	override val parameters: List<String>,
	val fields: Map<String, Command>
) : MethodDeclaration(className, parameters, listOf(), Return(0.toValue())) {
	override fun toString(): String {
		return "(class).{$className}.{$parameters}.{\n${
			fields.keys.joinToString("\n") {
				"\t$it=\t${fields[it]}"
			}
		}\n}"
	}
}

class Reference(
	val variableName: String,
	val accessors: List<Accessor>
) : Command() {
	override fun toString(): String {
		return "(variable).{$variableName}.{$accessors}"
	}
}

class Assignment(
	val reference: Reference,
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

class If(
	val booleanExpression: Command,
	val firstBody: List<Command>,
	val secondBody: List<Command>
) : Command() {
	override fun toString(): String {
		return "(if).{$booleanExpression}.{\n${firstBody.joinToString("\n\t")}" +
		       ".{\n${secondBody.joinToString("\n\t")}"
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

fun Int.toValue(): Value<BigInteger> = Value(toBigInteger())

class DefinedList(val elements: List<Command>) : Command() {
	override fun toString(): String {
		return "(array).{$elements}"
	}
}

class SizedList(val size: Command) : Command() {
	override fun toString(): String {
		return "(sized array).{$size}"
	}
}

class Return(val value: Command) : Command() {
	override fun toString(): String {
		return "(return).{$value}"
	}
}

abstract class Accessor

class Index(val index: Command) : Accessor() {
	var int = -1

	override fun toString(): String {
		return "(index).{$index}"
	}
}

class Property(val property: String) : Accessor() {
	override fun toString(): String {
		return "(property).{$property}"
	}
}
