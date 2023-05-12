#![feature(assert_matches)]
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
use std::{collections::HashMap, sync::Arc};

pub mod request;

fn get_client() -> PolywrapClient {
    let connection = Connection::new(
        "https://bsc-dataseed1.binance.org/".to_string(),
        Some(String::from(
            "0x4f3edf983ac636a65a842ce7c78d9aa706d3b113bce9c46f30d7d21715b23b1d",
        )),
    )
    .unwrap();
    let connections = Connections::new(
        HashMap::from([("bsc".to_string(), connection)]),
        Some("bsc".to_string()),
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
fn get_signer_address() {
    let client = get_client();
    let response = client.invoke::<String>(
        &Uri::try_from("plugin/ethereum-wallet").unwrap(),
        "signerAddress",
        None,
        None,
        None,
    );
    assert_eq!(
        response.unwrap(),
        "0x90f8bf6a479f320ead074411a4b0e7944ea8c9c1".to_string()
    )
}

#[test]
// @TODO(cbrzn): Remove ignore once https://github.com/polywrap/rust-client/issues/59 its fixed
#[ignore]
fn sign_message() {
    let client = get_client();
    let response = client.invoke::<String>(
        &Uri::try_from("plugin/ethereum-wallet").unwrap(),
        "signMessage",
        Some(&msgpack!({
            "message": "Hello World".as_bytes()
        })),
        None,
        None,
    );
    assert_eq!(
        response.unwrap(),
        "0xa4708243bf782c6769ed04d83e7192dbcf4fc131aa54fde9d889d8633ae39dab03d7babd2392982dff6bc20177f7d887e27e50848c851320ee89c6c63d18ca761c".to_string()
    )
}