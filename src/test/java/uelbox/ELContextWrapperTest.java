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

import static org.junit.Assert.*;

import javax.el.ELContext;
import javax.el.ELResolver;

import org.junit.Before;
import org.junit.Test;

public class ELContextWrapperTest {

    private ELContext wrapped;
    private ELContextWrapper contextWrapper;

    @Before
    public void setup() {
        wrapped = new SimpleELContext();
        contextWrapper = new ELContextWrapper(wrapped) {

            @Override
            protected ELResolver wrap(ELResolver elResolver) {
                return elResolver;
            }
        };
    }

    @Test
    public void testWrappedContextObject() {
        wrapped.putContext(getClass(), this);
        assertSame(this, contextWrapper.getContext(getClass()));
    }

    @Test
    public void testShadowedContextObject() {
        wrapped.putContext(getClass(), new ELContextWrapperTest());
        contextWrapper.putContext(getClass(), this);
        assertSame(this, contextWrapper.getContext(getClass()));
        assertNotSame(wrapped.getContext(getClass()), contextWrapper.getContext(getClass()));
    }

    @Test(expected = NullPointerException.class)
    public void testPutNullContextKey() {
        wrapped.putContext(null, this);
    }

    @Test(expected = NullPointerException.class)
    public void testPutNullContextObject() {
        wrapped.putContext(getClass(), null);
    }

    @Test
    public void testRemoveContextObject() {
        contextWrapper.putContext(getClass(), this);
        assertSame(this, contextWrapper.removeContext(getClass()));
        assertNull(contextWrapper.getContext(getClass()));
    }

    @Test
    public void testRemoveShadowedContextObject() {
        wrapped.putContext(getClass(), this);
        assertSame(this, contextWrapper.removeContext(getClass()));
        assertNull(contextWrapper.getContext(getClass()));
        assertSame(this, wrapped.getContext(getClass()));
    }
}
