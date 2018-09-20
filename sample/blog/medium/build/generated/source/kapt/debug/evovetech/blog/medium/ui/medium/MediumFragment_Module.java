package evovetech.blog.medium.ui.medium;

import codegraft.inject.android.ActivityScope;
import codegraft.inject.android.AndroidInjectSupportFragmentModule;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module(
    includes = AndroidInjectSupportFragmentModule.class
)
public interface MediumFragment_Module {
  @ContributesAndroidInjector
  @ActivityScope
  MediumFragment contributeMediumFragment();
}
