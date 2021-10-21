package Messages;


import MessageDefinition
import MetaData
import RawMessage
import Schema.CustomObjectSchema;
import Schema.StringSchema
import java.util.*

/**
 * @author Dream-Lab software technologies muhtarjan mahmood(مۇختەرجان مەخمۇت)
 * @email ug-project@outlook.com
 * @create date 2021-10-09 16:01:08
 * @modify date 2021-10-09 16:01:08
 * @desc [description]
 */
class NodeChangedMessage(val node: MetaData, val dispatcher: MetaData, messageKey: String) : RawMessage(messageKey, DEFINITION) {
  companion object {
    val DEFINITION = MessageDefinition(name = "Node connected message", version = 0.1, schema = CustomObjectSchema(false, mapOf(
      "node" to CustomObjectSchema(false, mapOf(
        "name" to StringSchema(false, 1, 255),
        "uuid" to StringSchema(false, 32, 255),
        "author" to StringSchema(false, 1, 255),
      )),
      "dispatcher" to CustomObjectSchema(false, mapOf(
        "name" to StringSchema(false, 1, 255),
        "uuid" to StringSchema(false, 32, 255),
        "author" to StringSchema(false, 1, 255),
      ))
    )))
  }
  init {
    this.data["node"] = this.node.data;
    this.data["dispatcher"] = this.dispatcher.data;
  }
}

class NodeErrorMessage(val node: MetaData, val dispatcher: MetaData, val errorMessage: String, val stackTrace: String, messageKey: String) : RawMessage(messageKey, DEFINITION) {
  companion object {
    val DEFINITION = MessageDefinition(name = "Node error message", version = 0.1, schema = CustomObjectSchema(false, mapOf(
      "node" to (NodeChangedMessage.DEFINITION.schema as CustomObjectSchema).properties["node"]!!,
      "dispatcher" to (NodeChangedMessage.DEFINITION.schema as CustomObjectSchema).properties["dispatcher"]!!,
      "errorMessage" to StringSchema(false, 1, 255),
      )));

    fun fromRawMessage(message: RawMessage): NodeErrorMessage {
      val value = NodeErrorMessage(MetaData.fromMap(message.data["node"] as Map<String, Any>), MetaData.fromMap(message.data["dispatcher"] as Map<String, Any>), message.data["errorMessage"] as String, message.data["stackTrace"] as String, message.messageKey);
      value.sender = message.sender;
      return value;
    }
  }

  init {
    this.data["node"] = this.node.data;
    this.data["dispatcher"] = this.dispatcher.data;
    this.data["errorMessage"] = this.errorMessage;
    this.data["stackTrace"] = this.stackTrace;
  }
}



