package generator

import language.AST
<<<<<<< Updated upstream
=======
import java.math.BigInteger
>>>>>>> Stashed changes
import java.util.UUID
import kotlin.math.exp

typealias MutableMCCommands = MutableList<String>
typealias MCCommands = List<String>


object MC {

    data class Function(
        val name: String,
        val contents: MCCommands = emptyList(),
    )

    data class Program(
        val contents: MCCommands = emptyList(),
        val functions: List<Function> = emptyList(),
    )

    fun createRandomId(): String {
        return UUID.randomUUID().toString()
    }

    fun createFunctionCall(functionName: String): String {
        return "function codecraft:${functionName}"
    }

    const val storageResourceLocation = "codecraft:var"
    const val loadFunctionName = "load"
}


object Generator {

    fun generateProgram(program: AST.Program): MC.Program {
        val contents: MutableMCCommands = mutableListOf()
        val functions: MutableList<MC.Function> = mutableListOf()

        program.elements.forEach { programElement ->
            val lineProgram = generateProgramElement(programElement)

            contents.addAll(lineProgram.contents)
            functions.addAll(lineProgram.functions)
        }

        if (functions.none { it.name == "tick" }) {
            functions.add(
                MC.Function(
                    name = "tick"
                )
            )
        }

        val loadFunction = MC.Function(
            name = MC.loadFunctionName,
            contents = contents
        )

        return MC.Program(
            functions = functions + listOf(loadFunction)
        )
    }

    fun generateProgramElement(programElement: AST.ProgramElement): MC.Program {
        return when (programElement) {
            is AST.ProgramElement.ProgramStatement -> generateProgramStatement(programElement)
            is AST.ProgramElement.ProgramFuncDef -> generateProgramFuncDef(programElement)
        }
    }

    fun generateProgramStatement(programStatement: AST.ProgramElement.ProgramStatement): MC.Program {
        return generateStatment(programStatement.statement)
    }

    fun generateProgramFuncDef(programFuncDef: AST.ProgramElement.ProgramFuncDef): MC.Program {
        val commands: MutableMCCommands = mutableListOf()

        programFuncDef.funcDef.parameters.forEachIndexed { i, param ->
            val paramSetCmd = "\$data modify storage codecraft:var ${param.name} set value \$(p${i})"

            commands.add(paramSetCmd)
        }

        val blockDefinition = generateBlock(programFuncDef.funcDef.body)

        val newFunction = MC.Function(
            name = programFuncDef.funcDef.name,
            contents = commands + blockDefinition.contents
        )

        return MC.Program(
            functions = listOf(newFunction) + blockDefinition.functions
        )
    }

    fun generateBlock(block: AST.Block): MC.Program {
        val contents: MutableMCCommands = mutableListOf()
        val functions: MutableList<MC.Function> = mutableListOf()

        block.statements.forEach { statement ->
            val lineProgram = generateStatment(statement)

            contents.addAll(lineProgram.contents)
            functions.addAll(lineProgram.functions)
        }

        return MC.Program(
            contents = contents,
            functions = functions
        )
    }

    fun generateStatment(statement: AST.Statement): MC.Program {
        return when (statement) {
            is AST.Statement.StatementAssignment -> generateStatmentAssignment(statement)
            is AST.Statement.StatementForStatement -> generateStatementForStatement(statement)
            is AST.Statement.StatementIfStatement -> generateStatmentIfStatement(statement)
<<<<<<< Updated upstream
            is AST.Statement.StatementWhileStatement -> generateStatmentWhileStatement(statement)
            is AST.Statement.StatementFuncCall -> generateExpressionFunctionCall(statement.assignment)
            is AST.Statement.StatementReturn -> TODO()
=======
            is AST.Statement.StatementWhileStatement -> generateStatementWhileStatement(statement)
            is AST.Statement.StatementFuncCall -> generateExpressionFunctionCall(statement.assignment, false)
>>>>>>> Stashed changes
        }
    }

    fun generateStatmentAssignment(statementAssignment: AST.Statement.StatementAssignment): MC.Program {
        val functionName = MC.createRandomId()

        val cmd =
            "execute store result storage ${MC.storageResourceLocation} ${statementAssignment.assignment.name} int 1 run ${
                MC.createFunctionCall(functionName)
            }"

        val expressionProgram = generateExpression(statementAssignment.assignment.expressions)

        val expressionFunction = MC.Function(
            name = functionName, contents = expressionProgram.contents,
        )

