package org.enodeframework.infrastructure.impl

import java.util.stream.Collectors

/**
 * @author anruence@gmail.com
 */
class ManyType(types: List<Class<*>>) {
    val types: List<Class<*>>

    override fun hashCode(): Int {
        return types.stream().map { obj: Class<*> -> obj.hashCode() }.reduce { x: Int, y: Int -> x xor y }.orElse(1)
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (other == null) {
            return false
        }
        if (other !is ManyType) {
            return false
        }
        return if (types.size != other.types.size) {
            false
        } else types.stream().allMatch { type: Class<*> -> other.types.stream().anyMatch { x: Class<*> -> x == type } }
                && other.types.stream()
            .allMatch { type: Class<*> -> types.stream().anyMatch { x: Class<*> -> x == type } }
    }

    init {
        require(HashSet(types).size == types.size) {
            String.format("Invalid ManyType: %s", types.stream().map { obj: Class<*> -> obj.name }
                .collect(Collectors.joining("|")))
        }
        this.types = types
    }
}