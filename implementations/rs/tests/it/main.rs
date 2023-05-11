use std::{collections::HashMap, sync::Arc};

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
use polywrap_plugin::package::PluginPackage;

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
    dbg!(response);
}
