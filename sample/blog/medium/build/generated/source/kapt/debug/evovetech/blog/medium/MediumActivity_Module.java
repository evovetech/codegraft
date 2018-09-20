package evovetech.blog.medium;

import codegraft.inject.android.ActivityScope;
import codegraft.inject.android.AndroidInjectActivityModule;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module(
    includes = AndroidInjectActivityModule.class
)
public interface MediumActivity_Module {
  @ContributesAndroidInjector
  @ActivityScope
  MediumActivity contributeMediumActivity();
}
