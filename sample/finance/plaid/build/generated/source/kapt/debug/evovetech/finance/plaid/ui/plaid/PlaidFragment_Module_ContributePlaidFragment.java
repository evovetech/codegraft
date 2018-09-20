package evovetech.finance.plaid.ui.plaid;

import android.support.v4.app.Fragment;
import codegraft.inject.android.ActivityScope;
import dagger.Binds;
import dagger.Module;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;
import dagger.android.support.FragmentKey;
import dagger.multibindings.IntoMap;

@Module(
  subcomponents = PlaidFragment_Module_ContributePlaidFragment.PlaidFragmentSubcomponent.class
)
public abstract class PlaidFragment_Module_ContributePlaidFragment {
  private PlaidFragment_Module_ContributePlaidFragment() {}

  @Binds
  @IntoMap
  @FragmentKey(PlaidFragment.class)
  abstract AndroidInjector.Factory<? extends Fragment> bindAndroidInjectorFactory(
      PlaidFragmentSubcomponent.Builder builder);

  @Subcomponent
  @ActivityScope
  public interface PlaidFragmentSubcomponent extends AndroidInjector<PlaidFragment> {
    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<PlaidFragment> {}
  }
}
