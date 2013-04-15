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

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.TypeUtils;

import therian.util.Types;

/**
 * Some operation; note that these are not intended for use on multiple threads. A concrete {@link Operation} class
 * should have its {@code RESULT} type parameter fully bound.
 * 
 * @param <RESULT>
 */
// TODO should we just store the result here and provide a setter?
public abstract class Operation<RESULT> {
    private static final TypeVariable<?> TYPE_VARIABLE_RESULT = Operation.class.getTypeParameters()[0];

    private static final Map<Class<?>, Boolean> VALID_INFO = new HashMap<Class<?>, Boolean>();

    private static boolean init(Class<?> type) {
        final boolean valid;
        synchronized (type) {
            if (VALID_INFO.containsKey(type)) {
                valid = VALID_INFO.get(type).booleanValue();
            } else if (Modifier.isAbstract(type.getModifiers())) {
                valid = true;
            } else {
                final Type resultType =
                    Types.unrollVariables(TypeUtils.getTypeArguments(type, Operation.class), TYPE_VARIABLE_RESULT);
                valid = !Types.containsTypeVariables(resultType);
                Validate.isTrue(valid, "%s does not fully bind type parameter %s from %s", type,
                    TYPE_VARIABLE_RESULT.getName(), Operation.class);
                VALID_INFO.put(type, Boolean.valueOf(valid));
            }
        }

        final Class<?> parent = type.getSuperclass();
        if (!Operation.class.equals(parent)) {
            init(parent.asSubclass(Operation.class));
        }
        return valid;
    }

    {
        @SuppressWarnings("unchecked")
        final Class<? extends Operation<?>> c = (Class<? extends Operation<?>>) getClass();
        init(c);
        Validate.isTrue(VALID_INFO.get(c).booleanValue(), "Invalid %s: %s", Operation.class.getName(), c);
    }

    private boolean successful;

    /**
     * Get the result. Default implementation throws {@link OperationException} if the operation was unsuccessful, else
     * defers to {@link #provideResult()}.
     * 
     * @return RESULT
     * @see #provideResult()
     */
    public RESULT getResult() {
        if (!isSuccessful()) {
            throw new OperationException(this, "result unavailable");
        }
        return provideResult();
    }

    /**
     * Template method; must be implemented unless {@link #getResult()} is overridden.
     * 
     * @return RESULT
     */
    protected RESULT provideResult() {
        throw new OperationException(this, "no result provided");
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    /**
     * Learn whether {@code operator} seems to implement {@code this}.
     * 
     * @param operator
     * @return boolean
     * @see #getType(TypeVariable)
     */
    public boolean matches(Operator<?> operator) {
        final Type expectedType =
            Types.unrollVariables(TypeUtils.getTypeArguments(operator.getClass(), Operator.class),
                Operator.class.getTypeParameters()[0]);

        if (!TypeUtils.isInstance(this, expectedType)) {
            return false;
        }

        for (Class<?> c : Types.hierarchy(TypeUtils.getRawType(expectedType, operator.getClass()))) {
            if (c.equals(Operation.class)) {
                break;
            }
            final Map<TypeVariable<?>, Type> typeArguments = TypeUtils.getTypeArguments(expectedType, c);

            for (TypeVariable<?> var : c.getTypeParameters()) {
                Type type = Types.resolveAt(this, var);
                if (type == null || typeArguments == null) {
                    continue;
                }
                if (type instanceof Class<?> && ((Class<?>) type).isPrimitive()) {
                    type = ClassUtils.primitiveToWrapper((Class<?>) type);
                }
                if (!TypeUtils.isAssignable(type, Types.unrollVariables(typeArguments, var))) {
                    return false;
                }
            }
        }
        return true;
    }

}
