package codegraft.inject.extension.okhttp3;

import codegraft.inject.Plugin;
import codegraft.inject.PluginKey;
import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public interface Okhttp_Module {
  @Binds
  @IntoMap
  @PluginKey(Okhttp.class)
  Plugin bindPlugin(Okhttp okhttp);
}
