package SharedData

import BridgeNode
import MessageDispatcher
import MetaData
import RawMessage
import SharedDataNode

/**
 * @author Dream-Lab software technologies muhtarjan mahmood(مۇختەرجان مەخمۇت)
 * @email ug-project@outlook.com
 * @create date 2021-10-10 00:02:01
 * @modify date 2021-10-10 00:02:01
 * @desc [description]
 */


class DataNodeA : SharedDataNode("AppData", MetaData(uuid = "424f64a4-292b-11ec-baac-43b4c3887f35", name = "Data node A", author= "Dream lab software technologies")) {
  override fun onSyncCompleted() {
    // print("Sync completed in A");
  }
}
class DataNodeB : SharedDataNode("AppData", MetaData(uuid = "6478add8-292b-11ec-8691-23b054478435", name = "Data node B", author= "Dream lab software technologies")) {
  override fun onSyncCompleted() {
    // print("Sync completed in B");
  }
}
class DataNodeC : SharedDataNode("AppData", MetaData(uuid = "64beba08-292b-11ec-8baf-474e0272e1dc", name = "Data node C", author= "Dream lab software technologies")) {
  override fun onSyncCompleted() {
    // print("Sync completed in C");
  }
}

class DataNodeD : SharedDataNode("AppData", MetaData(uuid = "a712773e-292e-11ec-88d4-d789edcc9266", name = "Data node D", author= "Dream lab software technologies")) {
  override fun onSyncCompleted() {
    // print("Sync completed in D");
  }
}

class _TestMessageDispatcherA : MessageDispatcher(MetaData(uuid = "ef7b46a4-2a96-11ec-a878-7b0bb0c8faaa", name = "Message dispatcher A", author= "Dream lab software technologies"))
class _TestMessageDispatcherB : MessageDispatcher(MetaData(uuid = "f51ef3f8-2a96-11ec-81d0-5734c7a638ed", name = "Message dispatcher B", author= "Dream lab software technologies"))
class _TestMessageDispatcherC : MessageDispatcher(MetaData(uuid = "005d8c52-2b32-11ec-b415-1724107572ce", name = "Message dispatcher C", author= "Dream lab software technologies"))
class _BridgeA : BridgeNode(MetaData(uuid = "0237fed6-2a97-11ec-98ca-5f18f9268373", name = "Bridge A", author = "Dream-Lab software technologies")) {
  var b: _BridgeB? = null;
  override fun handle(message: RawMessage) {
    if (message.sender!!.uuid == this.metaData.uuid) return;
    b?.dispatch(message = message)
  }
}
class _BridgeB : BridgeNode(MetaData(uuid = "30466696-2a92-11ec-a5cf-0b932ee29b77", name = "Bridge B", author = "Dream-Lab software technologies")) {
  var a: _BridgeA? = null;
  override fun handle(message: RawMessage) {
    if (message.sender!!.uuid == this.metaData.uuid) return;
    a?.dispatch(message = message)
  }
}
class _BridgeC : BridgeNode(MetaData(uuid = "fc167a7a-2b25-11ec-b30e-a7033bf53095", name = "Bridge C", author = "Dream-Lab software technologies")) {
  var d: _BridgeD? = null;
  override fun handle(message: RawMessage) {
    if (message.sender!!.uuid == this.metaData.uuid) return;
    d?.dispatch(message = message)
  }
}
class _BridgeD : BridgeNode(MetaData(uuid = "a73a2d0a-2b32-11ec-b7ae-cbe366c603d6", name = "Bridge D", author = "Dream-Lab software technologies")) {
  var c: _BridgeC? = null;

  override fun handle(message: RawMessage) {
    if (message.sender!!.uuid == this.metaData.uuid) return;
    c?.dispatch(message = message)
  }
}

