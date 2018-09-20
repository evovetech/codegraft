package codegraft.inject.extension.okhttp3;

import android.app.Application;
import javax.inject.Inject;
import okhttp3.OkHttpClient;

public final class OkhttpComponent_BootData {
  private final OkHttpClient okHttpClient;

  private final Application application;

  @Inject
  OkhttpComponent_BootData(OkHttpClient okHttpClient, Application application) {
    this.okHttpClient = okHttpClient;
    this.application = application;
  }

  public final OkHttpClient getOkHttpClient() {
    return okHttpClient;
  }

  public final Application getApplication() {
    return application;
  }
}
