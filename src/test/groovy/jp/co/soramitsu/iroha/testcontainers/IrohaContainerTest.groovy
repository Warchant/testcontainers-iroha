package jp.co.soramitsu.iroha.testcontainers

import com.google.protobuf.util.JsonFormat
import iroha.protocol.BlockOuterClass
import jp.co.soramitsu.iroha.testcontainers.detail.GenesisBlockBuilder
import jp.co.soramitsu.iroha.testcontainers.detail.IrohaConfig
import org.junit.Rule
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper
import spock.lang.Specification

class IrohaContainerTest extends Specification {

    @Rule
    IrohaContainer ir = new IrohaContainer()

    def "temp folder is created and files are written"() {
        given:
        def mapper = new ObjectMapper()
        def c = ir.getConf()
        def d = c.getDir()

        when: "create tmp dir and dump files"
        ir.configure()

        then:
        d.exists()
        d.list().toList().toSet() == [
                GenesisBlockBuilder.defaultGenesisBlockName,
                PeerConfig.peerKeypairName + ".pub",
                PeerConfig.peerKeypairName + ".priv",
                IrohaConfig.defaultConfigFileName
        ].toSet()

        when: "parse config.docker"
        File confFile = new File(d, "config.docker")
        IrohaConfig config = mapper.readValue(confFile, IrohaConfig.class)

        then: "configs are equal"
        config == c.getIrohaConfig()

        when: "parse genesis.block"
        File blockFile = new File(d, "genesis.block")
        def builder = BlockOuterClass.Block.newBuilder()
        JsonFormat.parser().merge(blockFile.getText(), builder)

        then: "genesis blocks are equal"
        builder.build() == c.getGenesisBlock()

    }

    def "iroha starts and stops with zero configuration"() {
        when:
        ir.start()

        then:
        ir.iroha.isCreated()
        ir.iroha.isHealthy()
        ir.iroha.isRunning()
        ir.postgres.isCreated()
        ir.postgres.isRunning()

        when:
        ir.stop()

        then:
        !ir.iroha.isCreated()
        !ir.iroha.isHealthy()
        !ir.iroha.isRunning()
        !ir.postgres.isCreated()
        !ir.postgres.isRunning()
    }
}
