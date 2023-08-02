package io.polywrap.plugins.ethereum

import org.kethereum.crypto.toECKeyPair
import org.kethereum.model.ECKeyPair
import org.kethereum.model.PrivateKey
import org.kethereum.rpc.BaseEthereumRPC
import org.kethereum.rpc.HttpEthereumRPC

/**
 * Represents a connection to an Ethereum network.
 *
 * @property provider The Ethereum Remote Procedure Call (RPC) provider.
 * @property signer The signer key pair used for signing transactions.
 */
data class Connection(var provider: BaseEthereumRPC, var signer: ECKeyPair? = null) {

    /**
     * Constructs a [Connection] instance from a [BaseEthereumRPC] provider and an [ECKeyPair] signer.
     *
     * @param provider The Ethereum RPC provider.
     * @param signer The private key for the signer.
     */
    constructor(provider: BaseEthereumRPC, signer: PrivateKey) : this(provider, signer.toECKeyPair())

    /**
     * Constructs a [Connection] instance from a [BaseEthereumRPC] provider and a ByteArray signer.
     *
     * @param provider The Ethereum RPC provider.
     * @param signer The private key for the signer as a ByteArray.
     */
    constructor(provider: BaseEthereumRPC, signer: ByteArray) : this(provider, PrivateKey(signer))

    /**
     * Constructs a [Connection] instance from a [BaseEthereumRPC] provider and a hexadecimal string signer.
     *
     * @param provider The Ethereum RPC provider.
     * @param signer The private key for the signer as a hexadecimal string.
     */
    constructor(provider: BaseEthereumRPC, signer: String) : this(provider, hexToBytes(signer))

    /**
     * Constructs a [Connection] instance from a [String] provider and an [ECKeyPair] signer.
     *
     * @param provider The Ethereum RPC provider as a JSON RPC node url.
     * @param signer The signer key pair.
     */
    constructor(provider: String, signer: ECKeyPair? = null) : this(HttpEthereumRPC(provider), signer)

    /**
     * Constructs a [Connection] instance from a [String] provider and a [PrivateKey] signer.
     *
     * @param provider The Ethereum RPC provider as a JSON RPC node url.
     * @param signer The private key for the signer.
     */
    constructor(provider: String, signer: PrivateKey) : this(HttpEthereumRPC(provider), signer)

    /**
     * Constructs a [Connection] instance from a [String] provider and a ByteArray signer.
     *
     * @param provider The Ethereum RPC provider as a JSON RPC node url.
     * @param signer The private key for the signer as a ByteArray.
     */
    constructor(provider: String, signer: ByteArray) : this(HttpEthereumRPC(provider), signer)

    /**
     * Constructs a [Connection] instance from a [String] provider and a hexadecimal string signer.
     *
     * @param provider The Ethereum RPC provider as a JSON RPC node url.
     * @param signer The private key for the signer as a hexadecimal string.
     */
    constructor(provider: String, signer: String) : this(HttpEthereumRPC(provider), signer)

    /**
     * Factory methods for constructing a [Connection] instance from different types.
     */
    companion object {
        /**
         * Creates a [Connection] from a known Ethereum network.
         *
         * @param network The known Ethereum network.
         * @return A connection to the specified Ethereum network.
         */
        fun from(network: KnownNetwork): Connection {
            val provider = "https://${network.toInfuraId()}.infura.io/v3/1ef7451bee5e458eb26738e521ad3074"
            return Connection(provider)
        }

        /**
         * Creates a [Connection] from a chain ID.
         *
         * @param chainId The chain ID for the Ethereum network.
         * @return A connection to the Ethereum network associated with the chain ID.
         * @throws Exception if the chainId is unknown.
         */
        fun from(chainId: Long): Connection {
            val network: KnownNetwork = KnownNetwork.from(chainId) ?: throw Exception("Unknown chainId: $chainId")
            return from(network)
        }

        /**
         * Creates a [Connection] from a node as a string.
         *
         * @param node A JSON RPC node url.
         * @return A connection to the Ethereum network associated with the node.
         */
        fun from(node: String): Connection {
            return Connection(node)
        }
    }

    /**
     * Updates the Ethereum RPC provider and signer key pair for this connection.
     *
     * @param provider The new Ethereum RPC provider.
     * @param signer The new signer key pair.
     */
    fun set(provider: BaseEthereumRPC, signer: ECKeyPair? = null) {
        this.provider = provider
        this.signer = signer
    }

    /**
     * Updates the Ethereum RPC provider and signer key pair for this connection.
     *
     * @param provider The new Ethereum RPC provider.
     * @param signer The new private key for the signer.
     */
    fun set(provider: BaseEthereumRPC, signer: PrivateKey) = set(provider, signer.toECKeyPair())

    /**
     * Updates the Ethereum RPC provider and signer key pair for this connection.
     *
     * @param provider The new Ethereum RPC provider.
     * @param signer The new private key for the signer as a ByteArray.
     */
    fun set(provider: BaseEthereumRPC, signer: ByteArray) = set(provider, PrivateKey(signer))

    /**
     * Updates the Ethereum RPC provider and signer key pair for this connection.
     *
     * @param provider The new Ethereum RPC provider.
     * @param signer The new private key for the signer as a hexadecimal string.
     */
    fun set(provider: BaseEthereumRPC, signer: String) = set(provider, hexToBytes(signer))

    /**
     * Updates the Ethereum RPC provider and signer key pair for this connection.
     *
     * @param provider The new Ethereum RPC provider as a JSON RPC node url.
     * @param signer The new signer key pair.
     */
    fun set(provider: String, signer: ECKeyPair? = null) {
        this.provider = HttpEthereumRPC(provider)
        this.signer = signer
    }

    /**
     * Updates the Ethereum RPC provider and signer key pair for this connection.
     *
     * @param provider The new Ethereum RPC provider as a JSON RPC node url.
     * @param signer The new private key for the signer.
     */
    fun set(provider: String, signer: PrivateKey) = set(provider, signer.toECKeyPair())

    /**
     * Updates the Ethereum RPC provider and signer key pair for this connection.
     *
     * @param provider The new Ethereum RPC provider as a JSON RPC node url.
     * @param signer The new private key for the signer as a ByteArray.
     */
    fun set(provider: String, signer: ByteArray) = set(provider, PrivateKey(signer))

    /**
     * Updates the Ethereum RPC provider and signer key pair for this connection.
     *
     * @param provider The new Ethereum RPC provider as a JSON RPC node url.
     * @param signer The new private key for the signer as a hexadecimal string.
     */
    fun set(provider: String, signer: String) = set(provider, hexToBytes(signer))
}

/**
 * Converts a hexadecimal string to a byte array.
 *
 * @param hex The hexadecimal string to be converted.
 * @return A byte array representing the hexadecimal string.
 */
private fun hexToBytes(hex: String): ByteArray {
    val hexBytes = if (hex.startsWith("0x")) hex.drop(2) else hex
    return hexBytes
        .chunked(2)
        .map { it.toInt(16).toByte() }
        .toByteArray()
}
