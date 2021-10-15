
import Messages.NodeChangedMessage
import Messages.NodeErrorMessage
import Schema.CustomObjectSchema
import Schema.ObjectSchema
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * @author Dream-Lab software technologies muhtarjan mahmood(مۇختەرجان مەخمۇت)
 * @email ug-project@outlook.com
 * @create date 2021-10-05 17:40:06
 * @modify date 2021-10-05 17:40:06
 * @desc [description]
 */

open class MetaData(uuid: String, name: String, author: String) {
    companion object {
        fun fromMap(map: Map<String, Any>): MetaData {
            return MetaData(uuid = map["uuid"] as String, name = map["name"] as String, author = map["author"] as String);
        }
    }
    val data = HashMap<String, Any>();
    init {
        this.data["name"] = name
        this.data["author"] = author
        this.data["uuid"] = uuid
    }
    val name get() = this.data["name"] as String
    val uuid get() = this.data["uuid"] as String
    val author get() = this.data["author"] as String

    override fun toString(): String = this.name;
}

open class MessageDefinition(val name: String, val version: Double, val schema: ObjectSchema)

open class RawMessage(val messageKey: String, val definition: MessageDefinition, val id: String? = UUID.randomUUID().toString()) {
    var sender: MetaData? = null
    val data = HashMap<String, Any>()
    fun cloneSelf(): RawMessage {
        val copyed = RawMessage(messageKey = messageKey, definition = definition, id = id)
        val cloned = this.data.deepClone();
        copyed.data.putAll(cloned);
        return copyed;
    }
}

abstract class MessageNode(val metaData: MetaData, val sendMessageKeys: Map<String, MessageDefinition>, val receiveMessageKeys: Map<String, MessageDefinition>) {
    val dispatcher get() = connection?.dispatcher;
    var connection: MessageNodeConnectionTicket? = null
    abstract fun handle(message: RawMessage)
    fun dispatch(message: RawMessage) {
        if (this.dispatcher == null) throw Exception("Message node '${this.metaData}' is not connected to any message dispatcher, please connect to message dispatcher before using it please.");
        this.dispatcher!!.dispatch(this, message);
    }
    open fun onConnected() {}
    open fun onDisconnected() {}
}

abstract class BridgeNode(metaData: MetaData) : MessageNode(metaData, sendMessageKeys = mapOf(), receiveMessageKeys = mapOf(
    "*.*.*" to MessageDefinition(name = "Bridged message", version = 0.1, schema = CustomObjectSchema(false, mapOf()))
))

class MessageNodeConnectionTicket(val dispatcher: MessageDispatcher, val connectionId: String) {
    fun disconnect() {
        this.dispatcher.disconnect(this.connectionId);
    }
}
class ConnectionDeniedException(message: String) : Exception(message)
class MessageValidationException(message: String) : Exception(message) {}

abstract class MessageDispatcher(val metaData: MetaData) {
    val _nodes = HashMap<String, MessageNode>()
    var _internalNode: _DispatcherInternalNode
    // final TreeNodeBase _messageNodeReceiveKeysIndex = TreeNodeBase("");
    init {
        this._internalNode = _DispatcherInternalNode(MetaData(uuid = this.metaData.uuid, name = "Internal node of '${this.metaData.name}'", author = this.metaData.author))
        this.connect(this._internalNode);
    }
    ///For speed up dispatch event, we will index all [MessageNode]'s receive keys as a tree so we can fast access corresponding destination [MessageNode] of message key
    // void reCreateMessageReceiverIndex() {
    //   this._messageNodeReceiveKeysIndex.children.clear();
    //   TreeNodeBase _createTreeNodeByKey(TreeNodeBase parentTree, List<String> keys, int currentIndex, MessageNode node) {
    //     if (currentIndex == keys.length) {
    //       TreeDataNode("", parentTree, node);
    //       return parentTree;
    //     }
    //     final key = keys[currentIndex];
    //     var childNodes = parentTree.children.where((element) => element.key == key).toList();
    //     if (childNodes.isEmpty) {
    //       childNodes.add(TreeIndexNode(key, parentTree));
    //     }
    //     return _createTreeNodeByKey(childNodes.first, keys, currentIndex + 1, node);
    //   }
    //   this._nodes.forEach((_, messageNode) {
    //     messageNode.receiveMessageKeys.forEach((messageKey, messageDefinition) {
    //       final keys = messageKey.split(".").where((element) => element.isNotEmpty).toList();
    //       _createTreeNodeByKey(this._messageNodeReceiveKeysIndex, keys, 0, messageNode) as TreeDataNode;
    //     });
    //   });
    // }

