use crate::networks::{get_name, KnownNetwork};
use ethers::providers::{Http, Provider};
use std::fmt::Debug;

pub struct Connection {
    pub provider: Provider<Http>,
    pub signer: Option<String>,
}

impl Debug for Connection {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "Connection")
    }
}

impl Connection {
    pub fn new(provider: String, signer: Option<String>) -> Result<Self, String> {
        let provider = Provider::<Http>::try_from(provider);
        if let Err(e) = provider {
            return Err(format!(
                "Error getting provider from network: {}",
                e.to_string()
            ));
        } else {
            Ok(Self {
                provider: provider.unwrap(),
                signer: signer,
            })
        }
    }

    pub fn from_node(node: String, signer: Option<String>) -> Result<Self, String> {
        let connection = Connection::new(node, signer);
        if let Err(e) = connection {
            return Err(format!(
                "Error creating connection in from_node method: {}",
                e.to_string()
            ));
        } else {
            connection
        }
    }

    pub fn from_network(network: KnownNetwork, signer: Option<String>) -> Result<Self, String> {
        let name = get_name(network);

        if let None = name {
            return Err(format!("Given network: {:#?} is not supported", network));
        };

        let name = name.unwrap();
        let connection = Connection::new(
            format!("https://{name}.infura.io/v3/1a8e6a8ab1df44ccb77d3e954082c5d4"),
            signer,
        );
        if let Err(e) = connection {
            return Err(format!(
                "Error creating connection in from_network method: {}",
                e.to_string()
            ));
        } else {
            connection
        }
    }

    pub fn has_signer(self) -> bool {
        self.signer.is_some()
    }
}
