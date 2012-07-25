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
package net.morph;

import javax.el.ELContextListener;
import javax.el.ELResolver;

/**
 * Morph module.
 */
public class MorphModule {
    private ELResolver[] elResolvers;
    private ELContextListener[] elContextListeners;
    private Operator<?>[] operators;

    private MorphModule() {
    }

    public synchronized ELResolver[] getElResolvers() {
        if (elResolvers == null) {
            elResolvers = new ELResolver[0];
        }
        return elResolvers;
    }

    public ELContextListener[] getElContextListeners() {
        if (elContextListeners == null) {
            elContextListeners = new ELContextListener[0];
        }
        return elContextListeners;
    }

    public Operator<?>[] getOperators() {
        if (operators == null) {
            operators = new Operator[0];
        }
        return operators;
    }

    public MorphModule withELResolvers(ELResolver... elResolvers) {
        this.elResolvers = elResolvers;
        return this;
    }

    public MorphModule withELContextListeners(ELContextListener... elContextListeners) {
        this.elContextListeners = elContextListeners;
        return this;
    }

    public MorphModule withOperators(Operator<?>... operators) {
        this.operators = operators;
        return this;
    }

    public static MorphModule create() {
        return new MorphModule();
    }
}
