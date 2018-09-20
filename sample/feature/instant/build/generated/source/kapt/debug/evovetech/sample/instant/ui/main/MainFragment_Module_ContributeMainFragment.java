package evovetech.sample.instant.ui.main;

import android.support.v4.app.Fragment;
import codegraft.inject.android.ActivityScope;
import dagger.Binds;
import dagger.Module;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;
import dagger.android.support.FragmentKey;
import dagger.multibindings.IntoMap;

@Module(subcomponents = MainFragment_Module_ContributeMainFragment.MainFragmentSubcomponent.class)
public abstract class MainFragment_Module_ContributeMainFragment {
  private MainFragment_Module_ContributeMainFragment() {}

  @Binds
  @IntoMap
  @FragmentKey(MainFragment.class)
  abstract AndroidInjector.Factory<? extends Fragment> bindAndroidInjectorFactory(
      MainFragmentSubcomponent.Builder builder);

  @Subcomponent
  @ActivityScope
  public interface MainFragmentSubcomponent extends AndroidInjector<MainFragment> {
    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<MainFragment> {}
  }
}
