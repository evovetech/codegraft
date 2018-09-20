package codegraft.inject.extension.crashlytics;

import android.app.Application;
import io.fabric.sdk.android.Fabric;
import javax.inject.Inject;

public final class CrashesComponent_BootData {
  private final Fabric fabric;

  private final Application application;

  @Inject
  CrashesComponent_BootData(Fabric fabric, Application application) {
    this.fabric = fabric;
    this.application = application;
  }

  public final Fabric getFabric() {
    return fabric;
  }

  public final Application getApplication() {
    return application;
  }
}
