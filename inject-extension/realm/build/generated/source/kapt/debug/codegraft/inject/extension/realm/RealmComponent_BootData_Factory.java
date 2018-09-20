// Generated by Dagger (https://google.github.io/dagger).
package codegraft.inject.extension.realm;

import android.app.Application;
import dagger.internal.Factory;
import io.realm.RealmConfiguration;
import javax.inject.Provider;

public final class RealmComponent_BootData_Factory implements Factory<RealmComponent_BootData> {
  private final Provider<RealmConfiguration> realmConfigurationProvider;

  private final Provider<Application> applicationProvider;

  public RealmComponent_BootData_Factory(
      Provider<RealmConfiguration> realmConfigurationProvider,
      Provider<Application> applicationProvider) {
    this.realmConfigurationProvider = realmConfigurationProvider;
    this.applicationProvider = applicationProvider;
  }

  @Override
  public RealmComponent_BootData get() {
    return new RealmComponent_BootData(realmConfigurationProvider.get(), applicationProvider.get());
  }

  public static RealmComponent_BootData_Factory create(
      Provider<RealmConfiguration> realmConfigurationProvider,
      Provider<Application> applicationProvider) {
    return new RealmComponent_BootData_Factory(realmConfigurationProvider, applicationProvider);
  }

  public static RealmComponent_BootData newRealmComponent_BootData(
      RealmConfiguration realmConfiguration, Application application) {
    return new RealmComponent_BootData(realmConfiguration, application);
  }
}