use crate::wrap::wrap_info::get_manifest;
use connections::Connections;
use ethers::providers::{Http, Provider, ProviderError};
use polywrap_core::invoker::Invoker;
use polywrap_plugin::{
    error::PluginError,
    implementor::plugin_impl,
    JSON::{self, Value},
};
use std::sync::Arc;
use tokio::runtime::{Handle, Runtime};
use wrap::module::{
    ArgsRequest, ArgsSignMessage, ArgsSignTransaction, ArgsSignerAddress, ArgsWaitForTransaction,
    Module,
};

pub mod connection;
pub mod connections;
pub mod networks;
pub mod wrap;

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
}

#[plugin_impl]
impl Module for EthereumWalletPlugin {
    fn request(&mut self, args: &ArgsRequest, _: Arc<dyn Invoker>) -> Result<String, PluginError> {
        let connection = self.connections.get_connection(args.connection.clone());
        let provider: Provider<Http> = connection.provider;
        let runtime = tokio::runtime::Runtime::new().unwrap();
        let response = Runtime::block_on(
            &runtime,
            provider.request(args.method.as_str(), &args.params),
        );
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
