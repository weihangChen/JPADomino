package persistence.proxy;


import java.util.Map;
import java.util.Set;

public abstract interface EnhancedEntity
{
  public abstract Map<String, Set<String>> getForeignKeysMap();

  public abstract Object getEntity();

  public abstract String getId();
}

/* Location:           C:\Users\SWECWI\Desktop\SECRET WEAPON\Kundera\kundera-mongo\kundera-mongo-2.0.6-jar-with-dependencies.jar
 * Qualified Name:     com.impetus.kundera.proxy.EnhancedEntity
 * Java Class Version: 6 (50.0)
 * JD-Core Version:    0.5.3
 */
