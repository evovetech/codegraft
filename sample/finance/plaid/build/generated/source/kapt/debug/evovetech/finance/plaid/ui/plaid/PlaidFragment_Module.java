package evovetech.finance.plaid.ui.plaid;

import codegraft.inject.android.ActivityScope;
import codegraft.inject.android.AndroidInjectSupportFragmentModule;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module(
    includes = AndroidInjectSupportFragmentModule.class
)
public interface PlaidFragment_Module {
  @ContributesAndroidInjector
  @ActivityScope
  PlaidFragment contributePlaidFragment();
}
