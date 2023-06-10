package io.github.landerlyoung.flashappsearch.search.repo

import androidx.annotation.VisibleForTesting
import io.github.landerlyoung.flashappsearch.search.model.Input
import kotlin.math.pow

object MatchScoreCalculator {
    private val fibos = DoubleArray(64)

    init {
        fibos[fibos.size - 1] = 1.toDouble()
        fibos[fibos.size - 2] = 2.toDouble()
        for (i in (0..fibos.size - 3).reversed()) {
            fibos[i] = fibos[i + 1] + fibos[i + 2]
        }
    }

    private fun indexMultiplier(index: Int): Double {
        if (index >= fibos.size) return 0.1
        return fibos[index]
    }

    /**
     * calculate how good input matches pinyinName
     */
    fun calculateMatchResult(input: List<Input>, pinyinName: PinyinSequence): Double {
        var total = 0.0

        var window = input
        val length = pinyinName.pinyin.size
        var index = 0

        var matchedUnit = 0

        while (window.isNotEmpty() && index < length) {
            val (r, score) = calculateMatchScoreForOneUnit(window, pinyinName.pinyin[index])
            val multiplier = indexMultiplier(index)
            total += score * multiplier

            window = r
            index++

            if (score > 0) {
                matchedUnit++
            }
        }

        // all matched !!!
        // bonus
        if (matchedUnit == pinyinName.pinyin.size) {
            total *= 10
        }

        return total
    }

    @VisibleForTesting
    internal fun calculateMatchScoreForOneUnit(
        input: List<Input>,
        pinyin: Pinyin
    ): Pair<List<Input>, Double> {
        // calculate for every reading, and choose the best one
        return pinyin.readings.map { reading ->
            var inputIndex = 0
            var total = 0.0
            reading.forEachIndexed { i, c ->
                if (inputIndex < input.size) {
                    val inputKey = input[inputIndex]
                    if (inputKey.keySets.contains(c)) {
                        val score = indexMultiplier(i)
                        total += score
                        inputIndex++
                    }
                }
            }
            input.subList(inputIndex, input.size) to total
        }.maxBy { it.second }
    }
}