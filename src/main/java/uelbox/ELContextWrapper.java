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
package uelbox;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.FunctionMapper;
import javax.el.ValueExpression;
import javax.el.VariableMapper;

import org.apache.commons.lang3.Validate;

/**
 * ELContext wrapper which wraps the ELResolver and may shadow variables, locale settings, and context objects.
 */
public abstract class ELContextWrapper extends ELContext {
    private final ELResolver elResolver;
    private final VariableMapper variableMapper;

    /**
     * Map in which context objects are potentially stored so that we can remove them if desired.
     * @see #removeContext(Class)
     */
    private Map<Class<?>, Object> contextObjects;

    protected final ELContext wrapped;

    /**
     * Create a new ELContextWrapper.
     * 
     * @param wrapped
     */
    protected ELContextWrapper(ELContext wrapped) {
        this.wrapped = Validate.notNull(wrapped, "wrapped ELContext");
        this.elResolver = Validate.notNull(wrap(wrapped.getELResolver()));
        this.variableMapper = new SimpleVariableMapper() {
            @Override
            public ValueExpression resolveVariable(String variable) {
                if (containsVariable(variable)) {
                    return super.resolveVariable(variable);
                }
                return ELContextWrapper.this.wrapped.getVariableMapper().resolveVariable(variable);
            }
        };
    }

    /**
     * Create a wrapped ELResolver for use with the wrapped {@link ELContext}.
     * 
     * @param elResolver
     *            to wrap
     * @return {@link ELResolver}
     */
    protected abstract ELResolver wrap(ELResolver elResolver);

    @Override
    public ELResolver getELResolver() {
        return elResolver;
    }

    @Override
    public FunctionMapper getFunctionMapper() {
        return wrapped.getFunctionMapper();
    }

    @Override
    public VariableMapper getVariableMapper() {
        return variableMapper;
    }

    @Override
    public Locale getLocale() {
        final Locale result = super.getLocale();
        return result == null ? super.getLocale() : result;
    }

    @Override
    public Object getContext(@SuppressWarnings("rawtypes") Class key) {
        if (contextObjects != null && contextObjects.containsKey(key)) {
            return contextObjects.get(key);
        }
        return wrapped.getContext(key);
    }

    @Override
    public void putContext(@SuppressWarnings("rawtypes") Class key, Object contextObject) throws NullPointerException {
        Validate.notNull(contextObject, "context object must not be null");
        putContextInternal(key, contextObject);
    }

    /**
     * Dedicated method for removing a context value, as
     * {@link #putContext(Class, Object)} is declared to throw {@link NullPointerException} for a {@code null} value.
     * @param key
     * @return previously stored context object
     */
    public Object removeContext(Class<?> key) {
        // hide the context objects of a wrapped ELContext by storing a null value:
        final Object result = putContextInternal(key, null);
        return result == null ? wrapped.getContext(key) : result;
    }

    private synchronized Object putContextInternal(Class<?> key, Object value) {
        Validate.notNull(key, "context key must not be null");
        if (contextObjects == null) {
            contextObjects = new HashMap<Class<?>, Object>();
        }
        return contextObjects.put(key, value);
    }

    /**
     * Convenience method to return a typed context object when key resolves per documented convention to an object of
     * the same type.
     * 
     * @param <T>
     * @param key
     * @return T
     * @see ELContext#getContext(Class)
     */
    public final <T> T getTypedContext(Class<T> key) {
        return UEL.getContext(this, key);
    }

    /**
     * Convenience method to return a typed context object when key resolves per documented convention to an object of
     * the same type.
     * 
     * @param <T>
     * @param key
     * @param defaultValue if absent
     * @return T
     * @see ELContext#getContext(Class)
     */
    public final <T> T getTypedContext(Class<T> key, T defaultValue) {
        return UEL.getContext(this, key, defaultValue);
    }
    
}
