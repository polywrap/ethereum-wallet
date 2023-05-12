use std::{assert_matches::assert_matches, collections::HashMap, str::FromStr, sync::Arc};

use ethers::types::{Bytes, TransactionRequest};
use polywrap_client::client::PolywrapClient;
use polywrap_core::{
    client::ClientConfig,
    resolvers::static_resolver::{StaticResolver, StaticResolverLike},
    uri::Uri,
};
use polywrap_ethereum_wallet_plugin::{
    connection::Connection, connections::Connections, EthereumWalletPlugin,
};
use polywrap_msgpack::msgpack;
use polywrap_plugin::{
    package::PluginPackage,
    JSON::{json, to_value, Value},
};
use serde::{Deserialize, Serialize};

fn get_client() -> PolywrapClient {
    let connection = Connection::new(
        "https://bsc-dataseed1.binance.org/".to_string(),
        Some(String::from(
            "0x4f3edf983ac636a65a842ce7c78d9aa706d3b113bce9c46f30d7d21715b23b1d",
        )),
    )
    .unwrap();
    let connections = Connections::new(
        HashMap::from([("binance".to_string(), connection)]),
        Some("binance".to_string()),
    );

    let wallet_plugin = EthereumWalletPlugin::new(connections);
    let plugin_pkg: PluginPackage = wallet_plugin.into();
    let package = Arc::new(plugin_pkg);

    let resolver = StaticResolver::from(vec![StaticResolverLike::Package(
        Uri::try_from("plugin/ethereum-wallet").unwrap(),
        package,
    )]);

    PolywrapClient::new(ClientConfig {
        resolver: Arc::new(resolver),
        interfaces: None,
        envs: None,
    })
}

#[test]
fn get_chain_id() {
    let client = get_client();
    let response = client.invoke::<String>(
        &Uri::try_from("plugin/ethereum-wallet").unwrap(),
        "request",
        Some(&msgpack!({
            "method": "eth_chainId"
        })),
        None,
        None,
    );
    assert_eq!(response.unwrap(), "0x38");
}

#[test]
fn get_transaction_count() {
    let client = get_client();
    let response = client.invoke::<String>(
        &Uri::try_from("plugin/ethereum-wallet").unwrap(),
        "request",
        Some(&msgpack!({
            "method": "eth_getTransactionCount",
            "params": "[\"0xf3702506acec292cfaf748b37cfcea510dc37714\",\"latest\"]",
        })),
        None,
        None,
    );
    assert_ne!(response.unwrap(), "0x0");
}

#[test]
fn get_handle_eth_call() {
    let mut t = TransactionRequest::new();
    // retrieve() call function hash
    let d = Bytes::from_str("0x2e64cec1").unwrap();
    t = t.data(d);
    t = t.to("0x9a752098eea4b09271fb9a774d5a50064bbefb22");

    #[derive(Deserialize, Serialize)]
    #[serde(untagged)]
    enum Arguments {
        Tx(TransactionRequest),
        Tag(String),
    }

    let parameters = vec![Arguments::Tx(t), Arguments::Tag("latest".to_string())];

    let client = get_client();
    let response = client.invoke::<String>(
        &Uri::try_from("plugin/ethereum-wallet").unwrap(),
        "request",
        Some(&msgpack!({
            "method": "eth_call",
            "params": Value::to_string(&to_value(parameters).unwrap()),
        })),
        None,
        None,
    );
    assert_eq!(
        response.unwrap(),
        "0x0000000000000000000000000000000000000000000000000000000000000002"
    );
}

#[test]
fn get_block_by_number() {
    let client = get_client();
    let response = client.invoke::<String>(
        &Uri::try_from("plugin/ethereum-wallet").unwrap(),
        "request",
        Some(&msgpack!({
            "method": "eth_getBlockByNumber",
            "params": "[\"latest\",false]",
        })),
        None,
        None,
    );
    if let Ok(r) = response {
        assert_matches!(to_value(r), Ok(_))
    } else {
        panic!("{}", response.unwrap_err())
    }
}

#[test]
fn sign_typed_data() {
    let json = json!({
      "types": {
        "EIP712Domain": [
          {
            "name": "name",
            "type": "string"
          },
          {
            "name": "version",
            "type": "string"
          },
          {
            "name": "chainId",
            "type": "uint256"
          },
          {
            "name": "verifyingContract",
            "type": "address"
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
      },
      "primaryType": "Mail",
      "domain": {
        "name": "Ether Mail",
        "version": "1",
        "chainId": 1,
        "verifyingContract": "0xCcCCccccCCCCcCCCCCCcCcCccCcCCCcCcccccccC"
      },
      "message": {
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
    });

    let params = Value::Array(vec![
        Value::String("0x90F8bf6A479f320ead074411a4B0e7944Ea8c9C1".to_string()),
        json,
    ]);
    let client = get_client();
    let response = client.invoke::<String>(
        &Uri::try_from("plugin/ethereum-wallet").unwrap(),
        "request",
        Some(&msgpack!({
            "method": "eth_signTypedData_v4",
            "params": params.to_string(),
        })),
        None,
        None,
    );
    assert_eq!(response.unwrap(), "12bdd486cb42c3b3c414bb04253acfe7d402559e7637562987af6bd78508f38623c1cc09880613762cc913d49fd7d3c091be974c0dee83fb233300b6b58727311c".to_string());
}
