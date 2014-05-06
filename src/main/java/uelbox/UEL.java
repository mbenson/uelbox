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

import java.beans.FeatureDescriptor;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.PropertyNotFoundException;
import javax.el.PropertyNotWritableException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

/**
 * UEL utility methods.
 */
public class UEL {
    /**
     * Default trigger character for delimited UEL expressions.
     */
    public static final char DEFAULT_TRIGGER = '#';

    private static final char DOT = '.';
    private static final char LBRACK = '[';
    private static final Pattern DELIMITED_EXPR = Pattern.compile("^.\\{\\s*(.*?)\\s*\\}$");

    private static final ELResolver NOP_EL_RESOLVER = new ELResolver() {

        @Override
        public Class<?> getCommonPropertyType(ELContext arg0, Object arg1) {
            return null;
        }

        @Override
        public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext arg0, Object arg1) {
            return null;
        }

        @Override
        public Class<?> getType(ELContext arg0, Object arg1, Object arg2) throws NullPointerException,
            PropertyNotFoundException, ELException {
            return null;
        }

        @Override
        public Object getValue(ELContext arg0, Object arg1, Object arg2) throws NullPointerException,
            PropertyNotFoundException, ELException {
            return null;
        }

        @Override
        public boolean isReadOnly(ELContext arg0, Object arg1, Object arg2) throws NullPointerException,
            PropertyNotFoundException, ELException {
            return false;
        }

        @Override
        public void setValue(ELContext arg0, Object arg1, Object arg2, Object arg3) throws NullPointerException,
            PropertyNotFoundException, PropertyNotWritableException, ELException {
        }

    };

    /**
     * Get an ExpressionFactory instance for the specified context, using {@link ELContext#getContext(Class)}, and
     * setting such a context value, if not found, to {@link javax.el.ExpressionFactory#newInstance()}.
     * 
     * @param context
     * @return ExpressionFactory
     */
    public static ExpressionFactory getExpressionFactory(ELContext context) {
        ExpressionFactory result = getContext(context, ExpressionFactory.class);
        if (result == null) {
            result = ExpressionFactory.newInstance();
            context.putContext(ExpressionFactory.class, result);
        }
        return result;
    }

    /**
     * Casts context objects per documented convention.
     * 
     * @param <T>
     * @param context
     * @param key
     * @return T
     */
    public static <T> T getContext(ELContext context, Class<T> key) {
        return getContext(context, key, null);
    }

    /**
     * Casts context objects per documented convention.
     * 
     * @param <T>
     * @param context
     * @param key
     * @param defaultValue
     * @return T
     */
    public static <T> T getContext(ELContext context, Class<T> key, T defaultValue) {
        @SuppressWarnings("unchecked")
        final T result = (T) context.getContext(key);
        return result == null ? defaultValue : result;
    }

    /**
     * Embed the specified expression, if necessary, using {@link #DEFAULT_TRIGGER} as the triggering character.
     * 
     * @param expression
     * @return String
     */
    public static String embed(String expression) {
        return embed(expression, DEFAULT_TRIGGER);
    }

    /**
     * Embed the specified expression, if necessary, using the specified triggering character.
     * 
     * @param expression
     * @param trigger
     * @return String
     */
    public static String embed(final String expression, final char trigger) {
        return new StringBuilder().append(trigger).append('{').append(strip(expression)).append('}').toString();
    }

    /**
     * Learn whether the specified expression is delimited.
     * 
     * @param expression
     * @return boolean
     */
    public static boolean isDelimited(final String expression) {
        return DELIMITED_EXPR.matcher(StringUtils.trimToEmpty(expression)).matches();
    }

    /**
     * Get the trigger character for the specified delimited expression.
     * 
     * @param delimitedExpression
     * @return first non-whitespace character of {@code delimitedExpression}
     * @throws IllegalArgumentException if argument expression is not delimited
     */
    public static char getTrigger(final String delimitedExpression) {
        final String expr = StringUtils.trimToEmpty(delimitedExpression);
        Validate.isTrue(isDelimited(expr));
        return expr.charAt(0);
    }

    /**
     * Strip any delimiter from the specified expression.
     * 
     * @param expression
     * @return String
     */
    public static String strip(final String expression) {
        final String expr = StringUtils.trimToEmpty(expression);
        final Matcher matcher = DELIMITED_EXPR.matcher(expr);
        return matcher.matches() ? matcher.group(1) : expr;
    }

    /**
     * Join expressions using the trigger character, if any ({@link #DEFAULT_TRIGGER} if absent), of the first.
     * 
     * @param expressions
     * @return String
     */
    public static String join(final String... expressions) {
        Validate.notEmpty(expressions);
        final char trigger = isDelimited(expressions[0]) ? getTrigger(expressions[0]) : DEFAULT_TRIGGER;
        return join(trigger, expressions);
    }

    /**
     * Join expressions using the specified trigger character.
     * 
     * @param trigger
     * @param expressions
     * 
     * @return String
     */
    public static String join(final char trigger, final String... expressions) {
        Validate.notEmpty(expressions);

        final StringBuilder buf = new StringBuilder();

        for (String expression : expressions) {
            final String stripped = strip(expression);
            if (expression.isEmpty()) {
                continue;
            }

            final int len = buf.length();
            if (len > 0) {
                final int end = len - 1;

                final char last = buf.charAt(end);
                final char first = stripped.charAt(0);
                
                switch (first) {
                case DOT:
                case LBRACK:
                    if (last == DOT) {
                        buf.deleteCharAt(end);
                    }
                    break;
                default:
                    if (last != DOT) {
                        buf.append(DOT);
                    }
                    break;
                }
            }
            buf.append(stripped);
        }
        return embed(buf.toString(), trigger);
    }

    /**
     * Use EL specification coercion facilities to coerce an object to the specified type.
     * 
     * @param context
     * @param toType
     * @param object
     * @return T
     * @throws ELException if the coercion fails.
     */
    public static <T> T coerceToType(ELContext context, Class<T> toType, Object object) {
        @SuppressWarnings("unchecked")
        T result = (T) getExpressionFactory(context).coerceToType(object, toType);
        return result;
    }

    /**
     * Get an {@link ELResolver} that handles nothing.
     * 
     * @return ELResolver
     */
    public static ELResolver nopELResolver() {
        return NOP_EL_RESOLVER;
    }
}
