package com.ricequant.apps.stress_test.client;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The struct contains feeds returned for one kind of feed
 *
 * @author chenfeng
 */
public class FeedsReturn {

  private List<JsonObject> iFeeds = Collections.emptyList();

  private int iNextIndex = 0;

  static FeedsReturn fromFeeds(JsonArray feeds) {
    FeedsReturn ret = new FeedsReturn();
    convertJsonArrayToList(feeds, ret);
    return ret;
  }

  static FeedsReturn fromSlice(JsonObject feedsSlice, String feedsKeyName) {
    FeedsReturn ret = new FeedsReturn();
    ret.nextIndex(feedsSlice.getInteger("NextIndex"));

    JsonArray jsonArray = feedsSlice.getJsonArray(feedsKeyName, new JsonArray());
    convertJsonArrayToList(jsonArray, ret);

    return ret;
  }

  private static void convertJsonArrayToList(JsonArray jsonArray, FeedsReturn ret) {
    List<JsonObject> feedsList = new ArrayList<>(jsonArray.size());
    for (int i = 0; i < jsonArray.size(); i++)
      feedsList.add(i, jsonArray.getJsonObject(i));

    ret.feeds(feedsList);
  }

  /**
   * Get the feeds
   *
   * @return list of JsonObject, each represent one feed
   */
  public List<JsonObject> feeds() {
    return iFeeds;
  }

  /**
   * If the client want to pull again, it should use this index as the "start-index"
   *
   * @return the "start-index" of next pull request
   */
  public int nextIndex() {
    return iNextIndex;
  }

  void feeds(List<JsonObject> feeds) {
    iFeeds = feeds;
  }

  void nextIndex(int index) {
    iNextIndex = index;
  }


  @Override
  public String toString() {
    return "{next-index:" + iNextIndex + ",feeds:" + iFeeds + "}";
  }
}
