package codegraft.inject;

import java.lang.Class;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.jetbrains.annotations.NotNull;

@Singleton
public final class Plugins extends ClassKeyProviderMap<Plugin> {
  @Inject
  Plugins(@NotNull Map<Class<? extends Plugin>, Provider<Plugin>> providers) {
    super(providers);
  }
}
