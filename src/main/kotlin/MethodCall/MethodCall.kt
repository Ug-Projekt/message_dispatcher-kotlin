import Schema.CustomObjectSchema
import Schema.StringSchema
import java.lang.Exception


class MethodCallRequest(messageKey: String, val methodName: String, val arguments: Map<String, Any>) : RawMessage(messageKey, definition = DEFINITION) {
    companion object {
        val DEFINITION = MessageDefinition(name = "Method Call Request", version = 0.1, schema = CustomObjectSchema(false, mapOf(
            "functionName" to StringSchema(false, 1, 255),
            "arguments" to CustomObjectSchema(false, mapOf()),
        )))
        fun fromMessage(message: RawMessage): MethodCallRequest {
            return MethodCallRequest(message.messageKey, methodName = message.data["functionName"] as String, arguments = message.data["arguments"] as Map<String, Any>);
        }
    }
    init {
        this.data["functionName"] = this.methodName;
        this.data["arguments"] = this.arguments;
    }
}
class MethodCallResponse(messageKey: String, val requestId: String, val methodName: String, val arguments: Map<String, Any>, val returns: Map<String, Any>?, val error: Any?) : RawMessage(messageKey, DEFINITION) {
    companion object {
        val DEFINITION = MessageDefinition(name = "Method Call Response", version = 0.1, schema = CustomObjectSchema(false, mapOf(
            "requestId" to StringSchema(false, 1, 64),
            "functionName" to StringSchema(false, 1, 255),
            "arguments" to CustomObjectSchema(false, mapOf()),
            "returns" to CustomObjectSchema(true, mapOf()),
            "error" to CustomObjectSchema(true, mapOf())
        )))
        fun fromMessage(message: RawMessage): MethodCallResponse {
            return MethodCallResponse(message.messageKey, requestId = message.data["requestId"] as String, methodName = message.data["functionName"] as String, arguments = message.data["arguments"] as Map<String, Any>, returns = message.data["returns"] as Map<String, Any>?, error = message.data["error"]);
        }
    }
    init {
        this.data["functionName"] = this.methodName;
        this.data["arguments"] = this.arguments;
        if (this.returns != null) this.data["returns"] = this.returns;
        this.data["requestId"] = this.requestId;
        if (this.error != null) this.data["error"] = this.error;
    }
}

//typealias MethodCallHandler = Future<Map<String, dynamic>> Function(MethodCallBridge bridge, Map<String, dynamic> args);
typealias MethodCallHandler = MethodCallBridge.(argument: Map<String, Any>, setResult: (result: Map<String, Any>) -> Unit) -> Unit
class MethodCallBridge(val key: String, metaData: MetaData, handlers: Map<String, MethodCallHandler>) : MessageNode(metaData, mapOf(
    "MethodCall.${key}.Request" to MethodCallRequest.DEFINITION,
    "MethodCall.${key}.Response" to MethodCallResponse.DEFINITION,
), mapOf(
    "MethodCall.${key}.Request" to MethodCallRequest.DEFINITION,
    "MethodCall.${key}.Response" to MethodCallResponse.DEFINITION,
)) {
    val _handlers: MutableMap<String, MethodCallHandler> = mutableMapOf();
    val _request: MutableMap<String, (error: Any?, argument: Map<String, Any>?) -> Unit> = mutableMapOf();

    init {
        this._handlers.putAll(handlers);
    }

    override fun handle(message: RawMessage) {
        if (message.sender?.uuid == this.metaData.uuid) return;
        val responseKey = "MethodCall.${key}.Response";
        val requestKey = "MethodCall.${key}.Request";
        if (message.messageKey == requestKey) {
            val request = MethodCallRequest.fromMessage(message);
            val handler = this._handlers[request.methodName]
            try {
                handler?.invoke(this, request.arguments) { value ->
                    this.dispatch(
                        message = MethodCallResponse(
                            responseKey,
                            requestId = message.id!!,
                            methodName = request.methodName,
                            arguments = request.arguments,
                            returns = value,
                            error = null
                        )
                    );
                }
            } catch (exception: Exception) {
                this.dispatch(
                    message = MethodCallResponse(
                        responseKey,
                        requestId = message.id!!,
                        methodName = request.methodName,
                        arguments = request.arguments,
                        returns = null,
                        error = mapOf("error" to exception.message, "stackTrace" to exception.stackTraceToString())
                    )
                );
            }
            //Because the kotlin is only one side, another side is dart, web, mybe this method not exists on kotlin side but it may exists dart side so we cannot throw an exception if handler is not found.
//            if (handler == null) this.dispatch(
//                message = MethodCallResponse(
//                    responseKey,
//                    requestId = message.id!!,
//                    methodName = request.methodName,
//                    arguments = request.arguments,
//                    returns = null,
//                    error = mapOf("error" to "handler ${request.methodName} is not found")
//                )
//            );
            return;
        }
        if (message.messageKey == responseKey) {
            val response = MethodCallResponse.fromMessage(message);
            val request = this._request[response.requestId];
            if (request == null) {
                println("Request ${response.requestId} is not found, maybe it is already timeout");
                return;
            }
            if (response.error != null) request(response.error, null);
            else request(null, response.returns);
            _request.remove(response.requestId);
            return;
        }
    }

    fun callMethod(name: String, argument: Map<String, Any>, action: (error: Any?, args: Map<String, Any>?) -> Unit) {
//    timeout ??= Duration(seconds: 10);
        val message = MethodCallRequest("MethodCall.${this.key}.Request", methodName = name, arguments = argument);
        this._request[message.id!!] = action
        this.dispatch(message = message);
    }
}
