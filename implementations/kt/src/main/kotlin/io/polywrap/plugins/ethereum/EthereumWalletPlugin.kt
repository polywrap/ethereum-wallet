package io.polywrap.plugins.ethereum

import io.polywrap.core.Invoker
import io.polywrap.plugin.PluginFactory
import io.polywrap.plugin.PluginPackage
import io.polywrap.plugins.ethereum.wrap.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import org.kethereum.crypto.*
import org.kethereum.eip1559.signer.signViaEIP1559
import org.kethereum.eip712.MoshiAdapter
import org.kethereum.extensions.transactions.encode
import org.kethereum.extensions.transactions.toTransaction
import org.kethereum.model.ECKeyPair
import org.kethereum.model.Transaction
import org.kethereum.rlp.RLPElement
import org.kethereum.rlp.RLPList
import org.kethereum.rlp.toRLP
import org.kethereum.rpc.EthereumRPC
import pm.gnosis.eip712.EIP712JsonParser
import pm.gnosis.eip712.typedDataHash

fun EthereumRPC.stringCallplaceholder(function: String, params: String): String? {
    return ""
}

/**
 * A plugin for Ethereum provider and signer management.
 *
 * @property config An optional configuration object for the plugin.
 */
@OptIn(ExperimentalStdlibApi::class)
class EthereumWalletPlugin(config: Connections) : Module<Connections>(config) {

    override suspend fun request(args: ArgsRequest, invoker: Invoker): Json {
        val connection = this.config.get(args.connection)
        val method = args.method
        val params = args.params ?: "[]"
        val signer = connection.signer

        if (method == "eth_sendTransaction" && signer != null) {
            val rlp = params.substring(1, params.length - 1).encodeToByteArray()
            val tx = rlp.toRLP().toTransaction()
            val signature = tx.signViaEIP1559(signer)
            val signedTx = tx.encode(signature).toHexString()
            return connection.provider.sendRawTransaction(signedTx) ?: throw Exception("Failed to send transaction")
        }

        if (method == "eth_signTypedData_v4" && signer != null) {
            return locallySignTypedData(params, connection.signer!!)
        }

        return connection.provider.stringCallplaceholder(method, params) ?: "{}"
    }

    override suspend fun waitForTransaction(args: ArgsWaitForTransaction, invoker: Invoker): Boolean {
        val connection = this.config.get(args.connection)
        val pollLatency = 100L // ms
        val confirmationLatency = 500L // ms
        val timeout = args.timeout?.toLong() ?: 300_000L // ms
        val confirmations = args.confirmations.toLong()

        return try {
            withTimeout(timeout) {
                var blockMined: Long? = connection.provider.getTransactionByHash(args.txHash)?.transaction?.blockNumber?.toLong()
                while (blockMined == null) {
                    delay(pollLatency)
                    blockMined = connection.provider.getTransactionByHash(args.txHash)?.transaction?.blockNumber?.toLong()
                }
                while (connection.provider.blockNumber()!!.toLong() - blockMined + 1 < confirmations) {
                    delay(confirmationLatency)
                }
                true
            }
        } catch (e: Exception) {
            throw Exception("Transaction timed out", e)
        }
    }

    override suspend fun signerAddress(args: ArgsSignerAddress, invoker: Invoker): String? {
        val connection = this.config.get(args.connection)
        return connection.signer?.toAddress()?.hex
    }

    override suspend fun signMessage(args: ArgsSignMessage, invoker: Invoker): String {
        val connection = this.config.get(args.connection)
        val signer = connection.signer
        if (signer == null) {
            throw Exception("No signer configured for connection: ${args.connection}")
        } else {
            return locallySignMessage(args.message, signer)
        }
    }

    override suspend fun signTransaction(args: ArgsSignTransaction, invoker: Invoker): String {
        val connection = this.config.get(args.connection)
        val signer = connection.signer
        if (signer == null) {
            throw Exception("No signer configured for connection: ${args.connection}")
        } else {
            return locallySignTransaction(args.rlp, signer)
        }
    }

    // returns signature as hex string
    private fun locallySignMessage(message: ByteArray, signer: ECKeyPair): String {
        return "0x" + signer.signMessage(message).toHex()
    }

    // returns signature as hex string
    private fun locallySignTransaction(rlp: ByteArray, signer: ECKeyPair): String {
        val tx = rlp.toRLP().toTransaction()
        return "0x" + tx.signViaEIP1559(signer).toHex()
    }

    // returns signature as hex string
    private fun locallySignTypedData(payload: Json, signer: ECKeyPair): String {
        val parser = EIP712JsonParser(MoshiAdapter())
        val parsed = parser.parseMessage(payload)
        val hash = typedDataHash(parsed.message, parsed.domain)
        return "0x" + signMessageHash(hash, signer).toHex()
    }

    private fun RLPElement.toTransaction(): Transaction {
        return RLPList(listOf(this)).toTransaction() ?: throw Exception("Failed to parse transaction")
    }
}

val ethereumWalletPlugin: PluginFactory<Connections> = { config: Connections ->
    PluginPackage(
        pluginModule = EthereumWalletPlugin(config),
        manifest = manifest
    )
}
