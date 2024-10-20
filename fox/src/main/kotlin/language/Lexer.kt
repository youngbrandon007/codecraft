package language

class Lexer {
    private val source: String

    constructor(source: String) {
        this.source = preprocess(source)
    }

    var index = 0

    interface SingleLexer {
        fun tryGetOneToken(source: String, index: Int): SingleLexResult?
    }

    data class SingleLexResult(val t: Token, val newIndex: Int)

    class SingleCharLexer(private val c: Char, private val t: Token) : SingleLexer {
        override fun tryGetOneToken(source: String, index: Int): SingleLexResult? {
            return if (source[index] == c) {
                SingleLexResult(t, index + 1)
            } else {
                null
            }
        }
    }

    class KeywordLexer(private val keyword: String, private val token: Token) : SingleLexer {
        override fun tryGetOneToken(source: String, index: Int): SingleLexResult? {
            return if (index + keyword.length < source.length && source.substring(
                    index,
                    index + keyword.length
                ) == keyword
            ) {
                SingleLexResult(token, index + keyword.length)
            } else {
                null
            }
        }
    }

    class OperatorLexer : SingleLexer {
        override fun tryGetOneToken(source: String, index: Int): SingleLexResult? {
            val s = "!@#$%^&*/=+-.<>|~"
            var endIndex = index
            while (endIndex < source.length && source[endIndex] in s) {
                endIndex += 1
            }
            return if (endIndex == index) {
                null
            } else {
                SingleLexResult(Token.ExpressionOperator(source.substring(index, endIndex)), endIndex)
            }
        }
    }

    class IdentifierLexer : SingleLexer {
        override fun tryGetOneToken(source: String, index: Int): SingleLexResult? {
            val s = "abcdefghijklmnopqrstuvwxyz_"
            var endIndex = index
            while (endIndex < source.length && source[endIndex] in s) {
                endIndex += 1
            }
            return if (endIndex == index) {
                null
            } else {
                SingleLexResult(Token.Identifier(source.substring(index, endIndex)), endIndex)
            }
        }
    }

    class TabLexer : SingleLexer {
        override fun tryGetOneToken(source: String, index: Int): SingleLexResult? {
            return if (source[index] == '\n') {
                var endIndex = index + 1
                while (source[endIndex] == ' ') {
                    endIndex += 1
                }
                val numSpaces = endIndex - index - 1
                val indents = numSpaces.div(4)
                if (endIndex == index) {
                    null
                } else {
                    SingleLexResult(Token.Tabs(indents), endIndex)
                }
            } else {
                null
            }
        }
    }

    class NumberLexer() : SingleLexer {
        override fun tryGetOneToken(source: String, index: Int): SingleLexResult? {
            val s = "0123456789"
            var endIndex = index
            while (endIndex < source.length && source[endIndex] in s) {
                endIndex += 1
            }
            return if (endIndex == index) {
                null
            } else {
                SingleLexResult(Token.Number(source.substring(index, endIndex).toBigInteger()), endIndex)
            }
        }
    }

    class SpaceLexer : SingleLexer {
        override fun tryGetOneToken(source: String, index: Int): SingleLexResult? {
            var endIndex = index
            while (endIndex < source.length && source[endIndex] == ' ') {
                endIndex += 1
            }
            return if (endIndex == index) {
                null
            } else {
                SingleLexResult(Token.Space(endIndex - index), endIndex)
            }
        }

    }

    class CatchAllLexer : SingleLexer {
        override fun tryGetOneToken(source: String, index: Int): SingleLexResult {
            return SingleLexResult(Token.Unknown(source.substring(index, source.length)), source.length)
        }
    }

    val singleLexers: List<SingleLexer> = listOf(
        KeywordLexer("if", Token.IfKeyword),
        KeywordLexer("elif", Token.ElifKeyword),
        KeywordLexer("else", Token.ElseKeyword),
        KeywordLexer("while", Token.WhileKeyword),
        KeywordLexer("for", Token.ForKeyword),
        KeywordLexer("return", Token.ReturnKeyword),
        KeywordLexer("def", Token.DefKeyword),
        SingleCharLexer('(', Token.LeftParens),
        SingleCharLexer(')', Token.RightParens),
        SingleCharLexer(':', Token.Colon),
        SingleCharLexer(',', Token.Comma),
        SingleCharLexer('"', Token.Quote),
        OperatorLexer(),
        IdentifierLexer(),
        TabLexer(),
        NumberLexer(),
        SpaceLexer(),
        CatchAllLexer(),
    )

    fun getOneToken(): Token {
        for (lexer in singleLexers) {
            val result = lexer.tryGetOneToken(source, index)
            if (result != null) {
                index = result.newIndex
                return result.t
            }
        }
        throw Exception("CAN'T HAPPEN!")
    }

    fun fix(tokens: List<Token>): List<Token> {
        val result = mutableListOf<Token>()
        var currentIndentLevel = 0
        tokens.forEach { token ->
            when (token) {
                is Token.Tabs -> {
                    val diff = token.indents - currentIndentLevel
                    if (diff > 0) {
                        for (i in 1..diff) {
                            result.add(Token.Indent)
                        }
                    } else if (diff < 0) {
                        for (i in 1..-diff) {
                            result.add(Token.Dedent)
                        }
                    }
                    currentIndentLevel = token.indents
                }
                is Token.Space -> {}
                else -> result.add(token)
            }
        }
        return result
    }

    fun hasNext(): Boolean {
        return index < source.length
    }

    fun preprocess(source: String) : String {
        return source.split("\n").filter {
            !(it.all { it == ' ' })
        }.joinToString("\n")
    }

    fun getAllTokens(): List<Token> {
        val tokens = mutableListOf<Token>();
        while (hasNext()) {
            tokens.add(getOneToken())
        }
        return fix(tokens)
    }
}
