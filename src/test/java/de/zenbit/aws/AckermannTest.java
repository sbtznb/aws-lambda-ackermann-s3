package de.zenbit.aws;

import static de.zenbit.aws.Ackermann.ackermann;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AckermannTest {
    
    @ParameterizedTest(name = "{index} => A(m={0}, n={1}) = result={2}")
    @CsvSource({
            "0, 0, 1",
            "0, 1000, 1001", // A(0, m) = m + 1
            "1, 1, 3",
            "2, 3, 9",
            "2, 4, 11",
            "3, 1, 13",
            "3, 2, 29",
            "3, 3, 61",
            "3, 4, 125",
            "4, 1, 65533" // A(4,1) may take a minute to compute
    })
    void testAckermann(long m, long n, long result) {
        assertThat(ackermann(m, n), is(result));
    }

    @Test
    void testAckermann_NegativeM_Error() {

        final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            ackermann(-1, 2);
        });

        assertThat(thrown.getMessage(), startsWith("Undefined"));
    }
    
    @Test
    void testAckermann_NegativeN_Error() {

        final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            ackermann(1, -2);
        });

        assertThat(thrown.getMessage(), startsWith("Undefined"));
    }

}
