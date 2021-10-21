package Schema;

/**
 * @author Dream-Lab software technologies muhtarjan mahmood(مۇختەرجان مەخمۇت)
 * @email ug-project@outlook.com
 * @create date 2021-10-06 16:43:28
 * @modify date 2021-10-06 16:43:28
 * @desc [description]
 */

enum class DataType {
  stringType,
  arrayType,
  numberType,
  booleanType,
  customObjectType,
  enumType,
}

/**
abstract class Constraint {}
abstract class LogicUnit extends Constraint {}
//Logic
class AndLogic extends LogicUnit {
  final List<Constraint> values;
  AndLogic(this.values);
}
class OrLogic extends LogicUnit {
  final List<Constraint> values;
  OrLogic(this.values);
}
class NotLogic extends LogicUnit {
  final Constraint value;
  NotLogic(this.value);
}
//Numbers
class NumberConstraint extends Constraint {}
class EqualConstraint extends NumberConstraint {
  final double value;
  EqualConstraint(this.value);
}
class GreaterConstraint extends NumberConstraint {
  final double value;
  GreaterConstraint(this.value);
}
class LessConstraint extends NumberConstraint {
  final double value;
  LessConstraint(this.value);
}
class InConstraint extends NumberConstraint {
  final List<double> values;
  InConstraint(this.values);
}
class BetweenConstraint extends NumberConstraint {
  final double first;
  final double last;
  BetweenConstraint(this.first, this.last);
}
 */
data class ValidationResult(val passed: Boolean, val message: String? = null) {
  companion object {
    fun passed() = ValidationResult(true)
    fun error(message: String) = ValidationResult(false, message)
  }
}

abstract class ObjectSchema(var type: DataType, var nullable: Boolean, var description: String? = null) {
  abstract fun validate(propertyName: String, value: Any?): ValidationResult
}

class IntegerSchema(nullable: Boolean, val maximumValue: Int, val minimumValue: Int, description: String? = null) : ObjectSchema(DataType.numberType, nullable, description) {
  override fun validate(propertyName: String, value: Any?): ValidationResult {
    val isRightType = value is Int || value is Int?
    if (!isRightType) return ValidationResult.error("${propertyName}.runtimeType == ${if (this.nullable) "int?" else "int"}");
    if (this.nullable && value == null) return ValidationResult.passed();
    if (!this.nullable && value == null) return ValidationResult.error("$propertyName != null");
    val isInRange = value as Int <= maximumValue && value >= minimumValue;
    if (!isInRange) return ValidationResult.error("$propertyName >= ${this.minimumValue} && $propertyName <= ${this.maximumValue}");
    return ValidationResult.passed();
  }
  // @override
  // Map<String, dynamic> serialize() => {
  //   "type": this.type.toString(),
  //   "minimumValue": this.minimumValue,
  //   "maximumValue": this.maximumValue,
  // };
}

class DoubleSchema(nullable: Boolean, val maximumValue: Double, val minimumValue: Double, description: String? = null) : ObjectSchema(DataType.numberType, nullable, description) {
//  final double minimumValue;
//  final double maximumValue;
//  DoubleSchema(bool nullable, this.maximumValue, this.minimumValue, {String? description}) : super(DataType.numberType, nullable, description: description);

  override fun validate(propertyName: String, value: Any?) : ValidationResult {
    val isRightType = value is Number || value is Number?;
    if (!isRightType) return ValidationResult.error("${propertyName}.runtimeType == ${if (this.nullable) "double?|int?" else "double|int"}");
    if (this.nullable && value == null) return ValidationResult.passed();
    if (!this.nullable && value == null) return ValidationResult.error("$propertyName != null");
    val isInRange = (value as Number).toDouble() in minimumValue..maximumValue;
    if (!isInRange) return ValidationResult.error("$propertyName >= ${this.minimumValue} && $propertyName <= ${this.maximumValue}");
    return ValidationResult.passed();
  }
}
class BooleanSchema(nullable: Boolean, description: String? = null) : ObjectSchema(DataType.booleanType, nullable, description) {
  override fun validate (propertyName: String, value: Any?): ValidationResult {
    if (!(value is Boolean || value is Boolean?)) return ValidationResult.error("$propertyName.runtimeType == ${if (this.nullable) "bool?" else "bool"}");
    if (!this.nullable && value == null) return ValidationResult.error("$propertyName != null");
    return ValidationResult.passed();
  }
}

