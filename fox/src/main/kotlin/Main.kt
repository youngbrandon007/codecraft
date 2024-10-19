//import generator.Generator
import generator.Generator
import language.Lexer
import language.Parser
import language.Token
import java.io.File
import java.nio.file.Path

var source =
"""def test():
    x = 2"""
fun main(args: Array<String>) {
    val sourceLocation = args.getOrNull(0)
    val resultingLocation = args.getOrNull(1) ?: "./"

    if(sourceLocation != null) {
        source = File(sourceLocation).readLines().joinToString("\n")
    }

    println(source)
    println(resultingLocation)

    val lexer = Lexer(source)
    val tokens = lexer.clean(lexer.getAllTokens()).filter { it !is Token.Space }
    val parser = Parser(tokens)

    val result = parser.parse()

    println(result)

    if(result is Parser.ParseResult.Success) {
        val mcProgram = Generator.generateProgram(result.t)

        println(mcProgram)
        
        mcProgram.functions.forEach { function ->
            val filePath = Path.of(resultingLocation, function.name + ".mcfunction")

//            println(filePath)

            val functionStr = function.contents.joinToString("\n")

            File(filePath.toUri()).writeText(functionStr)
        }
    }
}
