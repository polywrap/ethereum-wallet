# @polywrap/ethereum-wallet-js
The Ethereum Wallet plugin implements the `ethereum-wallet-interface` @ [wrapscan.io/polywrap/ethereum-wallet@1.0](../../interface/) (see [../../interface/polywrap.graphql](../../interface/polywrap.graphql)). It handles Ethereum wallet transaction signatures and sends JSON RPC requests for the Ethereum wrapper.

## Usage
### 1. Configure Client
When creating your Polywrap JS client, add the ethereum wallet plugin:
```typescript
import { PolywrapClient } from "@polywrap/client-js";
import { ethereumWalletPlugin } from "@polywrap/ethereum-wallet-js";

const client = new PolywrapClient({
  // 1. Add the plugin package @ an arbitrary URI
  packages: [{
    uri: "plugin/ethereum-wallet",
    package: ethereumWalletPlugin({ })
  }],
  // 2. Register this plugin as an implementation of the interface
  interfaces: [{
    interface: "wrapscan.io/polywrap/ethereum-wallet@1.0",
    implementations: ["plugin/ethereum-wallet"]
  }],
});
```

### 2. Invoke The Ethereum Wrapper
Invocations to the Ethereum wrapper may trigger sub-invocations to the Ethereum Wallet plugin:
```typescript
await client.invoke({
  uri: "wrapscan.io/polywrap/ethers@1.0",
  method: "getSignerAddress",
});
```
