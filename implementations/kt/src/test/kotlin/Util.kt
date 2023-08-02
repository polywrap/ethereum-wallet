import io.polywrap.configBuilder.ConfigBuilder
import io.polywrap.core.Invoker
import io.polywrap.plugins.ethereum.KnownNetwork

val emptyMockInvoker = Invoker(ffiInvoker = ConfigBuilder().build().ffiInvoker)

val getRpcUri: (KnownNetwork) -> String = { network ->
    "https://${network.toInfuraId()}.infura.io/v3/d119148113c047ca90f0311ed729c466"
}

private val domain = """
{
  "name": "Ether Mail",
  "version": "1",
  "chainId": 1,
  "verifyingContract": "0xCcCCccccCCCCcCCCCCCcCcCccCcCCCcCcccccccC"
}
""".trimIndent()

private val types = """
{
  "EIP712Domain": [
    {
      "type": "string",
      "name": "name"
    },
    {
      "type": "string",
      "name": "version"
    },
    {
      "type": "uint256",
      "name": "chainId"
    },
    {
      "type": "address",
      "name": "verifyingContract"
    }
  ],
  "Person": [
    {
      "name": "name",
      "type": "string"
    },
    {
      "name": "wallet",
      "type": "address"
    }
  ],
  "Mail": [
    {
      "name": "from",
      "type": "Person"
    },
    {
      "name": "to",
      "type": "Person"
    },
    {
      "name": "contents",
      "type": "string"
    }
  ]
}
""".trimIndent()

private val message = """
{
  "from": {
    "name": "Cow",
    "wallet": "0xCD2a3d9F938E13CD947Ec05AbC7FE734Df8DD826"
  },
  "to": {
    "name": "Bob",
    "wallet": "0xbBbBBBBbbBBBbbbBbbBbbbbBBbBbbbbBbBbbBBbB"
  },
  "contents": "Hello, Bob!"
}
""".trimIndent()

val typedDataJsonString = """
[
  "0x90F8bf6A479f320ead074411a4B0e7944Ea8c9C1",
  {
    "domain": $domain,
    "primaryType": "Mail",
    "types": $types,
    "message": $message
  }
]
""".trimIndent()
