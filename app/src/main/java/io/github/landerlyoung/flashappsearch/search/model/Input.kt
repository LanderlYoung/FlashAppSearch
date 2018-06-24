package io.github.landerlyoung.flashappsearch.search.model

import java.util.*

/**
 * <pre>
 * Author: taylorcyang@tencent.com
 * Date:   2018-06-23
 * Time:   12:41
 * Life with Passion, Code with Creativity.
 * </pre>
 */
data class Input(val keys: CharArray) {
    constructor(keys: String) : this(keys.toCharArray())
    constructor(char: Char) : this(charArrayOf(char))

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Input) return false

        if (!Arrays.equals(keys, other.keys)) return false

        return true
    }

    override fun hashCode(): Int {
        return Arrays.hashCode(keys)
    }

    companion object {
        val emptyInput = Input("")
    }
}

object T9 {
    val k0 = Input("0")
    val k1 = Input("1")
    val k2 = Input("2ABC")
    val k3 = Input("3DEF")
    val k4 = Input("4GHI")
    val k5 = Input("5JKL")
    val k6 = Input("6MNO")
    val k7 = Input("7PQRS")
    val k8 = Input("8TUV")
    val k9 = Input("9WXYZ")
}

object QWERT {
    val k1 = Input('1')
    val k2 = Input('2')
    val k3 = Input('3')
    val k4 = Input('4')
    val k5 = Input('5')
    val k6 = Input('6')
    val k7 = Input('7')
    val k8 = Input('8')
    val k9 = Input('9')
    val k0 = Input('0')
    val kQ = Input('Q')
    val kW = Input('W')
    val kE = Input('E')
    val kR = Input('R')
    val kT = Input('T')
    val kY = Input('Y')
    val kU = Input('U')
    val kI = Input('I')
    val kO = Input('O')
    val kP = Input('P')
    val kA = Input('A')
    val kS = Input('S')
    val kD = Input('D')
    val kF = Input('F')
    val kG = Input('G')
    val kH = Input('H')
    val kJ = Input('J')
    val kK = Input('K')
    val kL = Input('L')
    val kZ = Input('Z')
    val kX = Input('X')
    val kC = Input('C')
    val kV = Input('V')
    val kB = Input('B')
    val kN = Input('N')
    val kM = Input('M')
}

