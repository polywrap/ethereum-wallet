use ethers::types::transaction::eip712::TypedData;
use serde::{Deserialize, Serialize};

#[derive(Serialize, Deserialize, Debug)]
pub struct CallTransaction {
    #[serde(skip_serializing_if = "Option::is_none")]
    pub to: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub data: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none", rename = "type")]
    pub _type: Option<String>,
}

#[derive(Serialize, Deserialize, Debug)]
#[serde(untagged)]
pub enum EthCallParamaterTypes {
    Tx(CallTransaction),
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
