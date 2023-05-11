use std::collections::HashMap;

use crate::{
    connection::Connection,
    networks::{from_alias, KnownNetwork},
};

use super::wrap::types::Connection as SchemaConnection;

#[derive(Debug)]
pub struct Connections {
    pub connections: HashMap<String, Connection>,
    pub default_network: String,
}

impl Connections {
    pub fn new(connections: HashMap<String, Connection>, default_network: Option<String>) -> Self {
        let mainnet_string = String::from("mainnet");
        let (default_network, connections) = if let Some(default_network) = default_network {
            if let None = from_alias(default_network.as_str()) {
                panic!(
                    "{}",
                    format!("Default network: {default_network} not in connections")
                )
            }
            (default_network, connections)
        } else if let Some(_) = connections.get("mainnet") {
            (mainnet_string, connections)
        } else {
            let mainnet_connection = Connection::from_network(KnownNetwork::Mainnet, None).unwrap();
            let connections = HashMap::from([("mainnet".to_string(), mainnet_connection)]);
            (mainnet_string, connections)
        };

        Self {
            connections: connections,
            default_network: default_network,
        }
    }

    pub fn get_connection(&self, connection: Option<SchemaConnection>) -> Connection {
        let fetched_connection = if let Some(c) = connection.clone() {
            if let Some(n) = c.network_name_or_chain_id {
                Connection::from_network(from_alias(n.as_str()).unwrap(), None)
            } else if let Some(node_url) = c.node {
                Connection::from_node(node_url, None)
            } else {
                let network = from_alias(&self.default_network);
                return Connection::from_network(network.unwrap(), None).unwrap();
            }
        } else {
            let network = from_alias(&self.default_network);
            if network.is_none() {
                panic!("{}", format!("Connection: {:#?} not found", connection))
            };

            return if let Some(c) = self.connections.get(&self.default_network) {
                Connection {
                    provider: c.provider.clone(),
                    signer: c.signer.clone(),
                }
            } else {
                Connection::from_network(network.unwrap(), None).unwrap()
            };
        };

        if let Ok(c) = fetched_connection {
            c
        } else {
            panic!("{}", format!("Connection: {:#?} not found", connection))
        }
    }
}