    fun connect(node: MessageNode): MessageNodeConnectionTicket {
        val isApproved = this.approveMessageNode(node);
        if (!isApproved) {
            this._internalNode.dispatch(message = NodeChangedMessage(node.metaData, this.metaData, "message-dispatcher.node.connection-request-denied"));
            throw ConnectionDeniedException("Connection request of ${node.metaData} is denied by ${this.metaData}");
        }
        val connection = MessageNodeConnectionTicket(this, UUID.randomUUID().toString());
        node.connection = connection;
        this._nodes[connection.connectionId] = node;
        this._internalNode.dispatch(message = NodeChangedMessage(node.metaData, this.metaData, "message-dispatcher.node.connected"));
        node.onConnected();
        return connection;
    }

    fun dispatch(node: MessageNode , _message: RawMessage) {
        var message = _message
        val _sourceMessage = message;
        message = message.cloneSelf();
        message.sender = node.metaData;

        val result = message.definition.schema.validate("yourMessage", message.data);
        if (!result.passed) throw MessageValidationException("Message validation failed because your message schema should be '${result.message}'");
        val keySegments = message.messageKey.split(".").filter { it.isNotEmpty()}.toList()
        if (keySegments.any {it == "*" }) throw Exception("Message key cannot contain '*' matchers");
        val isBridgeNodeOrContainCorrectKey = node is BridgeNode || node.sendMessageKeys.containsKey(message.messageKey);
        if (!isBridgeNodeOrContainCorrectKey) throw Exception("Cannot dispatch your message because the message key '${message.messageKey}' is not registered in sendMessageKeys of ${node.metaData}, did you forget register it?");
        this._nodes.forEach {_, messageNode ->
            messageNode.receiveMessageKeys.forEach { key, _ ->
                val nodeKeySegments = key.split(".").filter { it.isNotEmpty() }.toList();
                if (keySegments.count() != nodeKeySegments.count()) return@forEach;
                for (i in 0 until keySegments.count()) {
                    if (nodeKeySegments[i] == "*") continue;
                    if (nodeKeySegments[i] == keySegments[i]) continue;
                    return@forEach;
                }
                try {
                    messageNode.handle(message);
                } catch (exception: Exception) {
                    if (message.sender!!.uuid == this._internalNode.metaData.uuid) throw exception
                    this._internalNode.dispatch(message = NodeErrorMessage(node.metaData, this.metaData, exception.toString(), exception.stackTraceToString(), "message-dispatcher.node.error"));
                }
            }
        }
    }

    fun disconnect(ticketId: String): Boolean {
        val node = this._nodes[ticketId];
        val value = this._nodes.remove(ticketId) != null;
        if (value) {
            this._internalNode.dispatch(message = NodeChangedMessage(node!!.metaData, this.metaData, "message-dispatcher.node.disconnected"));
            node!!.onDisconnected();
        }
        return value;
    }
    fun approveMessageNode(messageNode: MessageNode): Boolean {
        return true;
    }
}

class _DispatcherInternalNode(metaData: MetaData) : MessageNode(metaData, mapOf(
    "message-dispatcher.node.connected" to NodeChangedMessage.DEFINITION,
    "message-dispatcher.node.disconnected" to NodeChangedMessage.DEFINITION,
    "message-dispatcher.node.connection-request-denied" to NodeChangedMessage.DEFINITION,
    "message-dispatcher.node.error" to NodeErrorMessage.DEFINITION,
), mapOf(
    "message-dispatcher.node.connected" to NodeChangedMessage.DEFINITION,
    "message-dispatcher.node.disconnected" to NodeChangedMessage.DEFINITION,
    "message-dispatcher.node.connection-request-denied" to NodeChangedMessage.DEFINITION,
    "message-dispatcher.node.error" to NodeErrorMessage.DEFINITION,
)) {
    override fun handle(message: RawMessage) {
        if (message.sender!!.uuid == this.metaData.uuid) return;
        if (message.messageKey == "message-dispatcher.node.error") {
            val error = NodeErrorMessage.fromRawMessage(message);
            print("Node [${error.node}] that connected to [${error.dispatcher}] has an exception, the exception is ${error.errorMessage} and occurred here: ${error.stackTrace}]");
        }
    }
}

fun Map<String, Any>.deepClone(): Map<String, Any> {
    val _map = HashMap<String, Any>()
    this.forEach { key, value ->
        if (value is Collection<*>) {
            _map[key] = (value as Collection<Any>).deepClone();
            return@forEach;
        }
        if (value is Map<*, *>) {
            _map[key.toString()] = (value as Map<String, Any>).deepClone();
            return@forEach;
        }
        _map[key.toString()] = value;
    }
    return _map;
}

fun Collection<Any>.deepClone(): Array<Any> {
    val _list = ArrayList<Any>()
    this.forEach { element ->
        if (element is Collection<*>) {
            _list.add((element as Collection<Any>).deepClone());
            return@forEach;
        }
        if (element is Map<*, *>) {
            _list.add((element as Map<String, Any>).deepClone());
        }
        _list.add(element);
    }
    return _list.toTypedArray()
}
