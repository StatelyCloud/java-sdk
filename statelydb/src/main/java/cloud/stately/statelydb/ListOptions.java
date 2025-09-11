package cloud.stately.statelydb;

import cloud.stately.db.CelExpression;
import cloud.stately.db.FilterCondition;
import cloud.stately.db.KeyCondition;
import cloud.stately.db.Operator;
import cloud.stately.db.SortDirection;
import java.util.ArrayList;
import java.util.List;

/**
 * ListOptions provides configuration options for list operations (beginList). Use the builder
 * pattern to configure the desired options.
 */
public class ListOptions {
  private final int limit;
  private final SortDirection sortDirection;
  private final List<String> itemTypes;
  private final List<CelFilter> celFilters;
  private final String gt;
  private final String gte;
  private final String lt;
  private final String lte;

  private ListOptions(Builder builder) {
    this.limit = builder.limit;
    this.sortDirection = builder.sortDirection;
    this.itemTypes = builder.itemTypes;
    this.celFilters = builder.celFilters;
    this.gt = builder.gt;
    this.gte = builder.gte;
    this.lt = builder.lt;
    this.lte = builder.lte;
  }

  /**
   * Creates a new builder for ListOptions.
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
   * Returns the sort direction for the results.
   *
   * @return The sort direction
   */
  public SortDirection getSortDirection() {
    return sortDirection;
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
   * Builds the key conditions for the gRPC request.
   *
   * @return List of KeyCondition objects
   */
  public List<KeyCondition> buildKeyConditions() {
    List<KeyCondition> conditions = new ArrayList<>();

    if (gt != null) {
      conditions.add(
          KeyCondition.newBuilder()
              .setOperator(Operator.OPERATOR_GREATER_THAN)
              .setKeyPath(gt)
              .build());
    }

    if (gte != null) {
      conditions.add(
          KeyCondition.newBuilder()
              .setOperator(Operator.OPERATOR_GREATER_THAN_OR_EQUAL)
              .setKeyPath(gte)
              .build());
    }

    if (lt != null) {
      conditions.add(
          KeyCondition.newBuilder()
              .setOperator(Operator.OPERATOR_LESS_THAN)
              .setKeyPath(lt)
              .build());
    }

    if (lte != null) {
      conditions.add(
          KeyCondition.newBuilder()
              .setOperator(Operator.OPERATOR_LESS_THAN_OR_EQUAL)
              .setKeyPath(lte)
              .build());
    }

    return conditions;
  }

  /** Builder for ListOptions. */
  public static class Builder {

    /** Creates a new Builder for ListOptions. */
    public Builder() {}

    private int limit = 0;
    private SortDirection sortDirection = SortDirection.SORT_ASCENDING;
    private List<String> itemTypes = new ArrayList<>();
    private List<CelFilter> celFilters = new ArrayList<>();
    private String gt;
    private String gte;
    private String lt;
    private String lte;

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
     * Sets the sort direction for results.
     *
     * @param sortDirection The sort direction
     * @return This builder instance
     */
    public Builder sortDirection(SortDirection sortDirection) {
      this.sortDirection = sortDirection;
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
     * Sets the "greater than" key condition.
     *
     * @param keyPath The key path for the condition
     * @return This builder instance
     */
    public Builder greaterThan(String keyPath) {
      this.gt = keyPath;
      return this;
    }

    /**
     * Sets the "greater than or equal" key condition.
     *
     * @param keyPath The key path for the condition
     * @return This builder instance
     */
    public Builder greaterThanOrEqual(String keyPath) {
      this.gte = keyPath;
      return this;
    }

    /**
     * Sets the "less than" key condition.
     *
     * @param keyPath The key path for the condition
     * @return This builder instance
     */
    public Builder lessThan(String keyPath) {
      this.lt = keyPath;
      return this;
    }

    /**
     * Sets the "less than or equal" key condition.
     *
     * @param keyPath The key path for the condition
     * @return This builder instance
     */
    public Builder lessThanOrEqual(String keyPath) {
      this.lte = keyPath;
      return this;
    }

    /**
     * Builds the ListOptions instance.
     *
     * @return A new ListOptions instance
     */
    public ListOptions build() {
      return new ListOptions(this);
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
