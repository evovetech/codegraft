// Generated by Dagger (https://google.github.io/dagger).
package codegraft.inject.android;

import dagger.internal.Factory;
import javax.inject.Provider;

public final class ViewModelInstanceProvider_Factory implements Factory<ViewModelInstanceProvider> {
  private final Provider<ViewModelFactory> factoryProvider;

  public ViewModelInstanceProvider_Factory(Provider<ViewModelFactory> factoryProvider) {
    this.factoryProvider = factoryProvider;
  }

  @Override
  public ViewModelInstanceProvider get() {
    return new ViewModelInstanceProvider(factoryProvider.get());
  }

  public static ViewModelInstanceProvider_Factory create(
      Provider<ViewModelFactory> factoryProvider) {
    return new ViewModelInstanceProvider_Factory(factoryProvider);
  }
}
