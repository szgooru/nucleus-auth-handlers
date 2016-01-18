package org.gooru.auth.handlers.bootstrap.shutdown;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.gooru.auth.handlers.infra.DataSourceRegistry;
import org.gooru.auth.handlers.infra.RedisClient;

public class Finalizers implements Iterable<Finalizer> {

  private Iterator<Finalizer> internalIterator;

  public Finalizers() {
    final List<Finalizer> finalizers = new ArrayList<Finalizer>();
    finalizers.add(DataSourceRegistry.getInstance());
    finalizers.add(RedisClient.instance());
    internalIterator = finalizers.iterator();
  }

  @Override
  public Iterator<Finalizer> iterator() {
    Iterator<Finalizer> iterator = new Iterator<Finalizer>() {

      @Override
      public boolean hasNext() {
        return internalIterator.hasNext();
      }

      @Override
      public Finalizer next() {
        return internalIterator.next();
      }

    };
    return iterator;
  }

}
