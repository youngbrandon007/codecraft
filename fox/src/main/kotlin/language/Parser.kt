package language

import java.math.BigInteger

class Parser(val tokens: List<Token>) {
    var index = 0

    fun hasNext(): Boolean {
        return index < tokens.size
    }

    fun currentToken() : Token {
        return tokens[index]
    }

    fun skip() {
        index++
    }

    fun consumeToken() : Token {
        val t = tokens[index]
        index++
        return t
    }

    fun <T> fail(message: String) : ParseResult<T> {
        return ParseResult.Failure<T>(message, index)
    }

    sealed class ParseResult<T> {
        data class Success<T>(val t: T) : ParseResult<T>()
        data class Failure<T>(val errorMessage: String, val errorIndex: Int) : ParseResult<T>()
    }

    fun <T, U> retype(failure: ParseResult.Failure<T>) : ParseResult.Failure<U> {
        return ParseResult.Failure<U>(failure.errorMessage, failure.errorIndex)
    }

    fun <T> getMany(parser: () -> ParseResult<T>) : ParseResult<List<T>> {
        val elements = mutableListOf<T>()
        while (hasNext()) {
            when (val pe = parser()) {
                is ParseResult.Success -> {
                    elements.add(pe.t)
                }
                is ParseResult.Failure -> {
                    return retype(pe)
                }
            }
        }
        return ParseResult.Success(elements)
    }

    fun parseProgram(): ParseResult<AST.Program> {
        return when (val elements = getMany(::parseProgramElement)) {
            is ParseResult.Success -> {
                ParseResult.Success(AST.Program(elements.t))
            }
            is ParseResult.Failure -> {
                retype(elements)
            }
        }
    }

    fun parseProgramElement(): ParseResult<AST.ProgramElement> {
        when (currentToken()) {
            is Token.DefKeyword -> {
                when (val fd = parseFuncDef()) {
                    is ParseResult.Success -> return ParseResult.Success(AST.ProgramElement.ProgramFuncDef(fd.t))
                    is ParseResult.Failure -> return retype(fd)
                }
            }
            else -> {
                when (val statement = parseStatement()) {
                    is ParseResult.Success -> return ParseResult.Success(AST.ProgramElement.ProgramStatement(statement.t))
                    is ParseResult.Failure -> return retype(statement)
                }
            }
        }
    }

    private fun parseIfStatement(): ParseResult<AST.IfStatement> {
        skip()
        val condition = when (val condition = parseExpression()) {
            is ParseResult.Success -> condition.t
            is ParseResult.Failure -> return retype(condition)
        }
        skip()
        val body = when (val body = parseBlock()) {
            is ParseResult.Success -> body.t
            is ParseResult.Failure -> return retype(body)
        }
        val elifs = mutableListOf<Pair<AST.Expression, AST.Block>>()
        while (hasNext() && currentToken() == Token.ElifKeyword) {
            skip()
            val condition = when (val condition = parseExpression()) {
                is ParseResult.Success -> condition.t
                is ParseResult.Failure -> return retype(condition)
            }
            skip()
            val body = when (val body = parseBlock()) {
                is ParseResult.Success -> body.t
                is ParseResult.Failure -> return retype(body)
            }
            elifs.add(Pair(condition, body))
        }
        val elseBody =
            if (hasNext() && currentToken() == Token.ElseKeyword) {
                skip()
                skip()
                when (val body = parseBlock()) {
                    is ParseResult.Success -> body.t
                    is ParseResult.Failure -> return retype(body)
                }
            } else {
                null
            }
        return ParseResult.Success(AST.IfStatement(condition, body, elifs, elseBody))
    }

    private fun parseWhileStatement(): ParseResult<AST.WhileStatement> {
        skip()
        val condition = when (val condition = parseExpression()) {
            is ParseResult.Success -> condition.t
            is ParseResult.Failure -> return retype(condition)
        }
        skip()
        val body = when (val body = parseBlock()) {
            is ParseResult.Success -> body.t
            is ParseResult.Failure -> return retype(body)
        }
        return ParseResult.Success(AST.WhileStatement(condition, body))
    }

