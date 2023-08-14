package io.polywrap.plugins.ethereum

import io.polywrap.core.Invoker
import io.polywrap.plugin.PluginFactory
import io.polywrap.plugin.PluginPackage
import io.polywrap.plugins.ethereum.wrap.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import org.kethereum.crypto.*
import org.kethereum.eip712.MoshiAdapter
import org.kethereum.model.Address
import pm.gnosis.eip712.EIP712JsonParser
import pm.gnosis.eip712.typedDataHash

/**
 * A plugin for Ethereum provider and signer management.
 *
 * @property config An optional configuration object for the plugin.
 */
class EthereumWalletPlugin(config: Connections) : Module<Connections>(config) {

    companion object {
        val MESSAGE_PREFIX = "\u0019Ethereum Signed Message:\n".encodeToByteArray()
    }

    override suspend fun request(args: ArgsRequest, invoker: Invoker): Json {
        val connection = this.config.get(args.connection)
        val method = args.method
        val params = args.params?.trim()?.let {
            if (it.startsWith("[") && it.endsWith("]")) {
                it.substring(1, it.length - 1)
            } else {
                it
            }
        }
        val provider = connection.provider
        val signer = connection.signer

        if (signer != null) {
            // TODO: KEthereum does not provide a simple way to convert an RLP byte array to a transaction. Need to research this.
//            if (method == "eth_sendTransaction") {
//                val rlp = params.substring(1, params.length - 1).encodeToByteArray()
//                val tx = rlp.toRLP().toTransaction()
//                val signature = tx.signViaEIP1559(signer)
//                val signedTx = tx.encode(signature).toHexString()
//                return connection.provider.sendRawTransaction(signedTx) ?: throw Exception("Failed to send transaction")
//            }

            if (method == "eth_signTypedData_v4") {
                val payload = params!!.split(",", limit = 2)[1].trim()
                val parsed = EIP712JsonParser(MoshiAdapter()).parseMessage(payload)
                val hash = typedDataHash(parsed.message, parsed.domain)
                return "0x" + signMessageHash(hash, signer).toHex()
            }
        }

        when (method) {
            "eth_chainId" -> {
                return provider.chainId()?.value?.toString(16)?.let { "0x$it" }
                    ?: throw Exception("Failed to get chain ID")
            }
            "eth_sign" -> {
                val message = params!!.encodeToByteArray()
                return signMessage(ArgsSignMessage(message, args.connection), invoker)
            }
            "eth_getTransactionCount" -> {
                val (address, block) = params!!.replace("\"", "").split(",").map { it.trim() }
                return provider.getTransactionCount(Address(address), block)?.toString()
                    ?: throw Exception("Failed to get transaction count")
            }
            else -> {
                val response = params?.let { provider.stringCall(method, it) } ?: provider.stringCall(method)

                if (response?.error != null) {
                    throw Exception("RPC Error. Code: ${response.error?.code} Message: ${response.error?.message}")
                }
                return response?.result ?: throw Exception("Failed to get response")
            }
        }
    }

    override suspend fun signerAddress(args: ArgsSignerAddress, invoker: Invoker): String? {
        val connection = this.config.get(args.connection)
        return connection.signer?.toAddress()?.hex
            ?: connection.provider.accounts()?.get(0)?.hex
    }

    override suspend fun signMessage(args: ArgsSignMessage, invoker: Invoker): String {
        val connection = this.config.get(args.connection)
        val signer = connection.signer
        return if (signer == null) {
            val address = signerAddress(ArgsSignerAddress(args.connection), invoker)
                ?: throw Exception("No signer configured for connection: $connection")
            val signature = connection.provider.sign(Address(address), args.message)?.toHex()
                ?: throw Exception("Failed to sign message")
            "0x$signature"
        } else {
            val len = args.message.size.toString().encodeToByteArray()
            "0x" + signer.signMessage(MESSAGE_PREFIX + len + args.message).toHex()
        }
    }

    override suspend fun signTransaction(args: ArgsSignTransaction, invoker: Invoker): String {
        val connection = this.config.get(args.connection)
        val signer = connection.signer ?: throw Exception("No wallet configured for connection: $connection")
        return "0x" + signer.signMessage(args.rlp).toHex()
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
}

val ethereumWalletPlugin: PluginFactory<Connections> = { config: Connections ->
    PluginPackage(
        pluginModule = EthereumWalletPlugin(config),
        manifest = manifest
    )
}
