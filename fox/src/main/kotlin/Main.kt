import language.Lexer
import language.Parser
import language.Token

fun main() {
    val source =
        """
            def setup():
                x = 2
                y = 10
                if 10:
                    z = 15
                for i in 10:
                    while 10:
                        w = 100
            def loop():
                z = 10
                testing = 200
        """.trimIndent()
    val lexer = Lexer(source)
    val tokens = lexer.getAllTokens()
    println(tokens.withIndex().joinToString { (x, i) -> "${i}: ${x}" })
    val parser = Parser(tokens)
    println(parser.parse())
}
