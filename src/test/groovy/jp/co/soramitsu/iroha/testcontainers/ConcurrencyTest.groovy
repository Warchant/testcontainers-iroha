package jp.co.soramitsu.iroha.testcontainers

import spock.lang.Specification
import spock.lang.Unroll

class ConcurrencyTest extends Specification {

    def singleRun(int id) {
        new IrohaContainer().withCloseable { iroha ->
            iroha.start()
            printf("iroha %d started listening at %s\n", id, iroha.getToriiAddress())
            Thread.sleep(2000)
            printf("iroha %d at %s is stopping...\n", id, iroha.getToriiAddress())
        }
    }

    @Unroll
    def "#containersTotal containers can be started simultaneously"() {
        given:
        def threads = (0..<containersTotal).collect { id ->
            Thread.start {
                singleRun(id)
            }
        }

        when:
        threads*.join()

        then:
        noExceptionThrown()

        where:
        containersTotal = 5
    }

}
