import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.expect

/**
 * @author Dream-Lab software technologies muhtarjan mahmood(مۇختەرجان مەخمۇت)
 * @email ug-project@outlook.com
 * @create date 2021-10-12 15:41:53
 * @modify date 2021-10-12 15:41:53
 * @desc [description]
 */


class DeepClone_test {
  @Test
  fun deepCloneTest() {
    test("Deep clone => ") {
      test("Map and list object") {
        val mapA = mapOf(
          "address" to mutableMapOf(
            "name" to "Urumqi"
          ),
          "name" to "Ug-Project",
          "researchs" to mutableListOf<Any>(
            "Linux epoll",
            "V-Lang"
          )
        ).toMutableMap()
        val mapB = mapA.deepClone() as HashMap<String, Any>
        (mapA["address"] as MutableMap<String, Any>)["name"] = "Kashgar";
        mapA["name"] = "Dream-Lab";
        (mapA["researchs"] as MutableList<Any>).add("Unreal engine");
        (mapA["researchs"] as MutableList<Any>).add("Robotics automation");
        expect("Ug-Project") { mapB["name"] }
        expect("Urumqi") { (mapB["address"] as Map<String, Any>)["name"] }
        assertNotEquals(mapA["name"], mapB["name"])
        assertNotEquals((mapA["address"] as Map<String, Any>)["name"], (mapB["address"] as Map<String, Any>)["name"]);
        assertNotEquals((mapA["researchs"] as ArrayList<Any>).size, (mapB["researchs"] as Array<Any>).size);
        println("ha ha ha 😁")
      }
    };
  }
}

