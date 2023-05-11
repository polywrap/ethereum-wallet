use std::{collections::HashMap, str::FromStr, sync::Arc};

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
    JSON::{to_value, Value},
};
use serde::{Deserialize, Serialize};

fn get_client() -> PolywrapClient {
    let connection =
        Connection::new("https://bsc-dataseed1.binance.org/".to_string(), None).unwrap();
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
