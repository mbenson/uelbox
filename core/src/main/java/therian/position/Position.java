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
package therian.position;

import therian.Typed;

/**
 * Object position.
 * 
 * @param <T>
 */
public interface Position<T> extends Typed<T> {

    /**
     * Readable position.
     */
    public interface Readable<T> extends Position<T> {
        /**
         * Get the value at this position.
         * 
         * @return T
         */
        T getValue();
    }

    /**
     * Writable position.
     * 
     * @param <T>
     */
    public interface Writable<T> extends Position<T> {
        /**
         * Set a value at this position.
         * 
         * @param value
         */
        void setValue(T value);
    }

    /**
     * Read/write position.
     * 
     * @param <T>
     */
    public interface ReadWrite<T> extends Readable<T>, Writable<T> {
    }
}
