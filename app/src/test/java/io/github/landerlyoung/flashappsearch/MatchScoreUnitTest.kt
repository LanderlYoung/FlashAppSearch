package io.github.landerlyoung.flashappsearch

import io.github.landerlyoung.flashappsearch.search.model.T9
import io.github.landerlyoung.flashappsearch.search.repo.MatchScoreCalculator
import io.github.landerlyoung.flashappsearch.search.repo.Pinyin
import io.github.landerlyoung.flashappsearch.search.repo.PinyinSequence
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class MatchScoreUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    /**
     * search result keys:[Input(keys=[9, w, x, y, z]), Input(keys=[9, w, x, y, z]), Input(keys=[4, g, h, i]), Input(keys=[5, j, k, l])]
     *
     * list:[(AppInfo(packageName=com.miui.gallery, name=相册, pinyin=PinyinSequence(
     *     pinyin=[Pinyin(readings=[xiang]), Pinyin(readings=[zha, ce])])), 49322.0),
     *
     * (AppInfo(packageName=com.android.providers.downloads.ui, name=下载管理, pinyin=PinyinSequence(
     *     pinyin=[Pinyin(readings=[xia]), Pinyin(readings=[zai]), Pinyin(readings=[guan]), Pinyin(readings=[li])])), 28184.0),
     */
    @Test
    fun testNameMatch() {
        val keys = listOf(T9.k9, T9.k9, T9.k4, T9.k5)
        val xiangce = PinyinSequence(
            listOf(
                Pinyin(listOf("xiang")),
                Pinyin(listOf("zha", "ce")),
            )
        )
        val xiazaiguanli = PinyinSequence(
            listOf(
                Pinyin(listOf("xia")),
                Pinyin(listOf("zai")),
                Pinyin(listOf("guan")),
                Pinyin(listOf("li")),
            )
        )

        val score1 = MatchScoreCalculator.calculateMatchResult(
            keys,
            xiangce
        )
        val score2 = MatchScoreCalculator.calculateMatchResult(
            keys,
            xiazaiguanli
        )

        assertTrue(score1 < score2)
    }
}
