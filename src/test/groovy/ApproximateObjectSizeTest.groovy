import spock.lang.Specification
import spock.lang.Unroll

class ApproximateObjectSizeTest extends Specification {

    @Unroll
    def "test getSize object= #object"() {
        given:
        def objectSize = new ApproximateObjectSize()

        when:
        def result = objectSize.getSize(object)

        then:
        result == expectedResult

        where:
        object                                            || expectedResult
        1                                                 || 16
        4L                                                || 16
        "hello"                                           || 48
        new DateHolder()                                  || 16
        [1, 2, 3].toArray()                               || 40
        [[1, 2].toArray(), [3, 4, 5].toArray()].toArray() || 72
    }

}
