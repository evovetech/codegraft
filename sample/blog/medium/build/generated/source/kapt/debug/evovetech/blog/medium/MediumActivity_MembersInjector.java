// Generated by Dagger (https://google.github.io/dagger).
package evovetech.blog.medium;

import android.support.v4.app.Fragment;
import dagger.MembersInjector;
import dagger.android.AndroidInjector;
import javax.inject.Provider;

public final class MediumActivity_MembersInjector implements MembersInjector<MediumActivity> {
  private final Provider<AndroidInjector<Fragment>> supportFragmentInjectorProvider;

  public MediumActivity_MembersInjector(
      Provider<AndroidInjector<Fragment>> supportFragmentInjectorProvider) {
    this.supportFragmentInjectorProvider = supportFragmentInjectorProvider;
  }

  public static MembersInjector<MediumActivity> create(
      Provider<AndroidInjector<Fragment>> supportFragmentInjectorProvider) {
    return new MediumActivity_MembersInjector(supportFragmentInjectorProvider);
  }

  @Override
  public void injectMembers(MediumActivity instance) {
    injectSupportFragmentInjector(instance, supportFragmentInjectorProvider.get());
  }

  public static void injectSupportFragmentInjector(
      MediumActivity instance, AndroidInjector<Fragment> supportFragmentInjector) {
    instance.supportFragmentInjector = supportFragmentInjector;
  }
}