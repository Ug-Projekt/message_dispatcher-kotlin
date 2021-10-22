import SharedData.*
import kotlin.test.Test

class SharedDataTest {
    @Test
    fun main_test() {
        test("Shared data test") {
            ///Create three dispatcher
            ///Don't confuse there has two class named with same name "_TestMessageDispatcherA", "_TestMessageDispatcherB", "_TestMessageDispatcherC"
            ///They are different with each other.
            val dispatcherA = _TestMessageDispatcherA();
            val dispatcherB = _TestMessageDispatcherB();
            val dispatcherC = _TestMessageDispatcherC();

            ///And create their bridges
            val bridgeA = _BridgeA();
            val bridgeB = _BridgeB();
            val bridgeC = _BridgeC();
            val bridgeD = _BridgeD();

            ///and connect its dispatchers
            dispatcherA.connect(bridgeA);
            dispatcherB.connect(bridgeB);
            dispatcherB.connect(bridgeC);
            dispatcherC.connect(bridgeD);

            ///connect together
            bridgeA.b = bridgeB;
            bridgeB.a = bridgeA;
            bridgeC.d = bridgeD;
            bridgeD.c = bridgeC;

            val dataNodeA = DataNodeA();
            val dataNodeB = DataNodeB();
            val dataNodeC = DataNodeC();
            val dataNodeD = DataNodeD();
            dispatcherA.connect(dataNodeA);
            dispatcherB.connect(dataNodeB);
            dispatcherC.connect(dataNodeC);

            dataNodeA.applyChanges {
                it["name"] = "Hello";
            }
            expect(dataNodeC.data["name"], "Hello");
            expect(dataNodeB.data["name"], "Hello");
            dataNodeB.apply {
                applyChanges {
                    data["name"] = "changed"
                }
            }
            expect(dataNodeC.data["name"], "changed");
            expect(dataNodeA.data["name"], "changed");
            val connectionD = dispatcherA.connect(dataNodeD);
            expect(dataNodeD.data["name"], "changed");
            connectionD.disconnect();
            dataNodeC.apply {
                applyChanges {
                    data["name"] = "Changed to C"
                };
            }
            dispatcherB.connect(dataNodeD);
            expect(dataNodeD.data["name"], "Changed to C");
        };
    }
}
