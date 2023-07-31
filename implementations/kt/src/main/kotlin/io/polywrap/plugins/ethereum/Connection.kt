package io.polywrap.plugins.ethereum

import org.kethereum.rpc.EthereumRPC
import org.kethereum.rpc.HttpEthereumRPC

class Connection(var provider: EthereumRPC, var signer: String? = null) {

    constructor(provider: String, signer: String? = null) : this(HttpEthereumRPC(provider), signer)

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
        this.signer = signer
    }

    fun set(provider: EthereumRPC, signer: String? = null) {
        this.provider = provider
        this.signer = signer
    }
}
