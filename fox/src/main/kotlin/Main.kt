import generator.Generator
import language.Lexer
import language.Parser
import java.io.File
import java.nio.file.Path

var source =
"""
    t = 0
    def loop():
        t = t + 1
        draw_circle()
        
    def draw_circle():
        for x in range(50):
            for y in range(50):
                for z in range(50):
                    sq_dist = ((x - 25) * (x - 25)) + ((y - 25) * (y - 25)) + ((z - 25) * (z - 25))
                    if sq_dist <= t * t:
                        set_block(x, y, z, "minecraft:diamond_block")
                    else:
                        set_block(x, y, z, "minecraft:air")
""".trimIndent()
fun main(args: Array<String>) {
    val sourceLocation = args.getOrNull(0)
    val resultingLocation = args.getOrNull(1) ?: "./"

    if(sourceLocation != null) {
        source = File(sourceLocation).readLines().joinToString("\n")
    }

    println(source)
    println(resultingLocation)

    val lexer = Lexer(source)
    val tokens = lexer.getAllTokens()
//    println(tokens.withIndex().joinToString { (x, i) -> "${i}: ${x}" })
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
