package lang

import java.io.File
import kotlin.system.exitProcess

fun run(mainFile: File, args: List<String>) {
	var mainMethod = MainMethod(listOf())
	val methodDeclarations = mutableListOf<MethodDeclaration>()
	File(mainFile.parent).listFiles()?.forEach {
		val tokens = lex(it.readText())
		val (localMain, localMethods) = parse(tokens)
		if (it == mainFile) {
			mainMethod = localMain
		}
		methodDeclarations.addAll(localMethods)
	}
	Runner(mainMethod, methodDeclarations.toList(), args)
}

private fun debug(tokens: List<Token>, mainMethod: MainMethod, methods: List<MethodDeclaration>) {
	tokens.forEach { println(it) }
	println()
	println(reproduceSourceCode(tokens))
	println()
	println("main\n[args]")
	mainMethod.body.forEach { println("\t$it") }
	println()
	methods.filterNot { it is ClassDeclaration }.forEach {
		println(it.methodName)
		println(it.parameters)
		it.body.forEach { x -> println("\t$x") }
		println(it.returnStatement)
		println()
	}
	println()
	methods.filterIsInstance<ClassDeclaration>().forEach {
		println(it.className)
		println(it.parameters)
		it.fields.forEach { (key, value) -> println("\t$key=\t$value") }
		println()
	}
	println()
	println("EXECUTION:")
	println()
}

fun run(sourceCode: String, args: List<String>) {
	val tokens = lex(sourceCode)
	val (mainMethod, methods) = parse(tokens)

//	debug(tokens, mainMethod, methods)

	Runner(mainMethod, methods, args)
}

class Runner(
	mainMethod: MainMethod,
	methodDeclarations: List<MethodDeclaration>,
	args: List<String>,
	private val standardLibrary: StandardLibrary = StandardLibrary(
		{ readln() },
		{ print(it) },
		{ x -> exitProcess(x) },
		{ x -> Thread.sleep(x) }
	)
) {
	private val dataManager: DataManager = DataManager()
	private val methodMap = methodDeclarations.associateBy { it.methodName }

	init {
		dataManager.setVariable("args", listOf(), ListData(args.map { StringData(it) }.toMutableList()), Pair(1, 0))

		val exitCode = runSection(mainMethod.body, Pair(1, 0)) as IntData
		standardLibrary.exit(exitCode.value.toInt())
	}

	private fun runSection(
		commands: List<Command>,
		stackLevel: Pair<Int, Int>,
		returnExpression: Command = 0.toValue()
	): Data {
		for (command in commands) {
			when (command) {
				is Assignment -> {
					val value = evaluateExpression(command.value, stackLevel)
					val accessors = command.reference.accessors
					accessors.forEach {
						if (it is Index) {
							it.int = (evaluateExpression(it.index, stackLevel) as IntData).value.toInt()
						}
					}
					dataManager.setVariable(
						command.reference.variableName,
						accessors,
						value,
						if (command.isGlobal) Pair(0, 0) else stackLevel
					)
				}

				is MethodCall -> runMethod(command, stackLevel)

				is If -> {
					val booleanExpression = (evaluateExpression(command.booleanExpression, stackLevel) as IntData).value
					val newLevel = dataManager.nextStack(stackLevel)

					runSection(
						if (booleanExpression > 0.toBigInteger()) {
							command.firstBody
						} else {
							command.secondBody
						}, newLevel
					)

					dataManager.popStack(newLevel)
				}

				is While -> {
					var booleanExpression = (evaluateExpression(command.booleanExpression, stackLevel) as IntData).value
					while (booleanExpression > 0.toBigInteger()) {
						val newLevel = dataManager.nextStack(stackLevel)

						runSection(command.body, newLevel)

						dataManager.popStack(newLevel)

						booleanExpression = (evaluateExpression(command.booleanExpression, stackLevel) as IntData).value
					}
				}

				else -> throw Error("Unidentified Command: $command")
			}
		}

		return evaluateExpression(returnExpression, stackLevel)
	}

	private fun runMethod(methodCall: MethodCall, stackLevel: Pair<Int, Int>): Data {
		val arguments = methodCall.arguments.map { evaluateExpression(it, stackLevel) }
		val definedMethod = standardLibrary.methods[methodCall.methodName]

		return when {
			methodMap.containsKey(methodCall.methodName) -> {
				val methodDeclaration = methodMap[methodCall.methodName] ?: return IntData(0)

				val methodStackLevel = dataManager.addMethodLevel()

				methodDeclaration.parameters.forEachIndexed { index, parameter ->
					dataManager.setVariable(parameter, listOf(), arguments[index], methodStackLevel)
				}

				val returnValue = if (methodDeclaration is ClassDeclaration) {
					ClassData(
						methodDeclaration.className,
						methodDeclaration.fields.mapValues { (_, value) ->
							evaluateExpression(
								value,
								methodStackLevel
							)
						}.toMutableMap()
					)
				} else {
					runSection(
						methodDeclaration.body,
						methodStackLevel,
						methodDeclaration.returnStatement
					)
				}

				dataManager.removeMethodLevel()

				returnValue
			}

			definedMethod != null -> definedMethod.invoke(arguments)

			else -> throw Error("Unidentified Method: ${methodCall.methodName}")
		}
	}

	private fun evaluateExpression(expression: Command, stackLevel: Pair<Int, Int>): Data {
		when (expression) {
			is Reference -> {
				val name = expression.variableName

				val accessors = expression.accessors
				accessors.forEach {
					if (it is Index) {
						it.int = (evaluateExpression(it.index, stackLevel) as IntData).value.toInt()
					}
				}
				return dataManager.getVariable(name, accessors, stackLevel)
			}

			is Value<*> -> if (expression.value != null) {
				return wrap(expression.value)
			}

			is DefinedList -> {
				return ListData(expression.elements.map { evaluateExpression(it, stackLevel) }.toMutableList())
			}

			is SizedList -> {
				val size = (evaluateExpression(expression.size, stackLevel) as IntData).value.toInt()
				return ListData(MutableList(size) { IntData(0) })
			}

			is MethodCall -> return runMethod(expression, stackLevel)
		}

		throw Error("Unable to evaluate Expression: $expression")
	}

}
