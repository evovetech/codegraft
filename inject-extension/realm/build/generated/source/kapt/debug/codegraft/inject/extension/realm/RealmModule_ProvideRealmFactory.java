// Generated by Dagger (https://google.github.io/dagger).
package codegraft.inject.extension.realm;

import dagger.internal.Factory;
import dagger.internal.Preconditions;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import javax.inject.Provider;

public final class RealmModule_ProvideRealmFactory implements Factory<Realm> {
  private final RealmModule module;

  private final Provider<RealmConfiguration> configProvider;

  public RealmModule_ProvideRealmFactory(
      RealmModule module, Provider<RealmConfiguration> configProvider) {
    this.module = module;
    this.configProvider = configProvider;
  }

  @Override
  public Realm get() {
    return Preconditions.checkNotNull(
        module.provideRealm(configProvider.get()),
        "Cannot return null from a non-@Nullable @Provides method");
  }

  public static RealmModule_ProvideRealmFactory create(
      RealmModule module, Provider<RealmConfiguration> configProvider) {
    return new RealmModule_ProvideRealmFactory(module, configProvider);
  }

  public static Realm proxyProvideRealm(RealmModule instance, RealmConfiguration config) {
    return Preconditions.checkNotNull(
        instance.provideRealm(config), "Cannot return null from a non-@Nullable @Provides method");
  }
}