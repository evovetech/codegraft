package evovetech.finance.plaid;

import android.app.Activity;
import codegraft.inject.android.ActivityScope;
import dagger.Binds;
import dagger.Module;
import dagger.Subcomponent;
import dagger.android.ActivityKey;
import dagger.android.AndroidInjector;
import dagger.multibindings.IntoMap;

@Module(
  subcomponents = PlaidActivity_Module_ContributePlaidActivity.PlaidActivitySubcomponent.class
)
public abstract class PlaidActivity_Module_ContributePlaidActivity {
  private PlaidActivity_Module_ContributePlaidActivity() {}

  @Binds
  @IntoMap
  @ActivityKey(PlaidActivity.class)
  abstract AndroidInjector.Factory<? extends Activity> bindAndroidInjectorFactory(
      PlaidActivitySubcomponent.Builder builder);

  @Subcomponent
  @ActivityScope
  public interface PlaidActivitySubcomponent extends AndroidInjector<PlaidActivity> {
    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<PlaidActivity> {}
  }
}
