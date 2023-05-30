use std::assert_matches::assert_matches;

use polywrap_core::uri::Uri;
use polywrap_msgpack::msgpack;
use polywrap_plugin::JSON::{json, to_string, to_value, Value};

use crate::get_client;

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
    assert_eq!(response.unwrap(), to_string("0x38").unwrap());
}

#[test]
fn get_transaction_count() {
    let client = get_client();
    let response = client.invoke::<String>(
        &Uri::try_from("plugin/ethereum-wallet").unwrap(),
        "request",
        Some(&msgpack!({
            "method": "eth_getTransactionCount",
            "params": "[\"0xf3702506acec292cfaf748b37cfcea510dc37714\",\"latest\"]",
        })),
        None,
        None,
    );
    assert_ne!(response.unwrap(), "0x0");
}

#[test]
fn get_handle_eth_call() {
    let client = get_client();
    let response = client.invoke::<String>(
        &Uri::try_from("plugin/ethereum-wallet").unwrap(),
        "request",
        Some(&msgpack!({
            "method": "eth_call",
            "params": "[{\"data\":\"0x0dfe1681\",\"to\":\"0x8ad599c3a0ff1de082011efddc58f1908eb6e6d8\",\"type\":\"0x00\"},\"latest\"]",
            "connection": {
                "networkNameOrChainId": "mainnet"
            }
        })),
        None,
        None,
    );
    assert_eq!(
        response.unwrap(),
        to_string("0x000000000000000000000000a0b86991c6218b36c1d19d4a2e9eb0ce3606eb48").unwrap()
    );
}

#[test]
fn get_block_by_number() {
    let client = get_client();
    let response = client.invoke::<String>(
        &Uri::try_from("plugin/ethereum-wallet").unwrap(),
        "request",
        Some(&msgpack!({
            "method": "eth_getBlockByNumber",
            "params": "[\"latest\",false]",
        })),
        None,
        None,
    );
    if let Ok(r) = response {
        assert_matches!(to_value(r), Ok(_))
    } else {
        panic!("{}", response.unwrap_err())
    }
}

#[test]
fn sign_typed_data() {
    let payload = json!({
      "types": {
        "EIP712Domain": [
          {
            "name": "name",
            "type": "string"
          },
          {
            "name": "version",
            "type": "string"
          },
          {
            "name": "chainId",
            "type": "uint256"
          },
          {
            "name": "verifyingContract",
            "type": "address"
          }
        ],
        "Person": [
          {
              "name": "name",
              "type": "string"
          },
          {
              "name": "wallet",
              "type": "address"
          }
        ],
        "Mail": [
            {
                "name": "from",
                "type": "Person"
            },
            {
                "name": "to",
                "type": "Person"
            },
            {
                "name": "contents",
                "type": "string"
            }
          ]
      },
      "primaryType": "Mail",
      "domain": {
        "name": "Ether Mail",
        "version": "1",
        "chainId": 1,
        "verifyingContract": "0xCcCCccccCCCCcCCCCCCcCcCccCcCCCcCcccccccC"
      },
      "message": {
        "from": {
            "name": "Cow",
            "wallet": "0xCD2a3d9F938E13CD947Ec05AbC7FE734Df8DD826"
        },
        "to": {
            "name": "Bob",
            "wallet": "0xbBbBBBBbbBBBbbbBbbBbbbbBBbBbbbbBbBbbBBbB"
        },
        "contents": "Hello, Bob!"
      }
    });

    let params = Value::Array(vec![
        Value::String("0x90F8bf6A479f320ead074411a4B0e7944Ea8c9C1".to_string()),
        payload,
    ]);
    let client = get_client();
    let response = client.invoke::<String>(
        &Uri::try_from("plugin/ethereum-wallet").unwrap(),
        "request",
        Some(&msgpack!({
            "method": "eth_signTypedData_v4",
            "params": params.to_string(),
        })),
        None,
        None,
    );
    assert_eq!(response.unwrap(), "12bdd486cb42c3b3c414bb04253acfe7d402559e7637562987af6bd78508f38623c1cc09880613762cc913d49fd7d3c091be974c0dee83fb233300b6b58727311c");
}
