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
        }
    }

    fun generateStatmentAssignment(statementAssignment: AST.Statement.StatementAssignment): MC.Program {
        val functionName = MC.createRandomId()

        val cmd = "execute store result storage ${MC.storageResourceLocation} ${statementAssignment.assignment.name} int 1 run ${MC.createFunctionCall(functionName)}"

        val expressionProgram = generateExpression(statementAssignment.assignment.expressions)

        val expressionFunction = MC.Function(
            name = functionName,
            contents = expressionProgram.contents,
        )

        return MC.Program(
            contents = listOf(cmd),
            functions = listOf(expressionFunction) + expressionProgram.functions
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