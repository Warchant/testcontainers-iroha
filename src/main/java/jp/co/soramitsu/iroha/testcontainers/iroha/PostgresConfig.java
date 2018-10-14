package jp.co.soramitsu.iroha.testcontainers.iroha;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PostgresConfig {

  @Builder.Default
  private String host = "iroha.postgres";
  @Builder.Default
  private int port = 5432;
  @Builder.Default
  private String username = "postgres";
  @Builder.Default
  private String password = "postgres";

  @Override
  public String toString() {
    return String
        .format("host=%s port=%d username=%s password=%s", host, port, username, password);
  }
}