    private fun parseForStatement(): ParseResult<AST.ForStatement> {
        skip()
        if (currentToken() !is Token.Identifier) {
            return fail("only identifiers are allowed after for.")
        }
        val varName = (consumeToken() as Token.Identifier).name
        skip()
        val iterator = when (val iterator = parseExpression()) {
            is ParseResult.Success -> iterator.t
            is ParseResult.Failure -> return retype(iterator)
        }
        val range: Pair<BigInteger, BigInteger> = when (iterator) {
            is AST.Expression.ExpressionFuncCall -> {
                if (iterator.call.parameters.size == 1) {
                    Pair(BigInteger.ZERO, (iterator.call.parameters[0] as AST.Expression.ExpressionNumber).value)
                } else if (iterator.call.parameters.size == 2) {
                    Pair((iterator.call.parameters[0] as AST.Expression.ExpressionNumber).value, (iterator.call.parameters[1] as AST.Expression.ExpressionNumber).value)
                } else {
                    return fail("range may only contain two parameters.")
                }
            }
            else -> {
                return fail("you may only use range(...) calls in for loop iterators.")
            }
        }
        skip()
        val body = when (val body = parseBlock()) {
            is ParseResult.Success -> body.t
            is ParseResult.Failure -> return retype(body)
        }
        return ParseResult.Success(AST.ForStatement(varName, range, body))
    }

    private fun parseStatement(): ParseResult<AST.Statement> {
        val token = currentToken()
        when (token) {
            is Token.IfKeyword -> {
                return when (val ifs = parseIfStatement()) {
                    is ParseResult.Success -> ParseResult.Success(AST.Statement.StatementIfStatement(ifs.t))
                    is ParseResult.Failure -> retype(ifs)
                }
            }
            is Token.WhileKeyword -> {
                return when (val whiles = parseWhileStatement()) {
                    is ParseResult.Success -> ParseResult.Success(AST.Statement.StatementWhileStatement(whiles.t))
                    is ParseResult.Failure -> retype(whiles)
                }
            }
            is Token.ForKeyword -> {
                return when (val fors = parseForStatement()) {
                    is ParseResult.Success -> ParseResult.Success(AST.Statement.StatementForStatement(fors.t))
                    is ParseResult.Failure -> retype(fors)
                }
            }
            is Token.ReturnKeyword -> {
                skip()
                return when (val expr = parseExpression()) {
                    is ParseResult.Success -> ParseResult.Success(AST.Statement.StatementReturn(expr.t))
                    is ParseResult.Failure -> retype(expr)
                }
            }
            else -> {
                if (index + 1 < tokens.size && tokens[index] is Token.Identifier && tokens[index + 1] == Token.ExpressionOperator("=")) {
                    val name = (consumeToken() as Token.Identifier).name
                    skip()
                    val expr = when (val expr = parseExpression()) {
                        is ParseResult.Success -> {
                            expr.t
                        }
                        is ParseResult.Failure -> {
                            return retype(expr)
                        }
                    }
                    return ParseResult.Success(AST.Statement.StatementAssignment(AST.Assignment(name, expr)))
                } else if (index + 1 < tokens.size && tokens[index] is Token.Identifier && tokens[index + 1] == Token.LeftParens) {
                    return when (val fc = parseFuncCall()) {
                        is ParseResult.Success -> ParseResult.Success(AST.Statement.StatementFuncCall(fc.t))
                        is ParseResult.Failure -> retype(fc)
                    }
                } else {
                    return fail("this statement is not implemented :(");
                }
            }
        }
    }

//    private fun parseIfStatement(): ParseResult<AST.IfStatement> {
//        skip()
//        val condition = when (val c = parseExpression()) {
//            is ParseResult.Success -> c
//            is ParseResult.Failure -> return retype(c)
//        }
//        skip()
////        parseBlock()
//        throw Exception("not implemented :(")
//    }

