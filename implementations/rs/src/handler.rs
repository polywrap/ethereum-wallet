use ethers::types::TransactionRequest;
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

pub fn handle_call_params(values: &String, container: &mut Vec<Value>) -> Vec<Value> {
    let params_value = from_str::<Vec<EthCallParamaterTypes>>(values.as_str());

    if let Ok(v) = params_value {
        for value in v {
            container.push(to_value(value).unwrap())
        }
    } else {
        let err = params_value.unwrap_err();
        panic!("{}", err)
    }
    container.to_vec()
}
