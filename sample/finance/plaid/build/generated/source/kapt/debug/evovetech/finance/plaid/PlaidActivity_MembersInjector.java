// Generated by Dagger (https://google.github.io/dagger).
package evovetech.finance.plaid;

import android.support.v4.app.Fragment;
import dagger.MembersInjector;
import dagger.android.AndroidInjector;
import javax.inject.Provider;

public final class PlaidActivity_MembersInjector implements MembersInjector<PlaidActivity> {
  private final Provider<AndroidInjector<Fragment>> supportFragmentInjectorProvider;

  public PlaidActivity_MembersInjector(
      Provider<AndroidInjector<Fragment>> supportFragmentInjectorProvider) {
    this.supportFragmentInjectorProvider = supportFragmentInjectorProvider;
  }

  public static MembersInjector<PlaidActivity> create(
      Provider<AndroidInjector<Fragment>> supportFragmentInjectorProvider) {
    return new PlaidActivity_MembersInjector(supportFragmentInjectorProvider);
  }

  @Override
  public void injectMembers(PlaidActivity instance) {
    injectSupportFragmentInjector(instance, supportFragmentInjectorProvider.get());
  }

  public static void injectSupportFragmentInjector(
      PlaidActivity instance, AndroidInjector<Fragment> supportFragmentInjector) {
    instance.supportFragmentInjector = supportFragmentInjector;
  }
}