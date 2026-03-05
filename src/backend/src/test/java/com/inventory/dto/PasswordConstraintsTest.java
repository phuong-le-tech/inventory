package com.inventory.dto;

import com.inventory.dto.request.PasswordConstraints;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PasswordConstraints Tests")
class PasswordConstraintsTest {

    @Test
    @DisplayName("MIN_LENGTH should be 12")
    void minLengthShouldBe12() {
        assertThat(PasswordConstraints.MIN_LENGTH).isEqualTo(12);
    }

    @Nested
    @DisplayName("Lowercase pattern")
    class LowercasePattern {
        @ParameterizedTest
        @ValueSource(strings = {"abc", "ABCd", "123a456", "TESTpassWORD1"})
        void shouldMatchStringsWithLowercase(String input) {
            assertThat(input.matches(PasswordConstraints.LOWERCASE_PATTERN)).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {"ABC", "123", "!@#$%", "ABC123!@#"})
        void shouldRejectStringsWithoutLowercase(String input) {
            assertThat(input.matches(PasswordConstraints.LOWERCASE_PATTERN)).isFalse();
        }
    }

    @Nested
    @DisplayName("Uppercase pattern")
    class UppercasePattern {
        @ParameterizedTest
        @ValueSource(strings = {"ABC", "abcD", "123A456", "testPassWord1"})
        void shouldMatchStringsWithUppercase(String input) {
            assertThat(input.matches(PasswordConstraints.UPPERCASE_PATTERN)).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {"abc", "123", "!@#$%", "abc123!@#"})
        void shouldRejectStringsWithoutUppercase(String input) {
            assertThat(input.matches(PasswordConstraints.UPPERCASE_PATTERN)).isFalse();
        }
    }

    @Nested
    @DisplayName("Digit pattern")
    class DigitPattern {
        @ParameterizedTest
        @ValueSource(strings = {"123", "abc1", "A2B", "test3Password"})
        void shouldMatchStringsWithDigit(String input) {
            assertThat(input.matches(PasswordConstraints.DIGIT_PATTERN)).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {"abc", "ABC", "!@#$%", "abcABC!@#"})
        void shouldRejectStringsWithoutDigit(String input) {
            assertThat(input.matches(PasswordConstraints.DIGIT_PATTERN)).isFalse();
        }
    }

    @Nested
    @DisplayName("Special character pattern")
    class SpecialCharPattern {
        @ParameterizedTest
        @ValueSource(strings = {"abc!", "A@B", "test#1", "pass$word", "a^b", "a&b", "a*b", "a(b", "a)b", "a[b", "a{b", "a;b", "a:b", "a'b", "a\"b", "a\\b", "a|b", "a,b", "a.b", "a<b", "a>b", "a/b", "a?b", "a~b", "a`b", "a+b", "a-b", "a=b", "a_b"})
        void shouldMatchStringsWithSpecialChar(String input) {
            assertThat(input.matches(PasswordConstraints.SPECIAL_CHAR_PATTERN)).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {"abc", "ABC", "123", "abcABC123"})
        void shouldRejectStringsWithoutSpecialChar(String input) {
            assertThat(input.matches(PasswordConstraints.SPECIAL_CHAR_PATTERN)).isFalse();
        }
    }
}
