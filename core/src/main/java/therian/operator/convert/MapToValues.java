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
package therian.operator.convert;

import java.util.Collection;
import java.util.Map;

import therian.operation.Convert;

/**
 * Implements simple conversion of a {@link Map} to its own {@code value}
 * {@link Collection}.
 */
@SuppressWarnings("rawtypes")
public class MapToValues extends ElementConverter<Map<?, ?>, Collection> {

    public MapToValues() {
        super(Map.class.getTypeParameters()[1], Iterable.class.getTypeParameters()[0]);
    }

    public void perform(Convert<? extends Map<?, ?>, ? super Collection> operation) {
        operation.getTargetPosition().setValue(operation.getSourcePosition().getValue().values());
        operation.setSuccessful(true);
    }

}
