import MessageNode.*
import kotlin.test.Test
import kotlin.test.assertFails

class MessageNodeTest {
  @Test
  fun node_test() {
      group("Single message dispatcher test => ") {
          val dispatcher = _TestMessageDispatcherA();
          val nodeA = NodeA();
          val nodeB = NodeB();
          val nodeC = NodeC();
          dispatcher.connect(nodeA);
          dispatcher.connect(nodeB);
          dispatcher.connect(nodeC);
          _simpleTest(nodeA, nodeB, nodeC);
      };

      group("Two message dispatcher test => ") {
          val dispatcherA = _TestMessageDispatcherA();
          val dispatcherB = _TestMessageDispatcherB();
          val bridgeA = _BridgeA();
          val bridgeB = _BridgeB();
          dispatcherA.connect(bridgeA);
          dispatcherB.connect(bridgeB);
          bridgeA.b = bridgeB;
          bridgeB.a = bridgeA;

          val nodeA = NodeA();
          val nodeB = NodeB();
          val nodeC = NodeC();
          dispatcherA.connect(nodeA);
          dispatcherA.connect(nodeB);
          dispatcherB.connect(nodeC);
          _simpleTest(nodeA, nodeB, nodeC);
      };
      group("Three message dispatcher test => ") {
          ///Create threee dispatcher
          val dispatcherA = _TestMessageDispatcherA();
          val dispatcherB = _TestMessageDispatcherB();
          val dispatcherC = _TestMessageDispatcherC();

          ///And create their bridges
          val bridgeA = _BridgeA();
          val bridgeB = _BridgeB();
          val bridgeC = _BridgeC();
          val bridgeD = _BridgeD();

          ///and connect it's dispatchers
          dispatcherA.connect(bridgeA);
          dispatcherB.connect(bridgeB);
          dispatcherB.connect(bridgeC);
          dispatcherC.connect(bridgeD);

          ///connect together
          bridgeA.b = bridgeB;
          bridgeB.a = bridgeA;
          bridgeC.d = bridgeD;
          bridgeD.c = bridgeC;

          val nodeA = NodeA();
          val nodeB = NodeB();
          val nodeC = NodeC();

          dispatcherA.connect(nodeA);
          dispatcherB.connect(nodeB);
          dispatcherC.connect(nodeC);

          _simpleTest(nodeA, nodeB, nodeC);
      };
  }

  fun _simpleTest(nodeA: NodeA, nodeB: NodeB, nodeC: NodeC) {
      test("should throw exception with unregistered message key") {
          _testWithEXceptions(nodeA, nodeB, nodeC);
      };
      test("single message dispatcher delivery correctness") {
          _testWithDeliveryCorrectness(nodeA, nodeB, nodeC);
      };
  }

  fun _testWithDeliveryCorrectness(nodeA: NodeA, nodeB: NodeB, nodeC: NodeC) {
    var countA = 0;
    var countB = 0;
    var countC = 0;

    var valueA = -1;
    var valueB = -1;
    var valueC = -1;

      expect(nodeA.value, -1);
      expect(nodeB.value, -1);
      expect(nodeC.value, -1);

    for (a in 1..1000) {
        nodeA.dispatch(message = _TestMessage("xxx.yyy.aaa", a));
        expect(nodeA.counter, countA);
        expect(nodeA.value, valueA);
        countB++;
        valueB = a;
        expect(nodeB.counter, countB);
        expect(nodeB.value, valueB);
        countC++;
        valueC = a;
        expect(nodeC.counter, countC);
        expect(nodeC.value, valueC);

        nodeB.dispatch(message = _TestMessage("xxx.yyy.aaa", a + 1));
        valueA = a + 1;
        countA++;
        expect(nodeA.counter, countA);
        expect(nodeA.value, valueA);
        expect(nodeB.counter, countB);
        expect(nodeB.value, valueB);
        countC++;
        valueC = a + 1;
        expect(nodeC.counter, countC);
        expect(nodeC.value, valueC);

        nodeC.dispatch(message = _TestMessage("xxx.yyy.aaa", a + 2));
        countA++;
        valueA = a + 2;
        expect(nodeA.counter, countA);
        expect(nodeA.value, valueA);
        countB++;
        valueB = a + 2;
        expect(nodeB.counter, countB);
        expect(nodeB.value, valueB);
        countC++;
        valueC = a + 2;
        expect(nodeC.counter, countC);
        expect(nodeC.value, valueC);

        nodeC.dispatch(message = _TestMessage("xxx.yyy.ccc", a + 3));
        expect(nodeA.counter, countA);
        expect(nodeA.value, valueA);
        expect(nodeB.counter, countB);
        expect(nodeB.value, valueB);
        countC++;
        valueC = a + 3;
        expect(nodeC.counter, countC);
        expect(nodeC.value, valueC);
    }

    println("ha ha ha 😁 my unit test is passed.")
  }

  fun _testWithEXceptions(nodeA: NodeA, nodeB: NodeB, nodeC: NodeC) {
      assertFails {
          nodeA.dispatch(message = _TestMessage("xxx.yyy.zzz", 99))
      }
      assertFails {
          nodeB.dispatch(message = _TestMessage("xxx.*.zzz", 99))
      }
      assertFails {
          nodeC.dispatch(message = _TestMessage("xxx..zzz", 99))
      }
  }
}