# @polywrap/ethereum-wallet-js
The Ethereum Wallet plugin implements the `ethereum-wallet-interface` @ [wrapscan.io/polywrap/ethereum-wallet@1.0](https://github.com/polywrap/ethereum-wallet/tree/main/interface) (see [../../interface/polywrap.graphql](https://github.com/polywrap/ethereum-wallet/blob/main/interface/polywrap.graphql)). It handles Ethereum wallet transaction signatures and sends JSON RPC requests for the Ethereum wrapper. 

The JavaScript implementation of the Ethereum Wallet Plugin is based on the popular ethers.js library. You can learn more about ethers.js at https://docs.ethers.org/v5/.

## Usage
### 1. Configure the Ethereum Wallet Plugin

```typescript
import { PolywrapClient } from "@polywrap/client-js";
import { ethereumWalletPlugin, WalletConfig, Connection, Connections, EthereumProvider } from "@polywrap/ethereum-wallet-js";

// MetaMask requires requesting permission to connect users accounts
await window.ethereum!!.request({ method: "eth_requestAccounts" });

const walletConfig: WalletConfig = {
  connections: new Connections({
    networks: {
      // The Connnection will obtain a wallet-based signer from the MetaMask provider
      mainnet: new Connection({
        provider: window.ethereum as EthereumProvider,
      }),
      // Alternatively, you can use a provider from a service like Infura
      goerli: new Connection({
        provider: "https://goerli.infura.io/v3/b00b2c2cc09c487685e9fb061256d6a6",
      }),
      // You can also provide your own signer
      testnet: new Connection({
        provider: "https://localhost:8545",
        signer: new Wallet(
          "0x4f3edf983ac636a65a842ce7c78d9aa706d3b113bce9c46f30d7d21715b23b1d"
        ),
      }),
    },
    // The default network is used when no network is specified in the request
    defaultNetwork: "mainnet"
  }),
}
```

### 2. Configure Client
When creating your Polywrap JS client, add the ethereum wallet plugin:
```typescript
const client = new PolywrapClient({
  // 1. Add the plugin package @ an arbitrary URI
  packages: [{
    uri: "plugin/ethereum-wallet",
    package: ethereumWalletPlugin(walletConfig)
  }],
  // 2. Register this plugin as an implementation of the interface
  interfaces: [{
    interface: "wrapscan.io/polywrap/ethereum-wallet@1.0",
    implementations: ["plugin/ethereum-wallet"]
  }],
});
```

### 3. Invoke The Ethereum Wrapper
Invocations to the Ethereum wrapper may trigger sub-invocations to the Ethereum Wallet plugin:
```typescript
await client.invoke({
  uri: "wrapscan.io/polywrap/ethers@1.0",
  method: "getSignerAddress",
});
```

## Reference
### Connection
```ts
export type Address = string;
export type AccountIndex = number;
// Signer is from ethers.js
export type EthereumSigner = Signer | Address | AccountIndex;

// ExternalProvider and JsonRpcProvider are from ethers.js
export type EthereumProvider = string | ExternalProvider | JsonRpcProvider;
export type EthereumClient = Web3Provider | JsonRpcProvider;

// Users can configure the Connection with a provider and/or a signer
export interface ConnectionConfig {
  provider: EthereumProvider;
  signer?: EthereumSigner;
}

export declare class Connection {
  constructor(_config: ConnectionConfig);
  // these factory functions are used internally
  static fromNetwork(networkish: KnownNetwork): Connection;
  static fromNode(node: string): Connection;
  // methods
  setProvider(provider: EthereumProvider, signer?: EthereumSigner): void;
  getProvider(): EthereumClient;
  setSigner(signer: EthereumSigner): void;
  getSigner(): ethers.Signer;
  getSignerType(): SignerType;
}

// Returned by connection.getSignerType 
// A custom_signer requires a private key, while a provider_signer is obtained from a provider (e.g. MetaMask)
export declare enum SignerType {
  CUSTOM_SIGNER = 0,
  PROVIDER_SIGNER = 1
}

// Can be used in Connection.fromNetwork to obtain a signer-less Connection
export declare enum KnownNetworkId {
  mainnet = 1,
  goerli = 5,
  sepolia = 11155111,
  "celo-mainnet" = 42220,
  "celo-alfajores" = 44787,
  "avalanche-mainnet" = 43114,
  "avalanche-fuji" = 43113,
  "palm-mainnet" = 11297108109,
  "palm-testnet" = 11297108099,
  "aurora-mainnet" = 1313161554,
  "aurora-testnet" = 1313161555
}
```

### Connections
```ts
type Networks = {
  [network: string]: Connection;
};
// Connections is configured with a map of networks and a default network
// The default network is used when no network is specified in the request
export interface ConnectionsConfig {
  networks: Networks;
  defaultNetwork?: string;
}

export declare class Connections {
  constructor(config: ConnectionsConfig);
  // returns default network if key is undefined
  get(network?: string): Connection | undefined;
  set(network: string, connection: Connection | EthereumProvider): void;
  setDefaultNetwork(network: string, connection?: Connection | EthereumProvider): void;
  getDefaultNetwork(): string;
  // given a connection from the Ethereum Wallet **interface** graphql schema, return a Connection
  // if the connection is not found in the store, returns a new Connection
  // if the connection is undefined, returns the default network Connection
  getConnection(connection?: SchemaConnection | null): Promise<Connection>;
}
```

### EthereumWalletPlugin
```ts
export interface WalletConfig {
  connections: Connections;
}
// You do not need to interact with the EthereumWallet directly. Just add it to your PolywrapClient configuration.
export declare class EthereumWalletPlugin extends Module<WalletConfig> {
  constructor(config: WalletConfig);
  request(args: Args_request, _client: CoreClient, env?: Env | null): Promise<string>;
  waitForTransaction(args: Args_waitForTransaction, _client: CoreClient, env?: Env | null): Promise<boolean>;
  signerAddress(args: Args_signerAddress, _client: CoreClient, env?: Env | null): Promise<string | null>;
  signMessage(args: Args_signMessage, _client: CoreClient, env?: Env | null): Promise<string>;
  signTransaction(args: Args_signTransaction, _client: CoreClient, env?: Env | null): Promise<string>;
}
```