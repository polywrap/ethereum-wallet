package io.polywrap.plugins.ethereum

import io.polywrap.core.Invoker
import io.polywrap.plugin.PluginFactory
import io.polywrap.plugin.PluginPackage
import io.polywrap.plugins.ethereum.wrap.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import org.kethereum.crypto.*
import org.kethereum.eip712.MoshiAdapter
import org.kethereum.model.ECKeyPair
import pm.gnosis.eip712.EIP712JsonParser
import pm.gnosis.eip712.typedDataHash

/**
 * A plugin for Ethereum provider and signer management.
 *
 * @property config An optional configuration object for the plugin.
 */
class EthereumWalletPlugin(config: Connections) : Module<Connections>(config) {

    override suspend fun request(args: ArgsRequest, invoker: Invoker): Json {
        val connection = this.config.get(args.connection)
        val method = args.method
        val params = args.params ?: "[]"
        val signer = connection.signer

        if (signer != null) {
//            if (method == "eth_sendTransaction") {
//                val rlp = params.substring(1, params.length - 1).encodeToByteArray()
//                val tx = rlp.toRLP().toTransaction()
//                val signature = tx.signViaEIP1559(signer)
//                val signedTx = tx.encode(signature).toHexString()
//                return connection.provider.sendRawTransaction(signedTx) ?: throw Exception("Failed to send transaction")
//            }

            if (method == "eth_signTypedData_v4") {
                val payload = params.trim().split(",", limit = 2)[1].substringBeforeLast("]").trim()
                val parsed = EIP712JsonParser(MoshiAdapter()).parseMessage(payload)
                val hash = typedDataHash(parsed.message, parsed.domain)
                return "0x" + signMessageHash(hash, signer).toHex()
            }

            if (method == "eth_sign") {
                val message = params.substring(1, params.length - 1).encodeToByteArray()
                return locallySignMessage(message, signer)
            }
        }

//        val response = connection.provider.stringCall(method, params)
//        if (response?.error != null) {
//            throw Exception("RPC Error. Code: ${response.error.code} Message: ${response.error.message}")
//        }
//        return response?.result ?: "{}"
        return ""
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
        val signer = connection.signer
        if (signer == null) {
            val accounts = request(ArgsRequest(method = "eth_accounts", connection = args.connection), invoker)
            if (accounts.length < 2) {
                return null
            }
            return accounts.substring(2, 44)
        }
        return signer.toAddress().hex
    }

    override suspend fun signMessage(args: ArgsSignMessage, invoker: Invoker): String {
        val connection = this.config.get(args.connection)
        val signer = connection.signer
        return if (signer == null) {
            request(ArgsRequest(method = "eth_sign", connection = args.connection), invoker)
        } else {
            locallySignMessage(args.message, signer)
        }
    }

    override suspend fun signTransaction(args: ArgsSignTransaction, invoker: Invoker): String {
        val connection = this.config.get(args.connection)
        val signer = connection.signer ?: throw Exception("No signer configured for connection: ${args.connection}")
        return "0x" + signer.signMessage(args.rlp).toHex()
    }

    private fun locallySignMessage(message: ByteArray, signer: ECKeyPair): String {
        val len = message.size.toString().encodeToByteArray()
        return "0x" + signer.signMessage(MESSAGE_PREFIX + len + message).toHex()
    }

    companion object {
        val MESSAGE_PREFIX = "\u0019Ethereum Signed Message:\n".encodeToByteArray()
    }
}

val ethereumWalletPlugin: PluginFactory<Connections> = { config: Connections ->
    PluginPackage(
        pluginModule = EthereumWalletPlugin(config),
        manifest = manifest
    )
}
