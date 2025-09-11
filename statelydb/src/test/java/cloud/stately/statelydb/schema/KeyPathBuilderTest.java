package cloud.stately.statelydb.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Unit tests for KeyPathBuilder. */
class KeyPathBuilderTest {

  @Test
  @DisplayName("Supports single-level paths")
  void testSingleLevelPaths() {
    assertEquals(new KeyPathBuilder().with("foo", "bar").build(), "/foo-bar");
    assertEquals(new KeyPathBuilder().with("foo", "1234").build(), "/foo-1234");
    assertEquals(new KeyPathBuilder().with("foo", "1234/abc").build(), "/foo-1234%/abc");
    assertEquals(
        new KeyPathBuilder()
            .with("foo", UUID.fromString("f4a8a24a-129d-411f-91d2-6d19d0eaa096"))
            .build(),
        "/foo-f4a8a24a-129d-411f-91d2-6d19d0eaa096");
  }

  @Test
  @DisplayName("Supports multi-level paths")
  void testMultiLevelPaths() {
    assertEquals(
        new KeyPathBuilder().with("foo", "bar").with("baz", "qux").with("quux", "corge").build(),
        "/foo-bar/baz-qux/quux-corge");
  }

  @Test
  @DisplayName("Supports partial paths")
  void testPartialPaths() {
    assertEquals(
        new KeyPathBuilder().with("foo", "bar").with("namespace").build(), "/foo-bar/namespace");
  }

  @Test
  @DisplayName("Supports empty paths")
  void testEmptyPath() {
    assertEquals(new KeyPathBuilder().build(), "/");
  }

  @Test
  @DisplayName("Support UUIDs in paths")
  void testUuidInPath() {
    UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000005");
    assertEquals(
        new KeyPathBuilder().with("foo", uuid).build(),
        "/foo-00000000-0000-0000-0000-000000000005");
  }

  @Test
  @DisplayName("Supports strings")
  void testStringInPath() {
    assertEquals(new KeyPathBuilder().with("batman", "batman/123").build(), "/batman-batman%/123");
  }

  @Test
  @DisplayName("Supports byte arrays")
  void testByteArrayInPath() {
    byte[] bytes = new byte[] {0, 1, 2, 3};
    assertEquals(new KeyPathBuilder().with("data", bytes).build(), "/data-AAECAw");
  }

  @Test
  @DisplayName("Supports integers")
  void testIntegerInPath() {
    assertEquals(new KeyPathBuilder().with("number", 1234).build(), "/number-1234");
  }
}
