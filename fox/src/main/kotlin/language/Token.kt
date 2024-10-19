package language

import java.math.BigInteger

sealed class Token {
    object LeftParens : Token()
    object RightParens : Token()
    object Colon : Token()
    object Comma : Token()
    object IfKeyword : Token()
    object ElifKeyword : Token()
    object ElseKeyword : Token()
    object WhileKeyword : Token()
    object ForKeyword : Token()
    object ReturnKeyword : Token()
    object DefKeyword : Token()
    data class ExpressionOperator(val op: String) : Token()
    data class Identifier(val name: String) : Token()
    data class Tabs(val indents: Int) : Token()
    data class Number(val n: BigInteger) : Token()
    data class Unknown(val content: String) : Token()
    data class Space(val num: Int) : Token()
}