        return MC.Program(
            contents = listOf(cmd),
            functions = listOf(expressionFunction) + expressionProgram.functions
        )
    }

    fun generateStatementForStatement(forStatement: AST.Statement.StatementForStatement): MC.Program {
        return generateBlock(
            AST.Block(
                listOf(
                    AST.Statement.StatementAssignment(
                        AST.Assignment(
                            name = forStatement.forStatement.varName,
                            expressions = AST.Expression.ExpressionNumber(
                                forStatement.forStatement.range.first
                            )
                        )
                    ),
                    AST.Statement.StatementWhileStatement(
                        whileStatement = AST.WhileStatement(
                            condition = AST.Expression.ExpressionOperatorCall(
                                first = AST.Expression.ExpressionIdentifier(name = forStatement.forStatement.varName),
                                operator = "<",
                                second = AST.Expression.ExpressionNumber(forStatement.forStatement.range.second)
                            ),
                            body = AST.Block(
                                statements = forStatement.forStatement.body.statements + listOf(AST.Statement.StatementAssignment(
                                    assignment = AST.Assignment(
                                        name = forStatement.forStatement.varName,
                                        expressions = AST.Expression.ExpressionOperatorCall(
                                            first = AST.Expression.ExpressionIdentifier(name = forStatement.forStatement.varName),
                                            operator = "+",
                                            second = AST.Expression.ExpressionNumber(value = BigInteger.ONE)
                                        )
                                    )
                                ))
                            )
                        )
                    )
                )
            )
        )
    }

    fun generateStatementWhileStatement(statement: AST.Statement.StatementWhileStatement): MC.Program {
        val whileBodyName = MC.createRandomId()
        val whileBodyCmd = MC.createFunctionCall(whileBodyName)

        val whileConditionName = MC.createRandomId()
        val whileConditionExpression = statement.whileStatement.condition
        val whileCondition = generateExpression(whileConditionExpression)
        val whileConditionFunction = MC.Function(
            name = whileConditionName,
            contents = whileCondition.contents
        )

        val cmdCheckCondition = "execute unless ${MC.createFunctionCall(whileConditionName)} run return 1"

        val whileBody = generateBlock(statement.whileStatement.body)
        val whileBodyFunction = MC.Function(
            name = whileBodyName,
            contents = listOf(cmdCheckCondition) + whileBody.contents + listOf(whileBodyCmd)
        )

        return MC.Program(
            functions = whileCondition.functions + whileBody.functions + listOf(
                whileConditionFunction,
                whileBodyFunction
            ),
            contents = listOf(whileBodyCmd)
        )
    }

    fun generateStatmentIfStatement(statement: AST.Statement.StatementIfStatement): MC.Program {
        val ifFunctionName = MC.createRandomId()
        val ifFunctionCmd = MC.createFunctionCall(ifFunctionName)

        val conditionList = listOf(Pair(statement.ifStatement.condition, statement.ifStatement.ifBody))+statement.ifStatement.elifs

        val functionList: MutableList<MC.Function> = mutableListOf()
        val commands: MutableMCCommands = mutableListOf()

        conditionList.forEach { (ifConditionExpression, ifBodyBlock) ->
            val ifCondition = generateExpression(ifConditionExpression)
            val ifConditionFunctionName = MC.createRandomId()
            val ifConditionFunction = MC.Function(
                name = ifConditionFunctionName,
                contents = ifCondition.contents
            )

            functionList.addAll(ifCondition.functions)
            functionList.add(ifConditionFunction)

            val ifBodyFunctionName = MC.createRandomId()
            val ifBody = generateBlock(ifBodyBlock)
            val ifBodyFunction = MC.Function(
                name = ifBodyFunctionName,
                contents = ifBody.contents
            )

            functionList.addAll(ifBody.functions)
            functionList.add(ifBodyFunction)

            val ifCmd = "execute if ${MC.createFunctionCall(ifConditionFunctionName)} run return run ${
                MC.createFunctionCall(ifBodyFunctionName)
            }"

            commands.add(ifCmd)
        }

        if (statement.ifStatement.elseBody != null) {
            val elseBodyFunctionName = MC.createRandomId()
            val elseBody = generateBlock(statement.ifStatement.elseBody)
            val elseBodyFunction = MC.Function(
                name = elseBodyFunctionName,
                contents = elseBody.contents
            )

            functionList.addAll(elseBody.functions)
            functionList.add(elseBodyFunction)

            val elseCmd = "return run ${MC.createFunctionCall(elseBodyFunctionName)}"

            commands.add(elseCmd)
        }

        val ifFunction = MC.Function(
            name = ifFunctionName,
            contents = commands
        )

        functionList.add(ifFunction)

        return MC.Program(
            contents = listOf(ifFunctionCmd),
            functions = functionList
        )
    }

    fun generateExpression(expression: AST.Expression): MC.Program {
        return when (expression) {
            is AST.Expression.ExpressionNumber -> generateExpressionNumber(expression)
            is AST.Expression.ExpressionFuncCall -> generateExpressionFunctionCall(expression.call, true)
            is AST.Expression.ExpressionIdentifier -> generateExpressionIdentifier(expression)
            is AST.Expression.ExpressionParens -> generateExpressionParens(expression)
            is AST.Expression.ExpressionString -> TODO()
<<<<<<< Updated upstream
            is AST.Expression.ExpressionOperatorCall -> TODO()
=======
            is AST.Expression.ExpressionOperatorCall -> generateExpressionOperatorCall(expression) //!@#$%^&*==<>
>>>>>>> Stashed changes
        }
    }

    fun generateExpressionNumber(expressionNumber: AST.Expression.ExpressionNumber): MC.Program {
        val returnCmd = "return ${expressionNumber.value}"

        return MC.Program(
            contents = listOf(returnCmd)
        )
    }

    fun generateExpressionIdentifier(expressionIdentifier: AST.Expression.ExpressionIdentifier): MC.Program {
        val returnCmd = "return run data get storage ${MC.storageResourceLocation} ${expressionIdentifier.name} 1.0"

        return MC.Program(
            contents = listOf(returnCmd)
        )
    }

    fun generateExpressionParens(parenExpression: AST.Expression.ExpressionParens): MC.Program {
        return generateExpression(parenExpression.inner)
    }

    // Waiting for Erik to add Parameters
    fun generateExpressionFunctionCall(functionCall: AST.FuncCall, isExpression: Boolean): MC.Program {
        val funcCallParamUuid = MC.createRandomId()

        val functionList: MutableList<MC.Function> = mutableListOf()
        val commands: MutableMCCommands = mutableListOf()

        functionCall.parameters.forEachIndexed { i, paramExpression ->
            val param = generateExpression(paramExpression)
            val paramFunctionName = MC.createRandomId()
            val paramFunction = MC.Function(
                name = paramFunctionName,
                contents = param.contents
            )
            functionList.add(paramFunction)
            functionList.addAll(param.functions)

            val paramCmd = "execute store result storage codecraft:${funcCallParamUuid} p${i} int 1.0 run ${MC.createFunctionCall(paramFunctionName)}"

            commands.add(paramCmd)
        }
        val funcCallCmd = (when(isExpression) { true -> "return run "; false -> "" }) + "${MC.createFunctionCall(functionCall.funcName)} with storage codecraft:${funcCallParamUuid}"

        commands.add(funcCallCmd)

        return MC.Program(
            contents = commands,
            functions = functionList
        )
    }

    fun generateExpressionOperatorCall(expressionOperatorCall: AST.Expression.ExpressionOperatorCall): MC.Program {
        val params = listOf(expressionOperatorCall.first, expressionOperatorCall.second)

        return generateExpressionFunctionCall(when(expressionOperatorCall.operator) {
            "+" -> AST.FuncCall("add", params)
            "-" -> AST.FuncCall("sub", params)
            "*" -> AST.FuncCall("mult", params)
            "/" -> AST.FuncCall("div", params)
            "%" -> AST.FuncCall("mod", params)
            ">" -> AST.FuncCall("greater_than", params)
            ">=" -> AST.FuncCall("greater_than_equal", params)
            "<" -> AST.FuncCall("less_than", params)
            "<=" -> AST.FuncCall("less_than_equal", params)
            "==" -> AST.FuncCall("equal", params)
            "&&" -> AST.FuncCall("and", params)
            "||" -> AST.FuncCall("or", params)
            else -> TODO()
        }, true)
    }
}

//Success(t=Program(
// elements=[
// ProgramFuncDef(funcDef=FuncDef(name=test, parameters=[], body=Block(statements=[StatementAssignment(assignment=Assignment(name=x, expressions=ExpressionNumber(value=2)))])))
//
// ]
// ))