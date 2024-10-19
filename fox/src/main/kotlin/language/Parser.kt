package language

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

    private fun parseStatement(): ParseResult<AST.Statement> {
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
        } else {
            return ParseResult.Failure("non-assignment statement not implemented :(", index);
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
            else -> return ParseResult.Failure("must be an identifier", index);
        }
        skip()
        skip()
        skip()
        val body = when (val body = parseBlock()) {
            is ParseResult.Success -> body.t
            is ParseResult.Failure -> return retype(body)
        }
        return ParseResult.Success(AST.FuncDef(name, listOf(), body))
    }

    fun parseExpression(): ParseResult<AST.Expression> {
        when (val t = currentToken()) {
            is Token.Number -> {
                skip()
                return ParseResult.Success(AST.Expression.ExpressionNumber(t.n))
            } else -> {
                return ParseResult.Failure("non-int expr not implemented :(", index);
            }
        }
    }

    fun parseBlock(): ParseResult<AST.Block> {
        val statements = mutableListOf<AST.Statement>()
        while (hasNext() && currentToken() is Token.Tabs) {
            skip()
            val statement = when (val statement = parseStatement()) {
                is ParseResult.Success -> statement.t
                is ParseResult.Failure -> return retype(statement)
            }
            statements.add(statement)
        }
        return ParseResult.Success(AST.Block(statements))
    }

    fun parse(): ParseResult<AST.Program> {
        return parseProgram()
    }
}
