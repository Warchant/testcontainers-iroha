package jp.co.soramitsu.iroha.testcontainers.detail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import org.testcontainers.shaded.com.fasterxml.jackson.annotation.JsonProperty;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class IrohaConfig {

  // this one must match config name passed to iroha entrypoint.sh
  public static final String defaultConfigFileName = "config.docker";

  @Builder.Default
  @NonNull
  private String block_store_path = "/blocks";

  @Builder.Default
  private int torii_port = 50051;

  @Builder.Default
  private int internal_port = 10001;

  @Builder.Default
  @NonNull
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
