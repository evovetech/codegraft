package evovetech.blog.medium.ui.medium;

import android.support.v4.app.Fragment;
import codegraft.inject.android.ActivityScope;
import dagger.Binds;
import dagger.Module;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;
import dagger.android.support.FragmentKey;
import dagger.multibindings.IntoMap;

@Module(
  subcomponents = MediumFragment_Module_ContributeMediumFragment.MediumFragmentSubcomponent.class
)
public abstract class MediumFragment_Module_ContributeMediumFragment {
  private MediumFragment_Module_ContributeMediumFragment() {}

  @Binds
  @IntoMap
  @FragmentKey(MediumFragment.class)
  abstract AndroidInjector.Factory<? extends Fragment> bindAndroidInjectorFactory(
      MediumFragmentSubcomponent.Builder builder);

  @Subcomponent
  @ActivityScope
  public interface MediumFragmentSubcomponent extends AndroidInjector<MediumFragment> {
    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<MediumFragment> {}
  }
}
