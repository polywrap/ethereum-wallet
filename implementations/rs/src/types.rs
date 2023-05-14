use ethers::types::{
    transaction::{eip712::TypedData},
    TransactionRequest,
};
use polywrap_plugin::JSON::{from_str, to_value, Value};
use serde::{Deserialize, Serialize};

#[derive(Serialize, Deserialize, Debug)]
#[serde(untagged)]
pub enum EthCallParamaterTypes {
    Tx(TransactionRequest),
    Tag(String),
}

#[derive(Serialize, Deserialize)]
pub struct GetBlockByNumberParameters {
    block_tag: String,
    flag: bool,
}

#[derive(Serialize, Deserialize, Debug)]
#[serde(untagged)]
pub enum GetBlockByNumberParamaterTypes {
    Tag(String),
    Flag(bool),
}

#[derive(Serialize, Deserialize, Debug)]
#[serde(untagged)]
pub enum SignTypedDataArgs {
    Address(String),
    TypedData(TypedData),
}


pub fn parse_params<T: Serialize + for<'a> Deserialize<'a> + std::fmt::Debug>(
    values: &str,
) -> Vec<Value> {
    let params_value = from_str::<Vec<T>>(values);

    if let Ok(v) = params_value {
        v.iter()
            .map(|value| to_value(value).unwrap())
            .collect::<Vec<Value>>()
    } else {
        let err = params_value.unwrap_err();
        panic!("Error parsing paremeters: {}", err)
    }
}