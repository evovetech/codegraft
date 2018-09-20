package codegraft.inject.extension.realm;

import dagger.Binds;
import dagger.Module;

@Module(
    includes = RealmModule.class
)
public interface RealmComponent_Module {
  @Binds
  RealmComponent bindRealmComponent(RealmComponent_Implementation realmComponent_Implementation);
}
