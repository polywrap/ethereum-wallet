package io.polywrap.plugins.ethereum

import io.polywrap.core.Invoker
import io.polywrap.plugin.PluginFactory
import io.polywrap.plugin.PluginPackage
import io.polywrap.plugins.ethereum.wrap.*

/**
 * A plugin for Ethereum provider and signer management.
 *
 * @property config An optional configuration object for the plugin.
 */
class EthereumWalletPlugin(config: Config? = null) : Module<EthereumWalletPlugin.Config?>(config) {

    class Config()

    override suspend fun request(args: ArgsRequest, invoker: Invoker): Json {
        TODO("Not yet implemented")
    }

    override suspend fun waitForTransaction(args: ArgsWaitForTransaction, invoker: Invoker): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun signerAddress(args: ArgsSignerAddress, invoker: Invoker): String? {
        TODO("Not yet implemented")
    }

    override suspend fun signMessage(args: ArgsSignMessage, invoker: Invoker): String {
        TODO("Not yet implemented")
    }

    override suspend fun signTransaction(args: ArgsSignTransaction, invoker: Invoker): String {
        TODO("Not yet implemented")
    }
}

val ethereumWalletPlugin: PluginFactory<EthereumWalletPlugin.Config?> = { config: EthereumWalletPlugin.Config? ->
    PluginPackage(
        pluginModule = EthereumWalletPlugin(config),
        manifest = manifest
    )
}
