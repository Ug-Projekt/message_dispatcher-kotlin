import Schema.CustomObjectSchema
import Schema.IntegerSchema

/**
 * @author Dream-Lab software technologies muhtarjan mahmood(مۇختەرجان مەخمۇت)
 * @email ug-project@outlook.com
 * @create date 2021-10-09 17:51:17
 * @modify date 2021-10-09 17:51:17
 * @desc [description]
 */

open class SharedDataSyncMessage(messageKey: String, val sharedData: Map<String, Any>, val dataVersion: Int) : RawMessage(messageKey, DEFINITION) {
  companion object {
    val DEFINITION = MessageDefinition(name = "Shared data sync message", version = 0.1, schema = CustomObjectSchema(false, mapOf(
      // "created": CustomObjectSchema(false, {}),
      // "updated": CustomObjectSchema(false, {}),
      // "deleted": CustomObjectSchema(false, {}),
      "version" to IntegerSchema(false, 999999999, 0),
      "data" to CustomObjectSchema(false, mapOf()),
    )))
    fun fromRawMessage(message: RawMessage): SharedDataSyncMessage {
      val result = SharedDataSyncMessage(message.messageKey, message.data["data"] as Map<String, Any>, message.data["version"] as Int);
      result.sender = message.sender;
      return result;
    }
  }
  // final Map<String, dynamic> created;
  // final Map<String, dynamic> updated;
  // final Map<String, dynamic> deleted;
  init {
    this.data["data"] = this.sharedData;
    this.data["version"] = this.dataVersion;
  }
}

class SharedDataRequestDataMessage(messageKey: String): RawMessage(messageKey = messageKey, definition = DEFINITION) {
  companion object {
    val DEFINITION = MessageDefinition(name = "Shared data request message", version = 0.1, schema = CustomObjectSchema(false, mapOf()))
  }
}

abstract class SharedDataNode(val key: String, metaData: MetaData) : MessageNode(metaData, mapOf(
  "shared-data.$key.changed" to SharedDataSyncMessage.DEFINITION,
  "shared-data.$key.ready" to SharedDataSyncMessage.DEFINITION,
  "shared-data.$key.request-data" to SharedDataRequestDataMessage.DEFINITION,
), mapOf(
  "shared-data.$key.ready" to SharedDataSyncMessage.DEFINITION,
  "shared-data.$key.changed" to SharedDataSyncMessage.DEFINITION,
  "shared-data.$key.request-data" to SharedDataRequestDataMessage.DEFINITION,
)) {
  var dataVersion = 0
  var data = HashMap<String, Any>()
  init {
    assert(!key.contains(".") || !key.contains("*"))
  }

  override fun handle(message: RawMessage) {
    if (message.sender!!.uuid == this.metaData.uuid) return;
    if (message.messageKey == "shared-data.$key.changed" || message.messageKey == "shared-data.$key.ready") {
      val changeMessage = SharedDataSyncMessage.fromRawMessage(message);
      if (changeMessage.dataVersion == this.dataVersion) return;
      if (changeMessage.dataVersion < this.dataVersion) {
        this.notifySync();
        return;
      }
      this.data.clear();
      this.data.putAll(changeMessage.sharedData);
      this.dataVersion = changeMessage.dataVersion;
      onSyncCompleted();
    }
    val isRequestDataMessage = message.messageKey == "shared-data.$key.request-data";
    if (isRequestDataMessage) {
      this.dispatch(message = SharedDataSyncMessage("shared-data.$key.ready", data, dataVersion));
    }
  }
  fun notifySync(){
    dataVersion++;
    val message = SharedDataSyncMessage("shared-data.$key.changed", data, dataVersion);
    this.dispatch(message = message);
  }
  abstract fun onSyncCompleted()
  override fun onConnected() {
    this.dispatch(message = SharedDataRequestDataMessage("shared-data.$key.request-data"));
    super.onConnected();
  }
}
