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
        return ParseResult.Success(AST.IfStatement(condition, body, null, null))
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
        skip()
        val body = when (val body = parseBlock()) {
            is ParseResult.Success -> body.t
            is ParseResult.Failure -> return retype(body)
        }
        return ParseResult.Success(AST.ForStatement(varName, iterator, body))
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
                return fail("non-int expr not implemented :(");
            }
        }
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
