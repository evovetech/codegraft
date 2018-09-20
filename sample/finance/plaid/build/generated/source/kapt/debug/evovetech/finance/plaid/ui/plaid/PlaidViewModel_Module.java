package evovetech.finance.plaid.ui.plaid;

import android.arch.lifecycle.ViewModel;
import codegraft.inject.android.ViewModelKey;
import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public interface PlaidViewModel_Module {
  @Binds
  @IntoMap
  @ViewModelKey(PlaidViewModel.class)
  ViewModel bindViewModel(PlaidViewModel plaidViewModel);
}
