package io.github.landerlyoung.flashappsearch.search.repo

import io.github.landerlyoung.flashappsearch.search.model.Input
import kotlin.math.pow

object MatchScoreCalculator {

    /**
     * calculate how good input matches pinyinName
     */
    fun calculateMatchResult(input: List<Input>, pinyinName: PinyinSequence): Double {
        var total = 0.0

        var window = input
        val length = pinyinName.pinyin.size
        var index = 0

        var matchedUnitCount = 0

        while (window.isNotEmpty() && index < length) {
            val pinyin = pinyinName.pinyin[index]
            val (newWindow, score) = calculateMatchScoreForOneUnit(window, pinyin)
            val multiplier = (10 - index).coerceAtLeast(1)
            total += score * multiplier

            window = newWindow
            index++

            if (score > 0) {
                matchedUnitCount++
            }
        }

        // all matched !!! bonus
        if (window.isEmpty() && matchedUnitCount == pinyinName.pinyin.size) {
            total *= 10 * matchedUnitCount
        }

        return total
    }

    /**
     * @param input input keys
     * @param pinyin the name
     * @return (newWindow, score)
     */
    private fun calculateMatchScoreForOneUnit(
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
                        val score = (10 - i).coerceAtLeast(1).toDouble().pow(2)
                        total += score
                        inputIndex++
                    }
                }
            }
            input.subList(inputIndex, input.size) to total
        }.maxBy { it.second }
    }

}