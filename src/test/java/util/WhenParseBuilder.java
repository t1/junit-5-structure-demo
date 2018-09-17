package util;

import lombok.RequiredArgsConstructor;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.catchThrowableOfType;

@RequiredArgsConstructor
public class WhenParseBuilder<T> {
    public static <T> WhenParseBuilder<T> when(Supplier<T> call) { return new WhenParseBuilder<>(call); }

    private final Supplier<T> call;

    public <E extends Throwable> OngoingFailureBuilder<E> failsWith(Class<E> exceptionClass) {
        return new OngoingFailureBuilder<>(exceptionClass);
    }

    @RequiredArgsConstructor
    public class OngoingFailureBuilder<E extends Throwable> {

        private final Class<E> exceptionClass;
        private Consumer<E> verifyException;

        public OngoingFailureBuilder<E> then(Consumer<E> verifyException) {
            this.verifyException = verifyException;
            return this;
        }

        public OngoingSuccessBuilder succeeds() { return new OngoingSuccessBuilder(); }

        public class OngoingSuccessBuilder {
            public void then(Consumer<T> verify) {
                AtomicReference<T> success = new AtomicReference<>();
                E failure = catchThrowableOfType(() -> success.set(call.get()), exceptionClass);

                if (failure != null)
                    verifyException.accept(failure);
                else
                    verify.accept(success.get());
            }
        }
    }
}
