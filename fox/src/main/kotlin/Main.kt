import language.Lexer
import language.Parser
import language.Token

fun main() {
    val source =
        """
            def setup():
                x = 2
                y = 10
            def loop():
                z = 10
                testing = 200
        """.trimIndent()
    val lexer = Lexer(source)
    val tokens = lexer.clean(lexer.getAllTokens()).filter { it !is Token.Space }
    val parser = Parser(tokens)
    println(tokens)
    println(parser.parse())
}
