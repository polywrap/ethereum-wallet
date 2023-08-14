package io.polywrap.plugins.ethereum

import getRpcUri
import org.kethereum.rpc.HttpEthereumRPC
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ConnectionsTest {
    private lateinit var connections: Connections
    private lateinit var testnet: Connection
    private lateinit var goerli: Connection

    @BeforeTest
    fun setup() {
        testnet = Connection.from(getRpcUri(KnownNetwork.Sepolia))
        goerli = Connection.from(getRpcUri(KnownNetwork.Goerli))
        connections = Connections(
            mutableMapOf(
                "testnet" to testnet,
                "goerli" to goerli
            ),
            "testnet"
        )
    }

    @Test
    fun testGet() {
        assertEquals(testnet, connections.get("testnet"))
        assertEquals(goerli, connections.get("goerli"))
        assertEquals(testnet, connections.get())
        assertNull(connections.get("rinkeby"))
    }

    @Test
    fun testSet() {
        assertNull(connections.get("sepolia"))
        val sepolia = Connection.from(getRpcUri(KnownNetwork.Sepolia))
        connections.set("sepolia", sepolia)
        assertEquals(sepolia, connections.get("sepolia"))

        val goerliUri = getRpcUri(KnownNetwork.Goerli)
        connections.set("goerli", goerliUri)
        val httpRPC = connections.get("goerli")?.provider as HttpEthereumRPC?
        val providerUri = httpRPC?.baseURL
        assertEquals(goerliUri, providerUri)

        val ropsten = Connection.from(getRpcUri(KnownNetwork.Goerli))
        connections.set("existingNetwork", ropsten)
        assertEquals(ropsten, connections.get("existingNetwork"))
        connections.set("existingNetwork", goerli)
        assertEquals(goerli, connections.get("existingNetwork"))
    }

    @Test
    fun testGetDefaultNetwork() {
        assertEquals("testnet", connections.getDefaultNetwork())
    }

    @Test
    fun testSetDefaultNetwork() {
        connections.setDefaultNetwork("goerli")
        assertEquals("goerli", connections.getDefaultNetwork())

        connections.setDefaultNetwork("newDefault", goerli)
        assertEquals("newDefault", connections.getDefaultNetwork())
        assertEquals(goerli, connections.get("newDefault"))
    }
}
