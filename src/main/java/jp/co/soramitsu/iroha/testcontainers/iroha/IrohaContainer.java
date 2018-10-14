package jp.co.soramitsu.iroha.testcontainers.iroha;

import static org.testcontainers.containers.BindMode.READ_ONLY;

import com.github.dockerjava.api.command.InspectContainerResponse;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.ContainerState;
import org.testcontainers.containers.FailureDetectingExternalResource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitStrategyTarget;
import org.testcontainers.lifecycle.Startable;

@NoArgsConstructor
public class IrohaContainer extends FailureDetectingExternalResource implements AutoCloseable,
    Startable, WaitStrategyTarget, ContainerState {

  public static final String defaultPostgresAlias = "iroha.postgres";
  public static final String defaultIrohaAlias = "iroha";
  public static final String irohaWorkdir = "/opt/iroha_data";
  public static final String defaultIrohaDockerImage = "hyperledger/iroha:1.0.0_beta-4";
  public static final String postgresDockerImage = "postgres:9.5";

  // env vars
  private static final String POSTGRES_USER = "POSTGRES_USER";
  private static final String POSTGRES_PASSWORD = "POSTGRES_PASSWORD";
  private static final String POSTGRES_HOST = "POSTGRES_HOST";
  private static final String KEY = "KEY";

  private String irohaDockerImage = defaultIrohaDockerImage;

  private String networkName = String.format("iroha_net_%s", UUID.randomUUID().toString());

  private Logger logger = LoggerFactory.getLogger(IrohaContainer.class);

  // use default config
  private PeerConfig conf = new PeerConfig();

  private Slf4jLogConsumer logConsumer;

  @Getter
  private PostgreSQLContainer postgres;
  @Getter
  private GenericContainer iroha;
  @Getter
  private Network network;


  public IrohaContainer(String irohaDockerImage) {
    this.irohaDockerImage = irohaDockerImage;
  }

  public IrohaContainer configure() {
    // init logger
    logConsumer = new Slf4jLogConsumer(logger);

    // init docker network
    network = Network.builder().id(networkName).build();

    // init postgres
    PostgresConfig pg = conf.getIrohaConfig().getPg_opt();
    postgres = (PostgreSQLContainer) new PostgreSQLContainer(postgresDockerImage)
        .withUsername(pg.getUsername())
        .withPassword(pg.getPassword())
        .withExposedPorts(pg.getPort())
        .withNetwork(network)
        .withNetworkAliases(pg.getHost(), defaultPostgresAlias);

    // init iroha container
    iroha = new GenericContainer<>(irohaDockerImage)
        .withEnv(KEY, PeerConfig.peerKeypairName)
        .withEnv(POSTGRES_HOST, postgres.getContainerIpAddress())
        .withEnv(POSTGRES_USER, postgres.getUsername())
        .withEnv(POSTGRES_PASSWORD, postgres.getPassword())
        .withNetwork(network)
        .withExposedPorts(
            conf.getIrohaConfig().getTorii_port(),
            conf.getIrohaConfig().getInternal_port()
        )
        .withLogConsumer(logConsumer)
        .withFileSystemBind(conf.getDir().toString(), irohaWorkdir, READ_ONLY)
        .waitingFor(
            Wait.forLogMessage(".*iroha initialized.*", 1)
                .withStartupTimeout(Duration.ofSeconds(10))
        )
        .withNetworkAliases(defaultIrohaAlias);

    return this;
  }


  public IrohaContainer withPeerConfig(PeerConfig conf) {
    this.conf = conf;
    return this;
  }

  public IrohaContainer withDockerImage(String dockerImage) {
    this.irohaDockerImage = dockerImage;
    return this;
  }

  public IrohaContainer withNetwork(String networkName) {
    this.networkName = networkName;
    return this;
  }

  public IrohaContainer withLogger(Logger logger) {
    this.logger = logger;
    return this;
  }

  @Override
  public void start() {
    postgres.start();
    iroha.start();
  }

  @Override
  public void stop() {
    iroha.stop();
    postgres.stop();
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Integer> getExposedPorts() {
    return iroha.getExposedPorts();
  }

  @Override
  public String getContainerId() {
    return iroha.getContainerId();
  }

  @Override
  public InspectContainerResponse getContainerInfo() {
    return iroha.getContainerInfo();
  }
}
