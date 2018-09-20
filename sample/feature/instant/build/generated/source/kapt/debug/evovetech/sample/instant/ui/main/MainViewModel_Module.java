package evovetech.sample.instant.ui.main;

import android.arch.lifecycle.ViewModel;
import codegraft.inject.android.ViewModelKey;
import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public interface MainViewModel_Module {
  @Binds
  @IntoMap
  @ViewModelKey(MainViewModel.class)
  ViewModel bindViewModel(MainViewModel mainViewModel);
}
