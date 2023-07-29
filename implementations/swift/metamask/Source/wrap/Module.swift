// NOTE: This is an auto-generated file.
//       All modifications will be overwritten.

import PolywrapClient
import Foundation

public struct ArgsRequest: Codable {
    var method: String
    var params: String?
    var connection: Connection?
}

public struct ArgsWaitForTransaction: Codable {
    var txHash: String
    var confirmations: UInt32
    var timeout: UInt32?
    var connection: Connection?
}

public struct ArgsSignerAddress: Codable {
    var connection: Connection?
}

public struct ArgsSignMessage: Codable {
    var message: Data
    var connection: Connection?
}

public struct ArgsSignTransaction: Codable {
    var rlp: Data
    var connection: Connection?
}


public protocol Plugin: PluginModule {
    func request(_ args: ArgsRequest, _ env: VoidCodable?, _ invoker: Invoker) throws -> String

    func waitForTransaction(_ args: ArgsWaitForTransaction, _ env: VoidCodable?, _ invoker: Invoker) throws -> Bool

    func signerAddress(_ args: ArgsSignerAddress, _ env: VoidCodable?, _ invoker: Invoker) throws -> String?

    func signMessage(_ args: ArgsSignMessage, _ env: VoidCodable?, _ invoker: Invoker) throws -> String

    func signTransaction(_ args: ArgsSignTransaction, _ env: VoidCodable?, _ invoker: Invoker) throws -> String
}
