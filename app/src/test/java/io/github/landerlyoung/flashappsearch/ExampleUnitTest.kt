package io.github.landerlyoung.flashappsearch

import io.github.landerlyoung.flashappsearch.search.model.T9
import io.github.landerlyoung.flashappsearch.search.repo.AppNameRepo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }


    @Test
    fun testNameMatch() {
        val s0 = AppNameRepo.calculateMatchResult(listOf(T9.k9, T9.k4, T9.k2, T9.k6), "xiangji")
        val s1 = AppNameRepo.calculateMatchResult(listOf(T9.k9, T9.k4, T9.k2, T9.k6), "xiachufang")
        assertTrue(s0 > s1)
    }
}
