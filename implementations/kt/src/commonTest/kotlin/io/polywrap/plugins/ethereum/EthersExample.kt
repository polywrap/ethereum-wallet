package io.polywrap.plugins.ethereum

import io.polywrap.configBuilder.polywrapClient
import io.polywrap.core.InvokeResult
import io.polywrap.core.resolution.Uri
import io.polywrap.plugins.ethereum.wrap.Connection
import io.polywrap.plugins.ethereum.wrap.Json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import typedDataJsonString
import kotlin.test.*
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration

class EthersExample {

    private val ethersWrapUri = Uri("wrapscan.io/polywrap/ethers@1.0.0")
    private val pluginUri = Uri("plugin/ethereum-wallet@1.0")

    private val client = polywrapClient {
        addDefaults()
        setPackage(
            pluginUri.toString() to ethereumWalletPlugin(
                Connections(
                    networks = mutableMapOf(
                        "testnet" to Connection(
                            provider = "http://localhost:8545"
                        )
                    ),
                    defaultNetwork = "testnet"
                )
            )
        )
    }

    @Test
    fun signTypedData() = runTest(timeout = Duration.parse("30s")) {
        @Serializable
        class ArgsSignTypedData(
            val payload: Json,
            val connection: Connection? = null
        )

        val result: InvokeResult<String> = client.invoke(
            uri = ethersWrapUri,
            method = "signTypedData",
            args = ArgsSignTypedData(typedDataJsonString)
        )
        assertNull(result.exceptionOrNull())
        assertEquals(
            "0x12bdd486cb42c3b3c414bb04253acfe7d402559e7637562987af6bd78508f38623c1cc09880613762cc913d49fd7d3c091be974c0dee83fb233300b6b58727311c",
            result.getOrThrow()
        )
    }
}
