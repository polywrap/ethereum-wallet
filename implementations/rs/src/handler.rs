use ethers::types::{
    transaction::{eip2718::TypedTransaction, eip712::TypedData},
    TransactionRequest,
};
use polywrap_plugin::JSON::{from_str, to_value, Value};
use serde::{Deserialize, Serialize};

#[derive(Serialize, Deserialize)]
pub struct EthCallParameters {
    transaction: TransactionRequest,
    block_tag: String,
}

#[derive(Serialize, Deserialize, Debug)]
#[serde(untagged)]
enum EthCallParamaterTypes {
    Tx(TransactionRequest),
    Tag(String),
}

//@TODO: Pass a generic to this handle params function and re-use it with different types
pub fn handle_call_params(values: &str) -> Vec<Value> {
    let params_value = from_str::<Vec<EthCallParamaterTypes>>(values);

    if let Ok(v) = params_value {
        v.iter()
            .map(|value| to_value(value).unwrap())
            .collect::<Vec<Value>>()
    } else {
        let err = params_value.unwrap_err();
        panic!("Error parsing eth_call paremeters: {}", err)
    }
}

pub fn handle_send_params(values: &str) -> Vec<Value> {
    let params_value = from_str::<TypedTransaction>(values);
    if let Ok(v) = params_value {
        vec![to_value(v).unwrap()]
    } else {
        let err = params_value.unwrap_err();
        panic!("Error parsing transaction: {}", err)
    }
}

#[derive(Serialize, Deserialize)]
pub struct GetBlockByNumberParameters {
    block_tag: String,
    flag: bool,
}

#[derive(Serialize, Deserialize, Debug)]
#[serde(untagged)]
enum GetBlockByNumberParamaterTypes {
    Tag(String),
    Flag(bool),
}

pub fn handle_get_block_by_number_params(values: &str) -> Vec<Value> {
    let params_value = from_str::<Vec<GetBlockByNumberParamaterTypes>>(values);

    if let Ok(v) = params_value {
        v.iter()
            .map(|value| to_value(value).unwrap())
            .collect::<Vec<Value>>()
    } else {
        let err = params_value.unwrap_err();
        panic!("Error parsing eth_call paremeters: {}", err)
    }
}

#[derive(Serialize, Deserialize, Debug)]
#[serde(untagged)]
enum SignTypedDataArgs {
    Address(String),
    TypedData(TypedData),
}

pub fn handle_sign_typed_data_args(values: &str) -> Vec<Value> {
    let params_value = from_str::<Vec<SignTypedDataArgs>>(values);

    if let Ok(v) = params_value {
        v.iter()
            .map(|value| to_value(value).unwrap())
            .collect::<Vec<Value>>()
    } else {
        let err = params_value.unwrap_err();
        panic!("Error parsing eth_signTypedData paremeters: {}", err)
    }
}