class StringSchema(nullable: Boolean, val minimumLength: Int, val maximumLength: Int, description: String? = null) : ObjectSchema(DataType.stringType, nullable, description) {
  override fun validate(propertyName: String, value: Any?): ValidationResult {
    val isValidType = value is String || value is String?
    if (!isValidType) return ValidationResult.error("${propertyName}.runtimeType == ${if (this.nullable) "String?" else "String"}");
    if (this.nullable && value == null) return ValidationResult.passed();
    if (this.nullable == false && value == null) return ValidationResult.error("$propertyName != null");
    val isValidLength = (value as String).length <= maximumLength && value.length >= minimumLength;
    if (!isValidLength) return ValidationResult.error("$propertyName.length <= ${this.maximumLength} && $propertyName.length >= ${this.minimumLength}");
    return ValidationResult.passed();
  }
}

class ArraySchema(nullable: Boolean, val childrenSchema: ObjectSchema, val minimumItemsCount: Int, val maximumItemsCount: Int, description: String? = null) : ObjectSchema(DataType.arrayType, nullable, description) {

  override fun validate(propertyName: String, value: Any?): ValidationResult{
    val isList = value is Array<*> || value is Array<*>?;
    if (!isList) return ValidationResult.error("$propertyName.runtimeType == ${if (this.nullable) "List?" else "List"}");
    if (value == null && this.nullable == false) return ValidationResult.error("$propertyName != null");
    if (this.nullable && value == null) return ValidationResult.passed();
    val isValidLength = (value as Array<*>).count() in this.minimumItemsCount..this.maximumItemsCount;
    if (!isValidLength) return ValidationResult.error("$propertyName.length >= ${this.minimumItemsCount} && $propertyName.length <= ${this.maximumItemsCount}");
    var result: ValidationResult? = null
    var index = 0;
    for (item in value) {
      result = this.childrenSchema.validate("$propertyName[$index]", item);
      index++;
      if (!result.passed) break;
    }
    result = result ?: ValidationResult.passed()
    return result;
  }
}

class CustomObjectSchema(nullable: Boolean, val properties: Map<String, ObjectSchema>, description: String? = null) : ObjectSchema(DataType.customObjectType, nullable, description) {
  override fun validate(propertyName: String, value: Any?): ValidationResult {
    val isMap = value is Map<*, *> || value is Map<*, *>?;
    if (!isMap) return ValidationResult.error("$propertyName.runtimeType == ${if (this.nullable) "Map<String, dynamic>?" else "Map<String, dynamic>"} ");
    if (this.nullable == false && value == null) return ValidationResult.error("$propertyName != null");
    val map = value as Map<*, *>;
    var result: ValidationResult? = null
    for (key in this.properties.keys) {
      val property = this.properties[key]
      result = property!!.validate("$propertyName.$key", map[key]);
      if (!result.passed) break;
    }
    result = result ?: ValidationResult.passed();
    return result;
  }
}

class EnumSchema(nullable: Boolean, val itemSchema: ObjectSchema, val values: Array<Any>, description: String? = null) : ObjectSchema(DataType.enumType, nullable, description) {
  init {
    values.forEach {
      assert(itemSchema.validate("enum-schema-constructor", it).passed)
    }
  }
  override fun validate(propertyName: String, value: Any?): ValidationResult {
    return if (values.any{ it == value}) ValidationResult.passed() else ValidationResult.error("(${propertyName}/* valid values are: ${this.values.joinToString()}*/).shouldContain($value)");
  }
}

