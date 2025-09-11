package cloud.stately.statelydb.auth;

import java.time.Instant;

/** Persistent state for the token provider. */
public final class TokenState {

  private final String token;
  private final Instant expiresAt;

  /**
   * Create a new TokenState.
   *
   * @param token The access token
   * @param expiresAt The time when the token expires
   */
  public TokenState(String token, Instant expiresAt) {
    this.token = token;
    this.expiresAt = expiresAt;
  }

  /**
   * Get the token string.
   *
   * @return The token string
   */
  public String getToken() {
    return token;
  }

  /**
   * Get the expiration time.
   *
   * @return The expiration time as an Instant
   */
  public Instant getExpiresAt() {
    return expiresAt;
  }
}
