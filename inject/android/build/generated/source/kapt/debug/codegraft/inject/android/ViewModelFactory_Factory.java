// Generated by Dagger (https://google.github.io/dagger).
package codegraft.inject.android;

import dagger.internal.Factory;
import javax.inject.Provider;

public final class ViewModelFactory_Factory implements Factory<ViewModelFactory> {
  private final Provider<ViewModels> viewModelsProvider;

  public ViewModelFactory_Factory(Provider<ViewModels> viewModelsProvider) {
    this.viewModelsProvider = viewModelsProvider;
  }

  @Override
  public ViewModelFactory get() {
    return new ViewModelFactory(viewModelsProvider.get());
  }

  public static ViewModelFactory_Factory create(Provider<ViewModels> viewModelsProvider) {
    return new ViewModelFactory_Factory(viewModelsProvider);
  }

  public static ViewModelFactory newViewModelFactory(ViewModels viewModels) {
    return new ViewModelFactory(viewModels);
  }
}
