package codegraft.inject.extension.okhttp3;

import java.lang.Override;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public final class OkhttpComponent_Implementation implements OkhttpComponent {
  private final Provider<Okhttp> okhttpProvider;

  @Inject
  OkhttpComponent_Implementation(Provider<Okhttp> okhttpProvider) {
    this.okhttpProvider = okhttpProvider;
  }

  @Override
  public Okhttp getOkhttp() {
    return okhttpProvider.get();
  }
}
