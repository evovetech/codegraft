// Generated by Dagger (https://google.github.io/dagger).
package codegraft.inject.extension.okhttp3;

import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.inject.Provider;
import okhttp3.OkHttpClient;

public final class OkhttpModule_ProvideDefaultOkhttpBuilderFactory
    implements Factory<OkHttpClient.Builder> {
  private final OkhttpModule module;

  private final Provider<OkHttpClient> okhttpProvider;

  public OkhttpModule_ProvideDefaultOkhttpBuilderFactory(
      OkhttpModule module, Provider<OkHttpClient> okhttpProvider) {
    this.module = module;
    this.okhttpProvider = okhttpProvider;
  }

  @Override
  public OkHttpClient.Builder get() {
    return Preconditions.checkNotNull(
        module.provideDefaultOkhttpBuilder(okhttpProvider.get()),
        "Cannot return null from a non-@Nullable @Provides method");
  }

  public static OkhttpModule_ProvideDefaultOkhttpBuilderFactory create(
      OkhttpModule module, Provider<OkHttpClient> okhttpProvider) {
    return new OkhttpModule_ProvideDefaultOkhttpBuilderFactory(module, okhttpProvider);
  }

  public static OkHttpClient.Builder proxyProvideDefaultOkhttpBuilder(
      OkhttpModule instance, OkHttpClient okhttp) {
    return Preconditions.checkNotNull(
        instance.provideDefaultOkhttpBuilder(okhttp),
        "Cannot return null from a non-@Nullable @Provides method");
  }
}