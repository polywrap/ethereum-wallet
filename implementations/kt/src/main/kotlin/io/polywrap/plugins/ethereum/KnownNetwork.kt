package io.polywrap.plugins.ethereum

enum class KnownNetwork(val chainId: Long) {
    Mainnet(1L),
    Goerli(5L),
    BinanceSmartChain(56L),
    Sepolia(11155111L),
    CeloMainnet(42220L),
    CeloAlfajores(44787L),
    AvalancheMainnet(43114L),
    AvalancheFuji(43113L),
    PalmMainnet(11297108109L),
    PalmTestnet(11297108099L),
    AuroraMainnet(1313161554L),
    AuroraTestnet(1313161555L);

    fun toInfuraId(): String = when (this) {
        Mainnet -> "mainnet"
        Goerli -> "goerli"
        BinanceSmartChain -> "binance"
        Sepolia -> "sepolia"
        CeloMainnet -> "celo-mainnet"
        CeloAlfajores -> "celo-alfajores"
        AvalancheMainnet -> "avalanche-mainnet"
        AvalancheFuji -> "avalanche-fuji"
        PalmMainnet -> "palm-mainnet"
        PalmTestnet -> "palm-testnet"
        AuroraMainnet -> "aurora-mainnet"
        AuroraTestnet -> "aurora-testnet"
    }

    companion object {
        fun from(chainId: Long): KnownNetwork? = KnownNetwork.entries.find { it.chainId == chainId }
        fun from(name: String): KnownNetwork? = KnownNetwork.entries.find { it.name == name }
    }
}
