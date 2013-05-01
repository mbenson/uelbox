/*
 *  Copyright the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package therian;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Comparator;
import java.util.Map;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.TypeUtils;
import therian.util.Types;

/**
 * Utility methods for Operators.
 */
public class Operators {

    private static final Comparator<Operator<?>> COMPARATOR = new Comparator<Operator<?>>() {
        private final TypeVariable<?> opVar = Operator.class.getTypeParameters()[0];

        @Override
        public int compare(Operator<?> o1, Operator<?> o2) {
            final Type opType1 =
                    Types.unrollVariables(TypeUtils.getTypeArguments(o1.getClass(), Operator.class), opVar);
            final Type opType2 =
                    Types.unrollVariables(TypeUtils.getTypeArguments(o2.getClass(), Operator.class), opVar);

            return compareTypes(opType1, opType2);
        }

        private int compareTypes(Type t1, Type t2) {
            if (ObjectUtils.equals(t1, t2)) {
                return 0;
            }
            if (TypeUtils.isAssignable(t1, t2)) {
                return -1;
            }
            if (TypeUtils.isAssignable(t2, t1)) {
                return 1;
            }
            final Class<?> raw1 = raw(t1);
            final Class<?> raw2 = raw(t2);

            if (ObjectUtils.equals(raw1, raw2)) {
                if (raw1.getTypeParameters().length == 0) {
                    return 0;
                }
                final Map<TypeVariable<?>, Type> typeArgs1 = TypeUtils.getTypeArguments(t1, raw1);
                final Map<TypeVariable<?>, Type> typeArgs2 = TypeUtils.getTypeArguments(t2, raw2);
                for (TypeVariable<?> var : raw1.getTypeParameters()) {
                    final int recurse =
                            compareTypes(Types.unrollVariables(typeArgs1, var), Types.unrollVariables(typeArgs2, var));
                    if (recurse != 0) {
                        return recurse;
                    }
                }
                return 0;
            }
            return t1.toString().compareTo(t2.toString());
        }

        private Class<?> raw(Type type) {
            if (type instanceof WildcardType) {
                final Type upper = TypeUtils.getImplicitUpperBounds((WildcardType) type)[0];
                return upper instanceof Class<?> ? (Class<?>) upper : raw(upper);
            }
            return TypeUtils.getRawType(type, null);
        }

    };

    /**
     * Get standard operators.
     * 
     * @return Operator[]
     */
    public static Operator<?>[] standard() {
        throw new UnsupportedOperationException("should use therian-build-weaver");
    }

    /**
     * Validate an {@link Operator} implementation.
     * 
     * @param operator
     * @param <OPERATOR>
     * @return {@code operator}
     * @throws OperatorDefinitionException on invalid operator
     */
    public static <OPERATOR extends Operator<?>> OPERATOR validateImplementation(OPERATOR operator) {
        for (TypeVariable<?> var : Validate.notNull(operator, "operator").getClass().getTypeParameters()) {
            if (Types.resolveAt(operator, var) == null) {
                throw new OperatorDefinitionException(operator, "Could not resolve %s.%s against operator %s",
                    var.getGenericDeclaration(), var.getName(), operator);
            }
        }
        return operator;
    }

    /**
     * Get a comparator that compares {@link Operator}s by {@link Operation} type/type parameter assignability.
     * 
     * @return a Comparator that does not handle {@code null} values
     */
    public static Comparator<Operator<?>> comparator() {
        return COMPARATOR;
    }
}
