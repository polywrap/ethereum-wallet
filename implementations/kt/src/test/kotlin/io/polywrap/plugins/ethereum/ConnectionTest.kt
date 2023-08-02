package io.polywrap.plugins.ethereum

import getRpcUri
import kotlinx.coroutines.test.runTest
import org.kethereum.crypto.toAddress
import org.kethereum.rpc.HttpEthereumRPC
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ConnectionTest {

    val ethProvider = HttpEthereumRPC("http://localhost:8545")
    val signerAddress = "0x90F8bf6A479f320ead074411a4B0e7944Ea8c9C1"
    val testnet = Connection(
        provider = ethProvider,
        signer = "0x4f3edf983ac636a65a842ce7c78d9aa706d3b113bce9c46f30d7d21715b23b1d"
    )

    @Test
    fun constructsFromNetwork() {
        val connection = Connection.from(KnownNetwork.Mainnet)
        assertNotNull(connection)
        assertNotNull(connection.provider)
    }

    @Test
    fun constructsFromNode() {
        val connection = Connection.from(getRpcUri(KnownNetwork.Mainnet))
        assertNotNull(connection)
        assertNotNull(connection.provider)
    }

    @Test
    fun getProvider() {
        assertNotNull(testnet.provider)
    }

    @Test
    fun setProvider() = runTest {
        val goerliUri = getRpcUri(KnownNetwork.Goerli)
        val connection = Connection(goerliUri)
        assertEquals(
            KnownNetwork.Goerli.chainId,
            connection.provider.chainId()?.value?.toLong()
        )
        connection.set(ethProvider)
        assertEquals(
            ethProvider.chainId()?.value?.toLong(),
            connection.provider.chainId()?.value?.toLong()
        )
    }

    @Test
    fun getSigner() = runTest {
        val signer = testnet.signer
        assertNotNull(signer)
        assertEquals(signerAddress, signer.toAddress().hex)
    }

    @Test
    fun setSigner() = runTest {
        val connection = Connection(ethProvider)
        connection.set(connection.provider, "0x4f3edf983ac636a65a842ce7c78d9aa706d3b113bce9c46f30d7d21715b23b1d")
        val signer = connection.signer
        assertNotNull(signer)
        assertEquals(signerAddress, signer.toAddress().hex)
    }
}
