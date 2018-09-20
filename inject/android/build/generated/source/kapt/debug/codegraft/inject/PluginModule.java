package codegraft.inject;

import dagger.Module;
import dagger.multibindings.Multibinds;
import java.lang.Class;
import java.util.Map;
import javax.inject.Singleton;

@Module
public interface PluginModule {
  @Multibinds
  @Singleton
  Map<Class<? extends Plugin>, Plugin> bindPlugins();
}
