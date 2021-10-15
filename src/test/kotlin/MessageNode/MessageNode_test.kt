package MessageNode

import BridgeNode
import MessageDefinition
import MessageDispatcher
import MessageNode
import MetaData
import RawMessage
import Schema.CustomObjectSchema
import Schema.IntegerSchema

class _TestMessage(messageKey: String, val value: Int) : RawMessage(messageKey = messageKey, definition = DEFINITION) {
    companion object {
        val DEFINITION = MessageDefinition(name = "Test message", version = 0.1, schema = CustomObjectSchema(false, mapOf(
            "value" to IntegerSchema(false, 0xffffff, 0),
        )))
        fun fromRawMessage(message: RawMessage) = _TestMessage(message.messageKey, message.data["value"] as Int);
    }
  init {
    this.data["value"] = this.value;
  }
}

class NodeA : MessageNode(MetaData(uuid = "d5e9be78-2a96-11ec-8c46-1312bed55ca8", name = "Node A", author = "Dream lab software technologies"), mapOf(
    "xxx.yyy.aaa" to _TestMessage.DEFINITION,
), mapOf(
    "xxx.yyy.aaa" to _TestMessage.DEFINITION,
)) {
  var counter = 0;
  var value = -1;
  override fun handle(message: RawMessage) {
    if (message.sender!!.uuid == this.metaData.uuid) return;
    if (message.messageKey == "xxx.yyy.aaa") {
      val _message = _TestMessage.fromRawMessage(message);
      this.counter++;
      this.value = _message.value;
    }
  }
}

class NodeB : MessageNode(MetaData(uuid = "e0a20b86-2a96-11ec-923e-9ba7d7ac3025", name = "Node B", author = "Dream lab software technologies"), mapOf(
  "xxx.yyy.aaa" to _TestMessage.DEFINITION,
  "xxx.yyy.bbb" to _TestMessage.DEFINITION,
), mapOf(
  "xxx.yyy.aaa" to _TestMessage.DEFINITION,
  "xxx.yyy.bbb" to _TestMessage.DEFINITION,
  )) {
  var counter = 0;
  var value = -1;

  override fun handle(message: RawMessage) {
    if (message.sender!!.uuid == this.metaData.uuid) return;
    if (message.messageKey == "xxx.yyy.aaa") {
      val _message = _TestMessage.fromRawMessage(message);
      this.counter++;
      this.value = _message.value;
    }
    if (message.messageKey == "xxx.yyy.bbb") {
      val _message = _TestMessage.fromRawMessage(message);
      this.counter++;
      this.value = _message.value;
    }
  }
}

class NodeC : MessageNode(MetaData(uuid = "e900542c-2a96-11ec-88f0-07b737de8092", name = "Node C", author = "Dream lab software technologies"), mapOf(
  "xxx.yyy.aaa" to _TestMessage.DEFINITION,
  "xxx.yyy.bbb" to _TestMessage.DEFINITION,
  "xxx.yyy.ccc" to _TestMessage.DEFINITION,
  ), mapOf(
  "xxx.yyy.aaa" to _TestMessage.DEFINITION,
  "xxx.yyy.bbb" to _TestMessage.DEFINITION,
  "xxx.yyy.ccc" to _TestMessage.DEFINITION,
  )) {
  var counter = 0;
  var value = -1;

  override fun handle(message: RawMessage) {
    if (message.messageKey == "xxx.yyy.aaa") {
      val _message = _TestMessage.fromRawMessage(message);
      this.counter++;
      this.value = _message.value;
    }
    if (message.messageKey == "xxx.yyy.bbb") {
      val _message = _TestMessage.fromRawMessage(message);
      this.counter++;
      this.value = _message.value;
    }
    if (message.messageKey == "xxx.yyy.ccc") {
      val _message = _TestMessage.fromRawMessage(message);
      this.counter++;
      this.value = _message.value;
    }
  }
}

class _TestMessageDispatcherA : MessageDispatcher(MetaData(uuid = "ef7b46a4-2a96-11ec-a878-7b0bb0c8faaa", name = "Message dispatcher A", author = "Dream lab software technologies"))
class _TestMessageDispatcherB : MessageDispatcher(MetaData(uuid = "f51ef3f8-2a96-11ec-81d0-5734c7a638ed", name = "Message dispatcher B", author = "Dream lab software technologies"))
class _TestMessageDispatcherC : MessageDispatcher(MetaData(uuid = "005d8c52-2b32-11ec-b415-1724107572ce", name = "Message dispatcher C", author = "Dream lab software technologies"))
class _BridgeA : BridgeNode(MetaData(uuid = "0237fed6-2a97-11ec-98ca-5f18f9268373", name = "Bridge A", author = "Dream-Lab software technologies")) {
  var b: _BridgeB? = null
  override fun handle(message: RawMessage) {
    if (message.sender!!.uuid == this.metaData.uuid) return;
    b?.dispatch(message = message);
  }
}
class _BridgeB : BridgeNode(MetaData(uuid = "30466696-2a92-11ec-a5cf-0b932ee29b77", name = "Bridge B", author = "Dream-Lab software technologies")) {
  var a: _BridgeA? = null
  override fun handle(message: RawMessage) {
    if (message.sender!!.uuid == this.metaData.uuid) return;
    a?.dispatch(message = message);
  }
}

class _BridgeC : BridgeNode(MetaData(uuid = "fc167a7a-2b25-11ec-b30e-a7033bf53095", name = "Bridge C", author = "Dream-Lab software technologies")) {
  var d: _BridgeD? = null;
  override fun handle(message: RawMessage) {
    if (message.sender!!.uuid == this.metaData.uuid) return;
    d?.dispatch(message = message);
  }
}
class _BridgeD : BridgeNode(MetaData(uuid = "a73a2d0a-2b32-11ec-b7ae-cbe366c603d6", name = "Bridge D", author = "Dream-Lab software technologies")) {
  var c: _BridgeC? = null;
  override fun handle(message: RawMessage) {
    if (message.sender!!.uuid == this.metaData.uuid) return;
    c?.dispatch(message = message);
  }
}

