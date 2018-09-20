package codegraft.inject;

import android.app.Application;
import javax.inject.Inject;

public final class BaseComponent_BootData {
  private final Application application;

  @Inject
  BaseComponent_BootData(Application application) {
    this.application = application;
  }

  public final Application getApplication() {
    return application;
  }
}
