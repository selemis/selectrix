package org.example

import spock.lang.Specification

class SampleSpec extends Specification {

    def "test basic arithmetic"() {
        expect:
        1 + 1 == 2
        5 * 3 == 15
    }

    def "test string operations"() {
        given:
        def greeting = "Hello"

        when:
        def result = greeting + " World"

        then:
        result == "Hello World"
        result.length() == 11
    }

    def "test with where block"() {
        expect:
        a + b == result

        where:
        a | b | result
        1 | 2 | 3
        5 | 3 | 8
        10| 0 | 10
    }
}
