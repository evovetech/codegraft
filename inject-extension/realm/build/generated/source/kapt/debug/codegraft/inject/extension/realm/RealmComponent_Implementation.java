package codegraft.inject.extension.realm;

import io.realm.Realm;
import java.lang.Override;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public final class RealmComponent_Implementation implements RealmComponent {
  private final Provider<Realm> realmProvider;

  @Inject
  RealmComponent_Implementation(Provider<Realm> realmProvider) {
    this.realmProvider = realmProvider;
  }

  @Override
  public Realm getRealm() {
    return realmProvider.get();
  }
}
