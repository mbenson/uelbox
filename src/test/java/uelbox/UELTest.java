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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import javax.el.ELContext;
import javax.el.ExpressionFactory;

import org.junit.Before;
import org.junit.Test;

/**
 * Test {@link UEL}.
 */
public class UELTest {
    private ELContext context;

    @Before
    public void setup() {
        context = new SimpleELContext();
    }

    @Test
    public void testGetExpressionFactory() {
        ExpressionFactory expressionFactory = UEL.getExpressionFactory(context);
        assertNotNull(expressionFactory);
        assertSame(expressionFactory, UEL.getExpressionFactory(context));
    }

    @Test
    public void testEmbedExpression() {
        assertEquals("#{foo[bar].baz}", UEL.embed("foo[bar].baz"));
        assertEquals("#{foo[bar].baz}", UEL.embed(" foo[bar].baz "));
        assertEquals("#{foo[bar].baz}", UEL.embed("#{foo[bar].baz}"));
        assertEquals("#{foo[bar].baz}", UEL.embed("#{ foo[bar].baz }"));
        assertEquals("#{foo[bar].baz}", UEL.embed("${foo[bar].baz}"));
        assertEquals("#{foo[bar].baz}", UEL.embed("${ foo[bar].baz} "));
    }

    @Test
    public void testEmbedExpressionWithTrigger() {
        assertEquals("#{foo[bar].baz}", UEL.embed("foo[bar].baz", '#'));
        assertEquals("#{foo[bar].baz}", UEL.embed("#{foo[bar].baz}", '#'));
        assertEquals("#{foo[bar].baz}", UEL.embed("${foo[bar].baz}", '#'));
        assertEquals("!{foo[bar].baz}", UEL.embed("${foo[bar].baz}", '!'));
    }

    @Test
    public void testStripExpression() {
        assertEquals("foo[bar].baz", UEL.strip("#{foo[bar].baz}"));
        assertEquals("foo[bar].baz", UEL.strip(" #{foo[bar].baz} "));
        assertEquals("foo[bar].baz", UEL.strip("${foo[bar].baz}"));
        assertEquals("foo[bar].baz", UEL.strip("${ foo[bar].baz }"));
        assertEquals("foo[bar].baz", UEL.strip("!{foo[bar].baz}"));
        assertEquals("foo[bar].baz", UEL.strip(" !{foo[bar].baz }"));
        assertEquals("foo[bar].baz", UEL.strip("@{ foo[bar].baz} "));
        assertEquals("foo[bar].baz", UEL.strip(" %{ foo[bar].baz } "));
        assertEquals("foo[bar].baz", UEL.strip("foo[bar].baz"));
        assertEquals("foo[bar].baz", UEL.strip("\tfoo[bar].baz  "));
        assertEquals("", UEL.strip("${}"));
        assertEquals("", UEL.strip("${\n}"));
        assertEquals("", UEL.strip(""));
        assertEquals("", UEL.strip(null));
    }

    @Test
    public void testIsDelimited() {
        assertTrue(UEL.isDelimited("#{foo[bar].baz}"));
        assertTrue(UEL.isDelimited("${foo[bar].baz}"));
        assertTrue(UEL.isDelimited(" #{foo[bar].baz} "));
        assertFalse(UEL.isDelimited("foo[bar].baz"));
        assertFalse(UEL.isDelimited("\t"));
        assertFalse(UEL.isDelimited(""));
        assertFalse(UEL.isDelimited(null));
    }

    @Test
    public void testGetTrigger() {
        assertEquals('#', UEL.getTrigger("#{foo[bar].baz}"));
        assertEquals('$', UEL.getTrigger("${foo[bar].baz}"));
        assertEquals('#', UEL.getTrigger(" #{foo[bar].baz} "));
        assertEquals('#', UEL.getTrigger("#{ foo[bar].baz }"));
        assertEquals('#', UEL.getTrigger("#{ foo[bar].baz} "));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidGetTrigger() {
        UEL.getTrigger("foo[bar].baz");
    }

    @Test
    public void testJoin() {
        assertEquals("#{foo[bar].baz}", UEL.join("foo[bar]", "baz"));
        assertEquals("#{foo[bar].baz}", UEL.join("foo[bar]", ".baz"));
        assertEquals("#{foo[bar].baz}", UEL.join("foo[bar].", "baz"));
        assertEquals("#{foo[bar].baz}", UEL.join("foo[bar].", ".baz"));
        assertEquals("#{foo[bar].baz}", UEL.join("foo", "[bar]", "baz"));
        assertEquals("#{foo[bar].baz}", UEL.join("foo", "[bar]", ".baz"));
        assertEquals("#{foo[bar].baz}", UEL.join("foo", "[bar].", "baz"));
        assertEquals("#{foo[bar].baz}", UEL.join("foo.", "[bar].", ".baz"));
        assertEquals("#{foo[bar].baz}", UEL.join("foo.", "${ [bar]. }", ".baz"));
        assertEquals("${foo[bar].baz}", UEL.join("${foo}", "[bar].", ".baz"));
        assertEquals("${foo[bar].baz}", UEL.join("${foo}", "[bar].", "#{baz}"));
        assertEquals("${foo[bar].baz}", UEL.join(" ${ foo } ", " [bar] ", " #{ baz\t}\n"));
        assertEquals("@{foo[bar].baz}", UEL.join('@', " ${ foo } ", " [bar] ", " #{ baz\t}\n"));
    }
}
