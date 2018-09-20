package codegraft.inject.extension.crashlytics;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;
import java.lang.Override;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public final class CrashesComponent_Implementation implements CrashesComponent {
  private final Provider<Fabric> fabricProvider;

  private final Provider<Crashlytics> crashlyticsProvider;

  @Inject
  CrashesComponent_Implementation(Provider<Fabric> fabricProvider,
      Provider<Crashlytics> crashlyticsProvider) {
    this.fabricProvider = fabricProvider;
    this.crashlyticsProvider = crashlyticsProvider;
  }

  @Override
  public Fabric getFabric() {
    return fabricProvider.get();
  }

  @Override
  public Crashlytics getCrashlytics() {
    return crashlyticsProvider.get();
  }
}
