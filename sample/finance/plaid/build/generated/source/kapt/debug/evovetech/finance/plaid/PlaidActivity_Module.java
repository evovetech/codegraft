package evovetech.finance.plaid;

import codegraft.inject.android.ActivityScope;
import codegraft.inject.android.AndroidInjectActivityModule;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module(
    includes = AndroidInjectActivityModule.class
)
public interface PlaidActivity_Module {
  @ContributesAndroidInjector
  @ActivityScope
  PlaidActivity contributePlaidActivity();
}
