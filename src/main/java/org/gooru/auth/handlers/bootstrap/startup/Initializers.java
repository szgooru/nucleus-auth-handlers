package org.gooru.auth.handlers.bootstrap.startup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.gooru.auth.handlers.infra.ConfigRegistry;
import org.gooru.auth.handlers.infra.DataSourceRegistry;
import org.gooru.auth.handlers.infra.RedisClient;

public class Initializers implements Iterable<Initializer> {
  private Iterator<Initializer> internalIterator;

  public Initializers() {
    final List<Initializer> initializers = new ArrayList<Initializer>();
    initializers.add(DataSourceRegistry.getInstance());
    initializers.add(RedisClient.instance());
    initializers.add(ConfigRegistry.instance());
    internalIterator = initializers.iterator();
  }

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

}