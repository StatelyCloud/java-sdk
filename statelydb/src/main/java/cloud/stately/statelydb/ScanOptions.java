package cloud.stately.statelydb;

import cloud.stately.db.CelExpression;
import cloud.stately.db.FilterCondition;
import cloud.stately.db.SegmentationParams;
import java.util.ArrayList;
import java.util.List;

/**
 * ScanOptions provides configuration options for scan operations (beginScan). Use the builder
 * pattern to configure the desired options.
 */
public class ScanOptions {
  private final int limit;
  private final List<String> itemTypes;
  private final List<CelFilter> celFilters;
  private final Integer totalSegments;
  private final Integer segmentIndex;

  private ScanOptions(Builder builder) {
    this.limit = builder.limit;
    this.itemTypes = builder.itemTypes;
    this.celFilters = builder.celFilters;
    this.totalSegments = builder.totalSegments;
    this.segmentIndex = builder.segmentIndex;
  }

  /**
   * Creates a new builder for ScanOptions.
   *
   * @return A new Builder instance
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Returns the limit for the number of items to retrieve.
   *
   * @return The limit, or 0 for no limit
   */
  public int getLimit() {
    return limit;
  }

  /**
   * Returns the total number of segments for parallel scanning.
   *
   * @return The total segments, or null if not set
   */
  public Integer getTotalSegments() {
    return totalSegments;
  }

  /**
   * Returns the segment index for this scan operation.
   *
   * @return The segment index, or null if not set
   */
  public Integer getSegmentIndex() {
    return segmentIndex;
  }

  /**
   * Builds the filter conditions for the gRPC request.
   *
   * @return List of FilterCondition objects
   */
  public List<FilterCondition> buildFilterConditions() {
    List<FilterCondition> conditions = new ArrayList<>();

    // Add item type filters
    for (String itemType : itemTypes) {
      conditions.add(FilterCondition.newBuilder().setItemType(itemType).build());
    }

    // Add CEL expression filters
    for (CelFilter celFilter : celFilters) {
      conditions.add(
          FilterCondition.newBuilder()
              .setCelExpression(
                  CelExpression.newBuilder()
                      .setItemType(celFilter.getItemType())
                      .setExpression(celFilter.getExpression())
                      .build())
              .build());
    }

    return conditions;
  }

  /**
   * Builds the segmentation parameters for the gRPC request.
   *
   * @return SegmentationParams object, or null if not configured
   */
  public SegmentationParams buildSegmentationParams() {
    if (totalSegments != null && segmentIndex != null) {
      return SegmentationParams.newBuilder()
          .setTotalSegments(totalSegments)
          .setSegmentIndex(segmentIndex)
          .build();
    }
    return null;
  }

  /** Builder for ScanOptions. */
  public static class Builder {

    /** Creates a new Builder for ScanOptions. */
    public Builder() {}

    private int limit = 0;
    private List<String> itemTypes = new ArrayList<>();
    private List<CelFilter> celFilters = new ArrayList<>();
    private Integer totalSegments;
    private Integer segmentIndex;

    /**
     * Sets the maximum number of items to retrieve.
     *
     * @param limit The limit (0 for no limit)
     * @return This builder instance
     */
    public Builder limit(int limit) {
      this.limit = limit;
      return this;
    }

    /**
     * Adds an item type filter.
     *
     * @param itemType The item type to filter for
     * @return This builder instance
     */
    public Builder addItemType(String itemType) {
      this.itemTypes.add(itemType);
      return this;
    }

    /**
     * Adds a CEL expression filter.
     *
     * @param itemType The item type the expression applies to
     * @param expression The CEL expression
     * @return This builder instance
     */
    public Builder addCelFilter(String itemType, String expression) {
      this.celFilters.add(new CelFilter(itemType, expression));
      return this;
    }

    /**
     * Sets the segmentation parameters for parallel scanning.
     *
     * @param totalSegments The total number of segments
     * @param segmentIndex The index of this segment (0-based)
     * @return This builder instance
     */
    public Builder segmentation(int totalSegments, int segmentIndex) {
      this.totalSegments = totalSegments;
      this.segmentIndex = segmentIndex;
      return this;
    }

    /**
     * Builds the ScanOptions instance.
     *
     * @return A new ScanOptions instance
     */
    public ScanOptions build() {
      return new ScanOptions(this);
    }
  }

  /** Represents a CEL expression filter. */
  private static class CelFilter {
    private final String itemType;
    private final String expression;

    public CelFilter(String itemType, String expression) {
      this.itemType = itemType;
      this.expression = expression;
    }

    public String getItemType() {
      return itemType;
    }

    public String getExpression() {
      return expression;
    }
  }
}
