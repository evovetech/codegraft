package codegraft.inject.android;

import java.lang.Override;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public final class ViewModelComponent_Implementation implements ViewModelComponent {
  private final Provider<ViewModels> viewModelsProvider;

  @Inject
  ViewModelComponent_Implementation(Provider<ViewModels> viewModelsProvider) {
    this.viewModelsProvider = viewModelsProvider;
  }

  @Override
  public ViewModels getViewModels() {
    return viewModelsProvider.get();
  }
}
