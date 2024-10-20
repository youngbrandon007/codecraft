package generator

import language.AST
import java.util.UUID

typealias MutableMCCommands = MutableList<String>
typealias MCCommands = List<String>


object MC {

    data class Function (
        val name: String,
        val contents: MCCommands = emptyList(),
    )

    data class Program (
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

        val loadFunction = MC.Function(
            name = MC.loadFunctionName,
            contents = contents
        )

        return MC.Program(
            functions = functions + listOf(loadFunction)
        )
    }

    fun generateProgramElement(programElement: AST.ProgramElement): MC.Program {
        return when(programElement) {
            is AST.ProgramElement.ProgramStatement -> generateProgramStatement(programElement)
            is AST.ProgramElement.ProgramFuncDef -> generateProgramFuncDef(programElement)
        }
    }

    fun generateProgramStatement(programStatement: AST.ProgramElement.ProgramStatement): MC.Program {
        return generateStatment(programStatement.statement)
    }

    fun generateProgramFuncDef(programFuncDef: AST.ProgramElement.ProgramFuncDef): MC.Program {
        val blockDefinition = generateBlock(programFuncDef.funcDef.body)

        val newFunction = MC.Function(
            name = programFuncDef.funcDef.name,
            contents = blockDefinition.contents
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
        return when(statement) {
            is AST.Statement.StatementAssignment -> generateStatmentAssignment(statement)
            is AST.Statement.StatementForStatement -> TODO()
            is AST.Statement.StatementIfStatement -> generateStatmentIfStatement(statement)
            is AST.Statement.StatementWhileStatement -> generateStatmentWhileStatement(statement)
        }
    }

    fun generateStatmentAssignment(statementAssignment: AST.Statement.StatementAssignment): MC.Program {
        val functionName = MC.createRandomId()

        val cmd = "execute store result storage ${MC.storageResourceLocation} ${statementAssignment.assignment.name} int 1 run ${MC.createFunctionCall(functionName)}"

        val expressionProgram = generateExpression(statementAssignment.assignment.expressions)

        val expressionFunction = MC.Function( name = functionName, contents = expressionProgram.contents,
        )

        return MC.Program(
            contents = listOf(cmd),
            functions = listOf(expressionFunction) + expressionProgram.functions
        )
    }

    fun generateStatmentWhileStatement(statement: AST.Statement.StatementWhileStatement): MC.Program {
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
            functions = whileCondition.functions + whileBody.functions + listOf(whileConditionFunction, whileBodyFunction),
            contents = listOf(whileBodyCmd)
        )
    }

    fun generateStatmentIfStatement(statement: AST.Statement.StatementIfStatement): MC.Program {
        val ifFunctionName = MC.createRandomId()
        val ifFunctionCmd = MC.createFunctionCall(ifFunctionName)

        val conditionList = listOf(Pair(statement.ifStatement.condition, statement.ifStatement.ifBody))

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

            val ifCmd = "execute if ${MC.createFunctionCall(ifConditionFunctionName)} run return run ${MC.createFunctionCall(ifBodyFunctionName)}"

            commands.add(ifCmd)
        }

        if(statement.ifStatement.elseBody != null) {
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
        return when(expression) {
            is AST.Expression.ExpressionNumber -> generateExpressionNumber(expression)
        }
    }

    fun generateExpressionNumber(expressionNumber: AST.Expression.ExpressionNumber): MC.Program {
        val returnCmd = "return " + expressionNumber.value.toString()

        return MC.Program(
            contents = listOf(returnCmd)
        )
    }
}

//Success(t=Program(
// elements=[
// ProgramFuncDef(funcDef=FuncDef(name=test, parameters=[], body=Block(statements=[StatementAssignment(assignment=Assignment(name=x, expressions=ExpressionNumber(value=2)))])))
//
// ]
// ))