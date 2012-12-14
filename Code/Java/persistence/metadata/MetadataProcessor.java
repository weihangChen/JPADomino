package persistence.metadata;

import persistence.metadata.model.EntityMetadata;

public abstract interface MetadataProcessor
{
  public abstract void process(Class<?> paramClass, EntityMetadata paramEntityMetadata);
}
