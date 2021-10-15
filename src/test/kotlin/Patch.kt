import kotlin.test.assertEquals


fun group(name: String, action: () -> Unit) = action()
fun test(name: String, action: () -> Unit) = action()
fun <T> expect(actual: T, expected: T, reason: String? = null) = assertEquals(expected, actual, message = reason)
