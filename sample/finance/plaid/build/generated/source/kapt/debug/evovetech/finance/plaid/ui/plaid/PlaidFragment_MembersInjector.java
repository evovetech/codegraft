// Generated by Dagger (https://google.github.io/dagger).
package evovetech.finance.plaid.ui.plaid;

import codegraft.inject.android.ViewModelInstanceProvider;
import dagger.MembersInjector;
import javax.inject.Provider;

public final class PlaidFragment_MembersInjector implements MembersInjector<PlaidFragment> {
  private final Provider<ViewModelInstanceProvider> viewModelsProvider;

  public PlaidFragment_MembersInjector(Provider<ViewModelInstanceProvider> viewModelsProvider) {
    this.viewModelsProvider = viewModelsProvider;
  }

  public static MembersInjector<PlaidFragment> create(
      Provider<ViewModelInstanceProvider> viewModelsProvider) {
    return new PlaidFragment_MembersInjector(viewModelsProvider);
  }

  @Override
  public void injectMembers(PlaidFragment instance) {
    injectViewModels(instance, viewModelsProvider.get());
  }

  public static void injectViewModels(
      PlaidFragment instance, ViewModelInstanceProvider viewModels) {
    instance.viewModels = viewModels;
  }
}
