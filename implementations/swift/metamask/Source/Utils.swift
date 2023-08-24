import Foundation
import metamask_ios_sdk
import SocketIO

func buildHttpRequest(body: Data?) -> URLRequest {
    let endpoint = URL(string: "https://eth-goerli.g.alchemy.com/v2/zzLPStDTNNQdylflTNS_JN9Pc6y_u8_u")!
    var request = URLRequest(url: endpoint)
    request.httpMethod = "post"
    request.httpBody = body
    request.addValue("application/json", forHTTPHeaderField: "accept")
    request.addValue("application/json", forHTTPHeaderField: "content-type")

    return request
}

func handleHttpRequest(req: URLRequest, completion: @escaping (Result<String, Error>) -> Void) {
            let task = URLSession.shared.dataTask(with: req) { (data, _, error) in
            if let error = error {
                return completion(.failure(error))
            } else if let data = data {
                let json = try! JSONSerialization.jsonObject(with: data, options: []) as! [String: Any]
                let stringJson = try! JSONSerialization.data(withJSONObject: json["result"])
                return completion(.success(String(data: stringJson, encoding: .utf8)!))
            } else {
                print("unexpected error")
            }
        }

       task.resume()
}

public struct Transaction: CodableData {
    let from: String?
    let data: String
    let type: String?
    let value: String?
    let to: String?

    public init(to: String? = nil, from: String? = nil, value: String? = nil, data: String, type: String? = nil) {
        self.to = to
        self.from = from
        self.value = value
        self.data = data
        self.type = type
    }

    public init?(json: [String: Any]) {
        guard let data = json["data"] as? String
        else {
            return nil
        }

        self.data = data

        if let type = json["type"] as? String {
            self.type = type
        } else {
            self.type = nil
        }

        if let from = json["from"] as? String {
            self.from = from
        } else {
            self.from = nil
        }

        if let to = json["to"] as? String {
            self.to = to
        } else {
            self.to = nil
        }

        if let value = json["value"] as? String {
            self.value = value
        } else {
            self.value = nil
        }
    }
}

public enum ProviderError: Error {
    case notConnected
    case encodeError
    case methodNotSupported
    case dataCorruptedError
}

public enum KnownCodable: Codable {
    case string(String)
    case int(Int)
    case dict([String: KnownCodable])

    public init(from decoder: Decoder) throws {
        let container = try decoder.singleValueContainer()
        if let typedDataValue = try? container.decode(Int.self) {
            self = .int(typedDataValue)
        } else if let stringValue = try? container.decode(String.self) {
            self = .string(stringValue)
        } else if let dictValue = try? container.decode([String: KnownCodable].self) {
            self = .dict(dictValue)
        } else {
            throw DecodingError.dataCorruptedError(
                in: container,
                debugDescription: "Unable to decode KnownCodable"
            )
        }
    }

    public func encode(to encoder: Encoder) throws {
        var container = encoder.singleValueContainer()

        switch self {
        case .string(let stringValue):
            try container.encode(stringValue)
        case .int(let intValue):
            try container.encode(intValue)
        case .dict(let dictionaryValue):
            try container.encode(dictionaryValue)
        }
    }
}

public enum TxOrString: Codable {
    case string(String)
    case transaction(Transaction)

    public init(from decoder: Decoder) throws {
        let container = try decoder.singleValueContainer()

        if let txValue = try? container.decode(Transaction.self) {
            self = .transaction(txValue)
        } else if let stringValue = try? container.decode(String.self) {
            self = .string(stringValue)
        } else {
            throw DecodingError.dataCorruptedError(
                in: container,
                debugDescription: "Unable to decode TxOrString"
            )
        }
    }

    public func encode(to encoder: Encoder) throws {
        var container = encoder.singleValueContainer()

        switch self {
        case .string(let stringValue):
            try container.encode(stringValue)
        case .transaction(let txValue):
            try container.encode(txValue)
        }
    }

    func toString() -> String {
        switch self {
        case .transaction(let v):
            let encoder = JSONEncoder()
            let jsonData = try! encoder.encode(v)
            let string = String(data: jsonData, encoding: .utf8)!
            return string
        case.string(let v):
            return v
        }
    }

    func toTransaction() -> Transaction? {
        switch self {
        case .transaction(let v):
            return v
        case .string:
            return nil
        }
    }
}

public struct ParamsEthCall: CodableData {
    public var tx: Transaction
    public var tag: String

    public init(tx: Transaction, tag: String) {
        self.tx = tx
        self.tag = tag
    }

    public func socketRepresentation() throws -> SocketData {
        return [
         [
            "data": self.tx.data,
            "type": self.tx.type,
            "to": self.tx.to
         ], self.tag
        ]
    }
}

public struct CustomBoolOrStringArray: CodableData {
    let tag: String
    let include: Bool

    public func socketRepresentation() -> NetworkData {
        [self.tag, self.include]
    }
}

public struct Domain: Codable {
    public var name: String
    public var version: String
    public var chainId: Int
    public var verifyingContract: String
}

public struct TypedData: Codable {
    var domain: Domain
    var message: [String: KnownCodable]
    var primaryType: String
    var types: [String: [String: String]]
}
