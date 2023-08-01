package io.polywrap.plugins.ethereum

import org.kethereum.crypto.toECKeyPair
import org.kethereum.model.ECKeyPair
import org.kethereum.model.PrivateKey
import org.kethereum.rpc.BaseEthereumRPC
import org.kethereum.rpc.HttpEthereumRPC

class Connection(var provider: BaseEthereumRPC, var signer: ECKeyPair? = null) {

    constructor(provider: BaseEthereumRPC, signer: PrivateKey? = null) : this(provider, signer?.toECKeyPair())
    constructor(provider: BaseEthereumRPC, signer: ByteArray) : this(provider, PrivateKey(signer))
    constructor(provider: BaseEthereumRPC, signer: String) : this(provider, hexToBytes(signer))
    constructor(provider: String, signer: PrivateKey? = null) : this(HttpEthereumRPC(provider), signer)
    constructor(provider: String, signer: ByteArray) : this(HttpEthereumRPC(provider), signer)
    constructor(provider: String, signer: String) : this(HttpEthereumRPC(provider), signer)

    companion object {
        fun from(network: KnownNetwork): Connection {
            val provider = "https://${network.toInfuraId()}.infura.io/v3/1ef7451bee5e458eb26738e521ad3074"
            return Connection(provider)
        }

        fun from(chainId: Long): Connection {
            val network: KnownNetwork = KnownNetwork.from(chainId) ?: throw Exception("Unknown chainId: $chainId")
            return from(network)
        }

        fun from(node: String): Connection {
            return Connection(node)
        }
    }

    fun set(provider: String, signer: String? = null) {
        this.provider = HttpEthereumRPC(provider)
        this.signer = hexToKeyPair(signer)
    }

    fun set(provider: BaseEthereumRPC, signer: String? = null) {
        this.provider = provider
        this.signer = hexToKeyPair(signer)
    }

    private fun hexToKeyPair(hex: String?): ECKeyPair? = hex?.let { PrivateKey(hexToBytes(it)) }?.toECKeyPair()
}

private fun hexToBytes(hex: String): ByteArray {
    val hexBytes = if (hex.startsWith("0x")) hex.drop(2) else hex
    return hexBytes
        .chunked(2)
        .map { it.toInt(16).toByte() }
        .toByteArray()
}
