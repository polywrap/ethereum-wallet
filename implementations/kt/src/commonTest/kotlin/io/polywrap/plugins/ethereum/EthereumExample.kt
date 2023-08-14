package io.polywrap.plugins.ethereum

import io.polywrap.configBuilder.polywrapClient
import io.polywrap.core.InvokeResult
import io.polywrap.core.resolution.Uri
import io.polywrap.plugins.ethereum.wrap.ArgsRequest
import kotlinx.coroutines.test.runTest
import kotlin.test.*
import typedDataJsonString
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EthereumExample {

    private val uri = Uri("wrap://ens/wraps.eth:ethereum-provider@2.0.0")
    private val client = polywrapClient {
        setPackage(
            uri.toString() to ethereumWalletPlugin(
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
    fun signTypedData() = runTest {
        val args = ArgsRequest("eth_signTypedData_v4", "[\"0x90F8bf6A479f320ead074411a4B0e7944Ea8c9C1\", $typedDataJsonString]")
        val result: InvokeResult<String> = client.invoke(
            uri = uri,
            method = "request",
            args = args
        )
        assertNull(result.exceptionOrNull())
        assertEquals(
            "0x12bdd486cb42c3b3c414bb04253acfe7d402559e7637562987af6bd78508f38623c1cc09880613762cc913d49fd7d3c091be974c0dee83fb233300b6b58727311c",
            result.getOrThrow()
        )
    }
}