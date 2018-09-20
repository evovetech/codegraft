package evovetech.sample.instant;

import codegraft.inject.android.ActivityScope;
import codegraft.inject.android.AndroidInjectActivityModule;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module(
    includes = AndroidInjectActivityModule.class
)
public interface MainActivity_Module {
  @ContributesAndroidInjector
  @ActivityScope
  MainActivity contributeMainActivity();
}
