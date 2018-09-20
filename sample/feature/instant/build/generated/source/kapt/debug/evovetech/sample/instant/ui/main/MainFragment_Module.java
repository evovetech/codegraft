package evovetech.sample.instant.ui.main;

import codegraft.inject.android.ActivityScope;
import codegraft.inject.android.AndroidInjectSupportFragmentModule;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module(
    includes = AndroidInjectSupportFragmentModule.class
)
public interface MainFragment_Module {
  @ContributesAndroidInjector
  @ActivityScope
  MainFragment contributeMainFragment();
}
