package jp.co.soramitsu.iroha.testcontainers.iroha;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class IrohaConfig {

  // this one must match config name passed in iroha entrypoint.sh
  public static final String configFileName = "config.docker";

  @Builder.Default
  private String block_store_path = "/blocks";
  @Builder.Default
  private int torii_port = 50051;
  @Builder.Default
  private int internal_port = 10001;
  @Builder.Default
  private PostgresConfig pg_opt = PostgresConfig.builder().build();
  @Builder.Default
  private int max_proposal_size = 10;
  @Builder.Default
  private int proposal_delay = 1000;
  @Builder.Default
  private int vote_delay = 1000;
  @Builder.Default
  private int load_delay = 1000;
  @Builder.Default
  private boolean mst_enable = false;
}
