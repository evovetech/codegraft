package evovetech.sample;

import android.app.Application;
import codegraft.inject.android.AndroidInjectApplicationModule;
import codegraft.inject.android.ApplicationKey;
import dagger.Binds;
import dagger.Module;
import dagger.android.AndroidInjector;
import dagger.multibindings.IntoMap;

@Module(
    subcomponents = App_Module.Subcomponent.class,
    includes = AndroidInjectApplicationModule.class
)
public interface App_Module {
  @Binds
  @IntoMap
  @ApplicationKey(App.class)
  AndroidInjector.Factory<? extends Application> bindAppInjectorFactory(
      Subcomponent.Builder builder);

  @dagger.Subcomponent
  interface Subcomponent extends AndroidInjector<App> {
    @dagger.Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<App> {
      public abstract Subcomponent build();
    }
  }
}
