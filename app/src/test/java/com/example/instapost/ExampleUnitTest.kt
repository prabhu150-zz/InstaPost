package com.example.instapost

import com.example.instapost.Fragments.CreatePost
import org.junit.Assert.assertEquals
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

        val obj = CreatePost()

        var list = obj.getAllHashTags("#USA#kingpin#rofl #lol #lmao#god_king #!>#@>1.")

        for (hash in list)
            println(hash)

        assertEquals(list.size, 6)
    }
}
