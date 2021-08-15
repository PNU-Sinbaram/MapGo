package com.sinbaram.mapgo

import com.sinbaram.mapgo.API.ServerAPI
import org.junit.Assert.assertNotEquals
import org.junit.Test

/** Mapgo server api connection test */
class ServerAPITest {
    /** Test for server connection */
    @Test
    fun ServerConnectionTest() {
        assertNotEquals(ServerAPI.GetClient(), null)
    }
}
