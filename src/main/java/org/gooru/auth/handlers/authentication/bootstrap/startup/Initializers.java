package org.gooru.auth.handlers.authentication.bootstrap.startup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.gooru.auth.handlers.authentication.infra.DataSourceRegistry;
import org.gooru.auth.handlers.authentication.infra.RedisClient;

public class Initializers implements Iterable<Initializer> {
  private List<Initializer> initializers = null;
  private Iterator<Initializer> internalIterator;

  @Override
  public Iterator<Initializer> iterator() {
    Iterator<Initializer> iterator = new Iterator<Initializer>() {

      @Override
      public boolean hasNext() {
        return internalIterator.hasNext();
      }

      @Override
      public Initializer next() {
        return internalIterator.next();
      }

    };
    return iterator;
  }

  public Initializers() {
    initializers = new ArrayList<Initializer>();
    initializers.add(DataSourceRegistry.getInstance());
    initializers.add(RedisClient.getInstance());
    internalIterator = initializers.iterator();
  }

}