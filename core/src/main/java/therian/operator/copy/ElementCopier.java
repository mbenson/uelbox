package therian.operator.copy;

import java.util.List;

import org.apache.commons.lang3.reflect.TypeUtils;

import therian.Operator;
import therian.TherianContext;
import therian.operation.Copy;
import therian.operation.Size;
import therian.position.Position;
import therian.position.Position.ReadWrite;
import therian.position.relative.Element;

/**
 * Tries to copy between arrays/iterables using {@link Element} positions. This should be more efficient than
 * {@link IterableCopier} where usable. Where elements must be added, only {@link List} targets are supported.
 */
public class ElementCopier implements Operator<Copy<?, ?>> {
    private interface ElementFactory {
        ReadWrite<?> element(int index);
    }

    public void perform(TherianContext context, Copy<?, ?> copy) {
        final ElementFactory sourceElementFactory = createElementFactory(copy.getSourcePosition());
        final ElementFactory targetElementFactory = createElementFactory(copy.getTargetPosition());
        for (int i = 0, sz = context.eval(Size.of(copy.getSourcePosition())); i < sz; i++) {
            if (context
                .evalSuccessIfSupported(Copy.to(targetElementFactory.element(i), sourceElementFactory.element(i)))) {
                continue;
            }
            return;
        }
        copy.setSuccessful(true);
    }

    public boolean supports(TherianContext context, Copy<?, ?> copy) {
        final int sourceSize = context.eval(Size.of(copy.getSourcePosition()));
        final int targetSize = context.eval(Size.of(copy.getTargetPosition()));

        return sourceSize <= targetSize && createElementFactory(copy.getSourcePosition()) != null
            && createElementFactory(copy.getTargetPosition()) != null;
    }

    private static ElementFactory createElementFactory(final Position.Readable<?> source) {
        if (source.getValue() != null) {
            if (TypeUtils.isArrayType(source.getType())) {
                return new ElementFactory() {

                    public ReadWrite<?> element(int index) {
                        return Element.atArrayIndex(index).of(source);
                    }
                };
            }
            if (TypeUtils.isAssignable(source.getType(), Iterable.class)) {

                return new ElementFactory() {

                    @SuppressWarnings("unchecked")
                    public ReadWrite<?> element(int index) {
                        return Element.atIndex(index).of((Position.Readable<Iterable<?>>) source);
                    }
                };
            }
        }
        return null;
    }
}
