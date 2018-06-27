package io.github.landerlyoung.flashappsearch.search.model

/**
 * <pre>
 * Author: taylorcyang@tencent.com
 * Date:   2018-06-23
 * Time:   12:41
 * Life with Passion, Code with Creativity.
 * </pre>
 */
data class Input(val keys: List<Char>) {
    constructor(keys: String) : this(keys.toCharArray().map { it.toLowerCase() })
    constructor(char: Char) : this(listOf(char.toLowerCase()))

    companion object {
        @JvmField
        val emptyInput = Input("")
    }
}

object T9 {
    @JvmField
    val k0 = Input("0")
    @JvmField
    val k1 = Input("1")
    @JvmField
    val k2 = Input("2ABC")
    @JvmField
    val k3 = Input("3DEF")
    @JvmField
    val k4 = Input("4GHI")
    @JvmField
    val k5 = Input("5JKL")
    @JvmField
    val k6 = Input("6MNO")
    @JvmField
    val k7 = Input("7PQRS")
    @JvmField
    val k8 = Input("8TUV")
    @JvmField
    val k9 = Input("9WXYZ")

    @JvmField
    val kDelete = Input("\u232b") // ⌫
}

object QWERT {
    @JvmField
    val kQ = Input("Q1")
    @JvmField
    val kW = Input("W2")
    @JvmField
    val kE = Input("E3")
    @JvmField
    val kR = Input("R4")
    @JvmField
    val kT = Input("T5")
    @JvmField
    val kY = Input("Y6")
    @JvmField
    val kU = Input("U7")
    @JvmField
    val kI = Input("I8")
    @JvmField
    val kO = Input("O9")
    @JvmField
    val kP = Input("P0")
    @JvmField
    val kA = Input('A')
    @JvmField
    val kS = Input('S')
    @JvmField
    val kD = Input('D')
    @JvmField
    val kF = Input('F')
    @JvmField
    val kG = Input('G')
    @JvmField
    val kH = Input('H')
    @JvmField
    val kJ = Input('J')
    @JvmField
    val kK = Input('K')
    @JvmField
    val kL = Input('L')
    @JvmField
    val kZ = Input('Z')
    @JvmField
    val kX = Input('X')
    @JvmField
    val kC = Input('C')
    @JvmField
    val kV = Input('V')
    @JvmField
    val kB = Input('B')
    @JvmField
    val kN = Input('N')
    @JvmField
    val kM = Input('M')
}

