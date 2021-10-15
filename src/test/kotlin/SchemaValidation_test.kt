import Schema.*
import kotlin.test.Test

/**
 * @author Dream-Lab software technologies muhtarjan mahmood(مۇختەرجان مەخمۇت)
 * @email ug-project@outlook.com
 * @create date 2021-10-08 10:38:50
 * @modify date 2021-10-08 10:38:50
 * @desc [description]
 */

class SchemaValidation_test {
    @Test
    fun test() {
        group("String validation") {
            test("length validate") {
                val _string1 = StringSchema(false, 3, 5);
                val r0 = _string1.validate("", "adbc");
                expect(r0.passed, true, reason = r0.message);
                expect(_string1.validate("", "abcd").passed, true);
                expect(_string1.validate("", "acde").passed, true);
                expect(_string1.validate("", null).passed, false);
                expect(_string1.validate("", "").passed, false);
                expect(_string1.validate("", "abcdefg").passed, false);
            };
            test("null validate") {
                val _string2 = StringSchema(true, 3, 5);
                expect(_string2.validate("", null).passed, true);
                expect(_string2.validate("", "1").passed, false);
            };
        };
        group("integer test") {
            val _integerSchema1 = IntegerSchema(false, 10, 5);
            val _integerSchema2 = IntegerSchema(true, 10, 5);
            test("range test") {
                expect(_integerSchema1.validate("", 5).passed, true);
                expect(_integerSchema1.validate("", 10).passed, true);
                expect(_integerSchema1.validate("", 4).passed, false);
                expect(_integerSchema1.validate("", 11).passed, false);
                expect(_integerSchema1.validate("", null).passed, false);
            };
            test("null test") {
                expect(_integerSchema2.validate("", null).passed, true);
            };
        };
        group("double test") {
            val _integerSchema1 = DoubleSchema(false, 10.0, 5.0);
            val _integerSchema2 = DoubleSchema(true, 10.0, 5.0);
            test("range test") {
                val r0 = _integerSchema1.validate("", 5);
                expect(r0.passed, true, reason = r0.message);
                expect(_integerSchema1.validate("", 10).passed, true);
                expect(_integerSchema1.validate("", 4).passed, false);
                expect(_integerSchema1.validate("", 11).passed, false);
                expect(_integerSchema1.validate("", null).passed, false);
            };
            test("null test") {
                expect(_integerSchema2.validate("", null).passed, true);
            };
        };
        group("boolean") {
            val _boolean1 = BooleanSchema(false);
            val _boolean2 = BooleanSchema(true);
            test("valid value test") {
                expect(_boolean1.validate("", 10).passed, false);
                expect(_boolean1.validate("", "10").passed, false);
                expect(_boolean1.validate("", true).passed, true);
                expect(_boolean1.validate("", false).passed, true);
            };
            test("nullable test") {
                val r0 = _boolean2.validate("value", null);
                expect(r0.passed, true, reason = r0.message);
            };
        };
        group("EnumTest") {
            test("enum of integer") {
                val validValues = arrayOf(10, 20, 30);
                val invalidValues = arrayOf(11, 21, 31);
                val _enum = EnumSchema(false, IntegerSchema(false, 150, 0), validValues as Array<Any>);
                validValues.forEach { element ->
                    expect(_enum.validate("", element).passed, true);
                };
                invalidValues.forEach { element ->
                    expect(_enum.validate("", element).passed, false);
                };
                expect(_enum.validate("", null).passed, false);
            };
            test("enum of string") {
                val validValues = arrayOf("A", "B", "C");
                val _enum = EnumSchema(false, StringSchema(false, 1, 255), validValues as Array<Any>);
                validValues.forEach { element ->
                    expect(_enum.validate("", element).passed, true);
                };
                arrayOf("E", "F", "G", null).forEach { element ->
                    expect(_enum.validate("", element).passed, false);
                };
            };
        };
        group("Array test") {
            val _array1 = ArraySchema(false, StringSchema(false, 1, 5), 1, 3);
            val _array2 = ArraySchema(false, StringSchema(true, 1, 5), 1, 3);
            test("range test") {
                expect(_array1.validate("", arrayOf("A", "B")).passed, true);
                expect(_array1.validate("", arrayOf<Any>()).passed, false);
                expect(_array1.validate("", arrayOf("A")).passed, true);
                expect(_array1.validate("", arrayOf("A", "B", "C", "D")).passed, false);
                expect(_array1.validate("", arrayOf(0)).passed, false);
                expect(_array1.validate("", arrayOf<Any?>(null)).passed, false);
                expect(_array1.validate("", arrayOf("AA", true, 0)).passed, false);
                expect(_array1.validate("", arrayOf("")).passed, false);
                expect(_array1.validate("", arrayOf("123456")).passed, false);
                expect(_array1.validate("", arrayOf("12345", null)).passed, false);
            };
            test("null test") {
                expect(_array2.validate("", arrayOf<Any?>(null)).passed, true);
                expect(_array2.validate("", arrayOf("")).passed, false);
                expect(_array2.validate("", arrayOf("ABC")).passed, true);
            };
        };
        group("Object test") {
            val _person = CustomObjectSchema(
                false, mutableMapOf(
                    "name" to StringSchema(false, 1, 5),
                    "level" to IntegerSchema(false, 5, 0),
                    "gender" to EnumSchema(false, StringSchema(false, 0, 255), arrayOf("None", "Man", "Women")),
                    "married" to BooleanSchema(false),
                    "money" to DoubleSchema(false, 5.0, 0.0),
                )
            );
            (_person.properties as MutableMap<String, ObjectSchema>)["girlFriends"] = ArraySchema(false, _person, 0, 3);

            test("property validation") {
                val obj1 = mapOf(
                    "name" to "Abdu",
                    "level" to 3,
                    "gender" to "Man",
                    "married" to false,
                    "money" to 0.0,
                    "girlFriends" to arrayOf(
                        mapOf(
                            "name" to "AAA",
                            "level" to 1,
                            "gender" to "Women",
                            "married" to false,
                            "money" to 3.0,
                            "girlFriends" to arrayOf(
                                mapOf(
                                    "name" to "HHH",
                                    "level" to 1,
                                    "gender" to "Women",
                                    "married" to false,
                                    "money" to 3.0,
                                    "girlFriends" to arrayOf<Any>()
                                ),
                            )
                        ),
                        mapOf(
                            "name" to "BBB",
                            "level" to 2,
                            "gender" to "Women",
                            "married" to false,
                            "money" to 2,
                            "girlFriends" to arrayOf<Any>()
                        )
                    )
                );
                val result = _person.validate("obj", obj1);
                expect(result.passed, true, reason = result.message);
            }
            test("Empty object test") {
                val r0 = _person.validate(
                    "obj", mapOf(
                        "name" to "hello"
                    )
                );
                expect(r0.passed, false, reason = "expected: '${r0.message}'");
            }
            test("enum in object validation test") {
                val v0 = CustomObjectSchema(
                    false, mapOf(
                        "gender" to EnumSchema(false, StringSchema(false, 1, 10), arrayOf("Male", "Female", "None"))
                    )
                );
                val r0 = v0.validate("obj", {
                    "gender" to "-"
                });
                expect(r0.passed, false, reason = r0.message);
            };
        };
        println("Ha ha ha, my unit tests are passed correctly😁😃")
    }
}


