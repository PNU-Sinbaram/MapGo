package com.sinbaram.mapgo

import com.sinbaram.mapgo.API.NaverAPI
import org.junit.Assert.assertNotEquals
import org.junit.Test

class NaverAPITest {
    @Test
    fun ServerConnectionTest() {
        assertNotEquals(NaverAPI.GetClient(), null)
    }
}