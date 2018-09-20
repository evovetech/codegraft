package codegraft.inject.extension.realm;

import android.app.Application;
import io.realm.RealmConfiguration;
import javax.inject.Inject;

public final class RealmComponent_BootData {
  private final RealmConfiguration realmConfiguration;

  private final Application application;

  @Inject
  RealmComponent_BootData(RealmConfiguration realmConfiguration, Application application) {
    this.realmConfiguration = realmConfiguration;
    this.application = application;
  }

  public final RealmConfiguration getRealmConfiguration() {
    return realmConfiguration;
  }

  public final Application getApplication() {
    return application;
  }
}
