from typing import Any, Callable, Dict
from eth_account.signers.local import LocalAccount
from polywrap_client import PolywrapClient
from polywrap_core import Uri
import json

WithSigner = bool
provider_uri = Uri.from_str("plugin/ethereum-provider")


def test_eth_chain_id(client_factory: Callable[[WithSigner], PolywrapClient]):
    client = client_factory(False)
    result = client.invoke(
        uri=provider_uri,
        method="request",
        args={"method": "eth_chainId"},
        encode_result=False,
    )

    assert result == json.dumps("0xaa36a7")


def test_eth_get_transaction_count(client_factory: Callable[[WithSigner], PolywrapClient]):
    client = client_factory(False)
    result = client.invoke(
        uri=provider_uri,
        method="request",
        args={
            "method": "eth_getTransactionCount",
            "params": '["0xf3702506acec292cfaf748b37cfcea510dc37714","latest"]',
        },
        encode_result=False,
    )

    assert int(json.loads(result), base=16) > 0


def test_sign_typed_data(client_factory: Callable[[WithSigner], PolywrapClient]):
    client = client_factory(True)
    domain = {
        "name": "Ether Mail",
        "version": "1",
        "chainId": 1,
        "verifyingContract": "0xCcCCccccCCCCcCCCCCCcCcCccCcCCCcCcccccccC",
    }

    types = {
        "EIP712Domain": [
            {"type": "string", "name": "name"},
            {"type": "string", "name": "version"},
            {
                "type": "uint256",
                "name": "chainId",
            },
            {
                "type": "address",
                "name": "verifyingContract",
            },
        ],
        "Person": [
            {"name": "name", "type": "string"},
            {"name": "wallet", "type": "address"},
        ],
        "Mail": [
            {"name": "from", "type": "Person"},
            {"name": "to", "type": "Person"},
            {"name": "contents", "type": "string"},
        ],
    }

    message = {
        "from": {"name": "Cow", "wallet": "0xCD2a3d9F938E13CD947Ec05AbC7FE734Df8DD826"},
        "to": {"name": "Bob", "wallet": "0xbBbBBBBbbBBBbbbBbbBbbbbBBbBbbbbBbBbbBBbB"},
        "contents": "Hello, Bob!",
    }

    params = json.dumps(
        [
            "0x90F8bf6A479f320ead074411a4B0e7944Ea8c9C1",
            {
                "domain": domain,
                "primaryType": "Mail",
                "types": types,
                "message": message,
            },
        ]
    )

    result = client.invoke(
        uri=provider_uri,
        method="request",
        args={
            "method": "eth_signTypedData_v4",
            "params": params,
        },
        encode_result=False,
    )

    assert result == json.dumps(
        "0x12bdd486cb42c3b3c414bb04253acfe7d402559e7637562987af6bd78508f38623c1cc09880613762cc913d49fd7d3c091be974c0dee83fb233300b6b58727311c"
    )


async def test_send_transaction(client_factory: Callable[[WithSigner], PolywrapClient], account: LocalAccount):
    params: Dict[str, Any] = {
        'from': account.address,  # type: ignore
        'to': "0xcb93799A0852d94B65166a75d67ECd923fD951E4",
        'value': 1000,
        'gas': 21000,
        'gasPrice': 50000000000,
        'nonce': 0,
    }

    client = client_factory(True)
    result = client.invoke(
        uri=provider_uri,
        method="request",
        args={
            "method": "eth_sendTransaction",
            "params": json.dumps(params),
            "connection": {
                "networkNameOrChainId": "mocknet",
            }
        },
        encode_result=False,
    )
    tx_hash = json.loads(result)

    assert isinstance(tx_hash, str)
    assert len(tx_hash) == 66
    assert tx_hash.startswith('0x')
