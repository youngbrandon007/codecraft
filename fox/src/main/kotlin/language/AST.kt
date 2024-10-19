package language

import java.math.BigInteger

object AST {
    data class Program(val elements: List<ProgramElement>)

    sealed class ProgramElement {
        data class ProgramStatement(val statement: Statement) : ProgramElement()
        data class ProgramFuncDef(val funcDef: FuncDef) : ProgramElement()
    }

    sealed class Statement {
        data class StatementAssignment(val assignment: Assignment) : Statement()
//        data class StatementFuncCall(val assignment: FuncCall) : Statement()
        data class StatementIfStatement(val ifStatement: IfStatement) : Statement()
        data class StatementWhileStatement(val whileStatement: WhileStatement) : Statement()
        data class StatementForStatement(val forStatement: ForStatement) : Statement()
    }

    data class IfStatement(val condition: Expression, val ifBody: Block, val elifBodies: List<Block>?, val elseBody: Block?)
    data class WhileStatement(val condition: Expression, val body: Block)
    data class ForStatement(val varName: String, val iterator: Expression, val body: Block)

    data class Block(val statements: List<Statement>)

    data class FuncDef(val name: String, val parameters: List<FuncParam>, val body: Block)

    data class FuncCall(val funcName: String, val parameters: List<Expression>)

    data class FuncParam(val name: String)

    data class Assignment(val name: String, val expressions: Expression)

    sealed class Expression {
//        data class ExpressionParens(val inner: Expression): Expression()
//        data class ExpressionFuncCall(val call: FuncCall): Expression()
//        data class ExpressionOperatorCall(val operator: String, val first: Expression, val second: Expression): Expression()
//        data class ExpressionDotCallParams(val target: Expression, val name: String, val parameters: List<Expression>): Expression()
//        data class ExpressionDotCall(val target: Expression, val name: String): Expression()
        data class ExpressionNumber(val value: BigInteger): Expression()
//        data class ExpressionString(val value: BigInteger): Expression()
    }
}


/*

def main(x, y):
    # the main function!
    z = f(x, y)
    if x == y:
        g(x)
    elif y + x == z:
        z += f(x + f(x, y), y)
        return z.prop + z.call(x, y, z.z.z)
    else:
        return

    for i in range(100):
        j = 10
        while j < 1000:
           j *= 10

main(10, 20)

*/