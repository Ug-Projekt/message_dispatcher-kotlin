

fun main(args: Array<String>) {
    val arrayOfString = arrayOf("Hello", "World")
    val arrayOfObject: Array<Any> = arrayOfString as Array<Any>
    val sourceArray = arrayOfObject as Array<String>
    println("Hello World!, value is: ${sourceArray.joinToString()}")
    // Try adding program arguments at Run/Debug configuration
    println("Program arguments: ${args.joinToString()}")
}

//fun test() {
//    val dispatcher = _TestMessageDispatcherA();
//    val nodeA = NodeA();
//    val nodeB = NodeB();
//    val nodeC = NodeC();
//    dispatcher.connect(nodeA);
//    dispatcher.connect(nodeB);
//    dispatcher.connect(nodeC);
//    _simpleTest(nodeA, nodeB, nodeC);
//}