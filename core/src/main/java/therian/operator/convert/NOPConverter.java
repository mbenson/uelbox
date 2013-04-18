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

import org.apache.commons.lang3.reflect.TypeUtils;

import therian.Operator;
import therian.TherianContext;
import therian.operation.Convert;
import therian.operation.ImmutableCheck;

/**
 * Uses source value as target value when assignable and immutable.
 */
public class NOPConverter implements Operator<Convert<?, ?>> {

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public boolean perform(TherianContext context, Convert<?, ?> operation) {
        final Convert raw = operation;
        raw.getTargetPosition().setValue(raw.getSourcePosition().getValue());
        return true;
    }

    public boolean supports(TherianContext context, Convert<?, ?> operation) {
        return TypeUtils.isInstance(operation.getSourcePosition().getValue(), operation.getTargetPosition().getType())
                && context.eval(ImmutableCheck.of(operation.getSourcePosition())).booleanValue();
    }

}
