/// NOTE: This is an auto-generated file.
///       All modifications will be overwritten.

package io.polywrap.plugins.ethereum.wrap

import io.polywrap.core.Invoker
import io.polywrap.core.msgpack.msgPackDecode
import io.polywrap.core.msgpack.msgPackEncode
import io.polywrap.plugin.PluginMethod
import io.polywrap.plugin.PluginModule
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer

@Serializable
data class ArgsRequest(
    val method: String,
    val params: Json? = null,
    val connection: Connection? = null,
)

@Serializable
data class ArgsWaitForTransaction(
    val txHash: String,
    val confirmations: UInt,
    val timeout: UInt? = null,
    val connection: Connection? = null,
)

@Serializable
data class ArgsSignerAddress(
    val connection: Connection? = null,
)

@Serializable
data class ArgsSignMessage(
    val message: ByteArray,
    val connection: Connection? = null,
)

@Serializable
data class ArgsSignTransaction(
    val rlp: ByteArray,
    val connection: Connection? = null,
)

@Suppress("UNUSED_PARAMETER", "FunctionName")
abstract class Module<TConfig>(config: TConfig) : PluginModule<TConfig>(config) {

  final override val methods: Map<String, PluginMethod> = mapOf(
      "request" to ::__request,
      "waitForTransaction" to ::__waitForTransaction,
      "signerAddress" to ::__signerAddress,
      "signMessage" to ::__signMessage,
      "signTransaction" to ::__signTransaction,
  )

  abstract suspend fun request(
      args: ArgsRequest,
      invoker: Invoker
  ): Json

  abstract suspend fun waitForTransaction(
      args: ArgsWaitForTransaction,
      invoker: Invoker
  ): Boolean

  abstract suspend fun signerAddress(
      args: ArgsSignerAddress,
      invoker: Invoker
  ): String?

  abstract suspend fun signMessage(
      args: ArgsSignMessage,
      invoker: Invoker
  ): String

  abstract suspend fun signTransaction(
      args: ArgsSignTransaction,
      invoker: Invoker
  ): String

  private suspend fun __request(
      encodedArgs: ByteArray?,
      encodedEnv: ByteArray?,
      invoker: Invoker
    ): ByteArray {
        val args: ArgsRequest = encodedArgs?.let {
            msgPackDecode(ArgsRequest.serializer(), it).getOrNull()
                ?: throw Exception("Failed to decode args in invocation to plugin method 'request'")
        } ?: throw Exception("Missing args in invocation to plugin method 'request'")
        val response = request(args, invoker)
        return msgPackEncode(serializer(), response)
  }

  private suspend fun __waitForTransaction(
      encodedArgs: ByteArray?,
      encodedEnv: ByteArray?,
      invoker: Invoker
    ): ByteArray {
        val args: ArgsWaitForTransaction = encodedArgs?.let {
            msgPackDecode(ArgsWaitForTransaction.serializer(), it).getOrNull()
                ?: throw Exception("Failed to decode args in invocation to plugin method 'waitForTransaction'")
        } ?: throw Exception("Missing args in invocation to plugin method 'waitForTransaction'")
        val response = waitForTransaction(args, invoker)
        return msgPackEncode(serializer(), response)
  }

  private suspend fun __signerAddress(
      encodedArgs: ByteArray?,
      encodedEnv: ByteArray?,
      invoker: Invoker
    ): ByteArray {
        val args: ArgsSignerAddress = encodedArgs?.let {
            msgPackDecode(ArgsSignerAddress.serializer(), it).getOrNull()
                ?: throw Exception("Failed to decode args in invocation to plugin method 'signerAddress'")
        } ?: throw Exception("Missing args in invocation to plugin method 'signerAddress'")
        val response = signerAddress(args, invoker)
        return msgPackEncode(serializer(), response)
  }

  private suspend fun __signMessage(
      encodedArgs: ByteArray?,
      encodedEnv: ByteArray?,
      invoker: Invoker
    ): ByteArray {
        val args: ArgsSignMessage = encodedArgs?.let {
            msgPackDecode(ArgsSignMessage.serializer(), it).getOrNull()
                ?: throw Exception("Failed to decode args in invocation to plugin method 'signMessage'")
        } ?: throw Exception("Missing args in invocation to plugin method 'signMessage'")
        val response = signMessage(args, invoker)
        return msgPackEncode(serializer(), response)
  }

  private suspend fun __signTransaction(
      encodedArgs: ByteArray?,
      encodedEnv: ByteArray?,
      invoker: Invoker
    ): ByteArray {
        val args: ArgsSignTransaction = encodedArgs?.let {
            msgPackDecode(ArgsSignTransaction.serializer(), it).getOrNull()
                ?: throw Exception("Failed to decode args in invocation to plugin method 'signTransaction'")
        } ?: throw Exception("Missing args in invocation to plugin method 'signTransaction'")
        val response = signTransaction(args, invoker)
        return msgPackEncode(serializer(), response)
  }
}
