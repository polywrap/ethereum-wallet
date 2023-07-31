/// NOTE: This is an auto-generated file.
///       All modifications will be overwritten.

package io.polywrap.plugins.ethereum.wrap

import io.polywrap.core.msgpack.GenericMapExtensionSerializer
import kotlinx.serialization.Serializable

typealias GenericMap<K, V> = @Serializable(with = GenericMapExtensionSerializer::class) io.polywrap.core.msgpack.GenericMap<K, V>

typealias BigInt = String
typealias BigNumber = String
typealias Json = String

/// Env START ///
@Serializable
data class Env(
    val connection: Connection? = null,
)
/// Env END ///

/// Objects START ///
@Serializable
data class Connection(
    val node: String? = null,
    val networkNameOrChainId: String? = null,
)

/// Objects END ///

/// Enums START ///
/// Enums END ///

/// Imported Objects START ///
/// Imported Objects END ///

/// Imported Modules START ///
/// Imported Modules END ///
