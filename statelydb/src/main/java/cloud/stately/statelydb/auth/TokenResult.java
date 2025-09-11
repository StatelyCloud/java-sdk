package cloud.stately.statelydb.auth;

/** Result from a token fetch operation. */
public final class TokenResult {

  private final String token;
  private final long expiresInSecs;

  /**
   * Create a new TokenResult.
   *
   * @param token The access token
   * @param expiresInSecs The number of seconds until the token expires
   */
  public TokenResult(String token, long expiresInSecs) {
    this.token = token;
    this.expiresInSecs = expiresInSecs;
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
   * Get the expiration time in seconds.
   *
   * @return The expiration time in seconds
   */
  public long getExpiresInSecs() {
    return expiresInSecs;
  }
}
