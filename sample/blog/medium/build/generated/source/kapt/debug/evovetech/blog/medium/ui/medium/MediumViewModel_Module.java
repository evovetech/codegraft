package evovetech.blog.medium.ui.medium;

import android.arch.lifecycle.ViewModel;
import codegraft.inject.android.ViewModelKey;
import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
public interface MediumViewModel_Module {
  @Binds
  @IntoMap
  @ViewModelKey(MediumViewModel.class)
  ViewModel bindViewModel(MediumViewModel mediumViewModel);
}