    fun parseFuncDef(): ParseResult<AST.FuncDef> {
        skip()
        val name = when (val t = consumeToken()) {
            is Token.Identifier -> t.name
            else -> return fail("must be an identifier");
        }
        skip()
        val params = mutableListOf<AST.FuncParam>()
        while (hasNext() && currentToken() != Token.RightParens) {
            val name = (consumeToken() as Token.Identifier).name
            params.add(AST.FuncParam(name))
            if (hasNext() && currentToken() == Token.Comma) {
                skip()
            }
        }
        skip()
        skip()
        val body = when (val body = parseBlock()) {
            is ParseResult.Success -> body.t
            is ParseResult.Failure -> return retype(body)
        }
        return ParseResult.Success(AST.FuncDef(name, params, body))
    }

    fun parseExpression(): ParseResult<AST.Expression> {
        var result: ParseResult<AST.Expression> = when (val t = currentToken()) {
            is Token.Number -> {
                skip()
                ParseResult.Success(AST.Expression.ExpressionNumber(t.n))
            }
            is Token.LeftParens -> {
                skip()
                val inner = when (val inner = parseExpression()) {
                    is ParseResult.Success -> inner.t
                    is ParseResult.Failure -> return retype(inner)
                }
                skip()
                ParseResult.Success(AST.Expression.ExpressionParens(inner))
            }
            is Token.Identifier -> {
                if (index + 1 < tokens.size && tokens[index + 1] == Token.LeftParens) {
                    val funcCall = when (val funcCall = parseFuncCall()) {
                        is ParseResult.Success -> funcCall.t
                        is ParseResult.Failure -> return retype(funcCall)
                    }
                    ParseResult.Success(AST.Expression.ExpressionFuncCall(funcCall))
                } else {
                    skip()
                    ParseResult.Success(AST.Expression.ExpressionIdentifier(t.name))
                }
            }
            is Token.StringToken -> {
                skip()
                ParseResult.Success(AST.Expression.ExpressionString(t.string))
            }
            else -> {
                return fail("expr not implemented :(");
            }
        }
        while (hasNext() && currentToken() is Token.ExpressionOperator) {
            val operator = (consumeToken() as Token.ExpressionOperator).op
            val expr = when (val expr = parseExpression()) {
                is ParseResult.Success -> expr.t
                is ParseResult.Failure -> return retype(expr)
            }
            result = ParseResult.Success(AST.Expression.ExpressionOperatorCall(operator, (result as ParseResult.Success).t, expr))
        }
        return result
    }

    private fun parseFuncCall(): ParseResult<AST.FuncCall> {
        val name = (consumeToken() as Token.Identifier).name
        skip()
        val exprs = mutableListOf<AST.Expression>()
        while (hasNext() && currentToken() !is Token.RightParens) {
            val param = when (val param = parseExpression()) {
                is ParseResult.Success -> param.t
                is ParseResult.Failure -> return retype(param)
            }
            exprs.add(param)
            if (hasNext() && currentToken() is Token.Comma) {
                skip()
            }
        }
        skip()
        return ParseResult.Success(AST.FuncCall(name, exprs))
    }

    fun parseBlock(): ParseResult<AST.Block> {
        skip()
        val statements = mutableListOf<AST.Statement>()
        while (hasNext() && currentToken() !is Token.Dedent) {
            val statement = when (val statement = parseStatement()) {
                is ParseResult.Success -> statement.t
                is ParseResult.Failure -> return retype(statement)
            }
            statements.add(statement)
        }
        skip()
        return ParseResult.Success(AST.Block(statements))
    }

    fun parse(): ParseResult<AST.Program> {
        return parseProgram()
    }
}
