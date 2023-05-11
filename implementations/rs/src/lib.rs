use crate::wrap::wrap_info::get_manifest;
use connections::Connections;
use ethers::providers::{Http, Provider};
use handler::handle_call_params;
use polywrap_core::invoker::Invoker;
use polywrap_plugin::{
    error::PluginError,
    implementor::plugin_impl,
    JSON::{self, from_str, Value},
};
use std::sync::Arc;
use tokio::runtime::Runtime;
use wrap::module::{
    ArgsRequest, ArgsSignMessage, ArgsSignTransaction, ArgsSignerAddress, ArgsWaitForTransaction,
    Module,
};

pub mod connection;
pub mod connections;
mod handler;
mod networks;
mod wrap;

#[derive(Debug)]
pub struct EthereumWalletPlugin {
    connections: Connections,
}

impl EthereumWalletPlugin {
    pub fn new(connections: Connections) -> Self {
        Self {
            connections: connections,
        }
    }

    fn parse_parameters(&self, method: &str, params: &Option<String>) -> Vec<Value> {
        if self.is_transaction_method(method) {
            let mut call_parameters: Vec<Value> = vec![];
            if let Some(p) = params {
                return handle_call_params(p, &mut call_parameters);
            } else {
                panic!("Eth call needs arguments")
            }
        } else {
            if let Some(parameters) = params {
                from_str(&parameters).unwrap()
            } else {
                vec![]
            }
        }
    }

    fn is_transaction_method(&self, method: &str) -> bool {
        let transaction_methods = ["eth_sendTransaction", "eth_estimateGas", "eth_call"];
        transaction_methods.contains(&method)
    }
}

#[plugin_impl]
impl Module for EthereumWalletPlugin {
    fn request(&mut self, args: &ArgsRequest, _: Arc<dyn Invoker>) -> Result<String, PluginError> {
        let connection = self.connections.get_connection(args.connection.clone());
        let provider: Provider<Http> = connection.provider;
        let method = args.method.as_str();
        let parameters = self.parse_parameters(method, &args.params);
        let runtime = tokio::runtime::Runtime::new().unwrap();
        let response = Runtime::block_on(&runtime, provider.request(method, parameters));
        Ok(response.unwrap())
    }

    fn wait_for_transaction(
        &mut self,
        args: &ArgsWaitForTransaction,
        _: Arc<dyn Invoker>,
    ) -> Result<bool, PluginError> {
        todo!()
    }

    fn signer_address(
        &mut self,
        args: &ArgsSignerAddress,
        _: Arc<dyn Invoker>,
    ) -> Result<Option<String>, PluginError> {
        todo!()
    }

    fn sign_message(
        &mut self,
        args: &ArgsSignMessage,
        _: Arc<dyn Invoker>,
    ) -> Result<String, PluginError> {
        todo!()
    }

    fn sign_transaction(
        &mut self,
        args: &ArgsSignTransaction,
        _: Arc<dyn Invoker>,
    ) -> Result<String, PluginError> {
        todo!()
    }
}
