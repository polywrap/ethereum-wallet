package io.polywrap.plugins.ethereum

import io.polywrap.plugins.ethereum.wrap.Connection as SchemaConnection

class Connections(
    private var connections: MutableMap<String, Connection>,
    private var defaultNetwork: String = "mainnet"
) {

    init {
        connections.forEach { (network, connection) ->
            connections.remove(network)
            set(network, connection)
        }

        if (connections["mainnet"] == null) {
            connections["mainnet"] = Connection.from(KnownNetwork.Mainnet)
        }
        if (connections[defaultNetwork] == null) {
            throw Exception("No connection found for default network: $defaultNetwork")
        }
    }

    fun set(network: String, connection: Connection) {
        connections[network.lowercase()] = connection
    }

    fun set(network: String, connection: KnownNetwork) = set(network, Connection.from(connection))

    fun set(network: String, connection: Long) = set(network, Connection.from(connection))

    fun set(network: String, connection: String) = set(network, Connection.from(connection))

    fun get(network: String? = null): Connection? {
        val formattedNetwork = network?.lowercase() ?: defaultNetwork.lowercase()
        return connections[formattedNetwork]
    }

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

    fun setDefaultNetwork(network: String, connection: Connection) {
        set(network, connection)
        defaultNetwork = network
    }

    fun setDefaultNetwork(network: String) {
        connections[network]?.let {
            defaultNetwork = network
        } ?: throw Exception("No connection found for network: $network")
    }

    fun setDefaultNetwork(network: String, connection: KnownNetwork) {
        setDefaultNetwork(network, Connection.from(connection))
    }
    fun setDefaultNetwork(network: String, connection: Long) = setDefaultNetwork(network, Connection.from(connection))

    fun setDefaultNetwork(network: String, connection: String) = setDefaultNetwork(network, Connection.from(connection))

    fun getDefaultNetwork(): String = defaultNetwork
}
