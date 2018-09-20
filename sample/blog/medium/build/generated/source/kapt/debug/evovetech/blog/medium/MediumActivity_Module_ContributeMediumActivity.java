package evovetech.blog.medium;

import android.app.Activity;
import codegraft.inject.android.ActivityScope;
import dagger.Binds;
import dagger.Module;
import dagger.Subcomponent;
import dagger.android.ActivityKey;
import dagger.android.AndroidInjector;
import dagger.multibindings.IntoMap;

@Module(
  subcomponents = MediumActivity_Module_ContributeMediumActivity.MediumActivitySubcomponent.class
)
public abstract class MediumActivity_Module_ContributeMediumActivity {
  private MediumActivity_Module_ContributeMediumActivity() {}

  @Binds
  @IntoMap
  @ActivityKey(MediumActivity.class)
  abstract AndroidInjector.Factory<? extends Activity> bindAndroidInjectorFactory(
      MediumActivitySubcomponent.Builder builder);

  @Subcomponent
  @ActivityScope
  public interface MediumActivitySubcomponent extends AndroidInjector<MediumActivity> {
    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<MediumActivity> {}
  }
}
