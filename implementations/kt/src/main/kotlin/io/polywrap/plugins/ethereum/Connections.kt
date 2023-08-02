package io.polywrap.plugins.ethereum

import io.polywrap.plugins.ethereum.wrap.Connection as SchemaConnection

/**
 * A class representing multiple connections to different Ethereum networks.
 *
 * @property networks A mutable map of connections, indexed by network names.
 * @property defaultNetwork The name of the default network to use when a specific network isn't specified.
 * @constructor Instantiates the Connections class with the specified map of connections and optional default network.
 * If no default network is specified, the default network will be set to "mainnet".
 */
class Connections(
    private var networks: MutableMap<String, Connection>,
    private var defaultNetwork: String = "mainnet"
) {

    init {
        networks.forEach { (network, connection) ->
            networks.remove(network)
            set(network, connection)
        }

        if (networks["mainnet"] == null) {
            networks["mainnet"] = Connection.from(KnownNetwork.Mainnet)
        }
        if (networks[defaultNetwork] == null) {
            throw Exception("No connection found for default network: $defaultNetwork")
        }
    }

    /**
     * Sets or replaces a connection for a given network.
     *
     * @param network The name of the network.
     * @param connection The connection to the network.
     */
    fun set(network: String, connection: Connection) {
        networks[network.lowercase()] = connection
    }

    /**
     * Sets or replaces a connection for a given network using a known network.
     *
     * @param network The name of the network.
     * @param knownNetwork The known network to establish a connection to.
     */
    fun set(network: String, knownNetwork: KnownNetwork) = set(network, Connection.from(knownNetwork))

    /**
     * Sets or replaces a connection for a given network using a chain ID.
     *
     * @param network The name of the network.
     * @param chainId The chain ID of the network to establish a connection to.
     */
    fun set(network: String, chainId: Long) = set(network, Connection.from(chainId))

    /**
     * Sets or replaces a connection for a given network using a node URL.
     *
     * @param network The name of the network.
     * @param node The node URL of the network to establish a connection to.
     */
    fun set(network: String, node: String) = set(network, Connection.from(node))

    /**
     * Retrieves the connection for a specified network, if it exists.
     *
     * @param network The name of the network.
     * @return The connection to the network, or null if it doesn't exist.
     */
    fun get(network: String): Connection? = networks[network.lowercase()]

    /**
     * Retrieves the connection for a specified SchemaConnection object, or the default connection if none is specified.
     *
     * @param connection The SchemaConnection object representing the network to connect to, or null for the default network.
     * @return The requested [Connection].
     * @throws Exception if connection is null and no [Connection] can be found for the default network.
     */
    fun get(connection: SchemaConnection? = null): Connection {
        if (connection == null) {
            return get(defaultNetwork) ?: throw Exception("No connection found for default network: $defaultNetwork")
        }

        val (node, networkNameOrChainId) = connection

        var result: Connection? = null

        if (node != null) {
            result = Connection.from(node)
        } else if (networkNameOrChainId != null) {
            val networkStr = networkNameOrChainId.lowercase()
            result = get(networkStr)
                ?: networkStr.toLongOrNull()?.let { Connection.from(it) }
                ?: KnownNetwork.from(networkStr)?.let { Connection.from(it) }
        }

        return result
            ?: get(defaultNetwork)
            ?: throw Exception("No connection found for default network: $defaultNetwork")
    }

    /**
     * Sets or replaces a connection for a given network and sets that network as the default.
     *
     * @param network The name of the network.
     * @param connection The connection to the network.
     */
    fun setDefaultNetwork(network: String, connection: Connection) {
        set(network, connection)
        defaultNetwork = network
    }

    /**
     * Sets a network as the default, if a connection for that network exists.
     *
     * @param network The name of the network.
     * @throws Exception if no connection can be found for the specified network.
     */
    fun setDefaultNetwork(network: String) {
        networks[network]?.let {
            defaultNetwork = network
        } ?: throw Exception("No connection found for network: $network")
    }

    /**
     * Sets or replaces a connection for a given network and sets that network as the default using a known network.
     *
     * @param network The name of the network.
     * @param knownNetwork The known network to establish a connection to.
     */
    fun setDefaultNetwork(network: String, knownNetwork: KnownNetwork) {
        setDefaultNetwork(network, Connection.from(knownNetwork))
    }

    /**
     * Sets or replaces a connection for a given network and sets that network as the default using a chain ID.
     *
     * @param network The name of the network.
     * @param chainId The chain ID of the network to establish a connection to.
     */
    fun setDefaultNetwork(network: String, chainId: Long) = setDefaultNetwork(network, Connection.from(chainId))

    /**
     * Sets or replaces a connection for a given network and sets that network as the default using a node URL.
     *
     * @param network The name of the network.
     * @param node The node URL of the network to establish a connection to.
     */
    fun setDefaultNetwork(network: String, node: String) = setDefaultNetwork(network, Connection.from(node))

    /**
     * Retrieves the name of the default network.
     *
     * @return The name of the default network.
     */
    fun getDefaultNetwork(): String = defaultNetwork
}
