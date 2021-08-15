package com.sinbaram.mapgo

import com.sinbaram.mapgo.API.NaverAPI
import org.junit.Assert.assertNotEquals
import org.junit.Test

/** Naver api connection test */
class NaverAPITest {
    /** Test for server connection */
    @Test
    fun ServerConnectionTest() {
        assertNotEquals(NaverAPI.GetClient(), null)
    }
}
