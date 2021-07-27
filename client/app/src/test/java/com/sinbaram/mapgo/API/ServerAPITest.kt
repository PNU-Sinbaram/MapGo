package com.sinbaram.mapgo

import com.sinbaram.mapgo.API.ServerAPI
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ServerAPITest {
    @Test
    fun ServerConnectionTest() {
        assertNotEquals(ServerAPI.GetClient(), null)
    }
}