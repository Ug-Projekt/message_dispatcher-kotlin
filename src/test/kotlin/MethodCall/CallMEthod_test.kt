import kotlin.test.Test

class _MyDispatcher : MessageDispatcher(MetaData(uuid = "47753420-42cd-11ec-b0e3-5ffe43506a83", name = "MyDispatcher", author = "Dream-LAb software technologies")) {}

class CallMEthod_test {
  @Test
  fun main() {
    val dispatcher = _MyDispatcher();

    group("Test method call") {
      val methodCallerA = MethodCallBridge("MyCallBridge", MetaData(uuid = "6b644eb6-42cd-11ec-a581-cb31a87005eb", name = "MyBridge", author = "Dream-Lab software technologies"), mapOf(
        "hello" to { args, setResult ->
          this.callMethod("hi", mapOf("message" to args["helloMessage"]!!)) { error, result ->
            setResult(mapOf(
              "result" to result!!
            ))
          }
        }
      ))
      val methodCallerB = MethodCallBridge("MyCallBridge", MetaData(uuid = "8d72f73c-42cd-11ec-ac43-3bf7b79a3bf8", name = "MyBridge", author = "Dream-Lab software technologies"), mapOf(
        "hi" to {args, setResult ->
          setResult(mapOf(
            "result" to "yes",
            "replayMessage" to "Hi ${args["message"]}"
          ))
        }
      ))

      dispatcher.connect(methodCallerB);
      dispatcher.connect(methodCallerA);
      var counter = 0;
      test("Main test") {
        val result = methodCallerB.callMethod("hello", mapOf("helloMessage" to "Dream-Lab")) { error, result ->
          expect((result!!["result"] as Map<String, Any>)["result"]!!, "yes");
          expect((result["result"] as Map<String, Any>)["replayMessage"]!!, "Hi Dream-Lab");
          counter++
        };
      };
      expect(counter, 1)
    };
  }
}

