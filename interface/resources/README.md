# ethereum-wallet Wrap Interface

| Version | URI | WRAP Version |
|-|-|-|
| 2.0.0 | [`wrap://ens/wraps.eth:ethereum-provider@2.0.0`](https://wrappers.io/v/ens/wraps.eth:ethereum-provider@2.0.0) | 0.1 |

## Interface
```graphql
type Connection {
  node: String
  networkNameOrChainId: String
}

type Env {
  connection: Connection
}

type Module {
  """
  Send a remote RPC request to the registered provider
  """
  request(method: String!, params: JSON, connection: Connection): JSON!

  """
  Wait for a transaction to be confirmed
  """
  waitForTransaction(
    txHash: String!
    confirmations: UInt32!
    timeout: UInt32
    connection: Connection
  ): Boolean!

  """
  Get the ethereum address of the signer. Return null if signer is missing.
  """
  signerAddress(connection: Connection): String

  """
  Sign a message and return the signature. Throws if signer is missing.
  """
  signMessage(message: Bytes!, connection: Connection): String!

  """
  Sign a serialized unsigned transaction and return the signature. Throws if signer is missing.
  This method requires a wallet-based signer with a private key, and is not needed for most use cases.
  Typically, transactions are sent by `request` and signed by the wallet.
  """
  signTransaction(rlp: Bytes!, connection: Connection): String!
}
```

## Usage
```graphql
#import * from "ens/wraps.eth:ethereum-provider@2.0.0"
```

And implement the interface methods within your programming language of choice.

## Source Code
[Link](https://github.com/polywrap/ethereum-wallet)

## Known Implementations
[Link](https://github.com/polywrap/ethereum-wallet/tree/main/implementations)
