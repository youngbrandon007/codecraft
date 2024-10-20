import language.Lexer
import language.Parser
import language.Token

fun main() {
    val source =
        """
            def setup():
                x = f(x, y)
                z = g(x, f(g(x, x, x)), h())
                if g(((x)), f(g(x, x, x)), h()):
                    z = (((g(x, f(g(x, x, x)), h()))))
        """.trimIndent()
    val lexer = Lexer(source)
    val tokens = lexer.getAllTokens()
    println(tokens.withIndex().joinToString { (x, i) -> "${i}: ${x}" })
    val parser = Parser(tokens)
    println(parser.parse())
}
