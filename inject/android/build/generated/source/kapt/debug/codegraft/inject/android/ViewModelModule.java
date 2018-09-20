package codegraft.inject.android;

import android.arch.lifecycle.ViewModel;
import dagger.Module;
import dagger.multibindings.Multibinds;
import java.lang.Class;
import java.util.Map;

@Module
public interface ViewModelModule {
  @Multibinds
  Map<Class<? extends ViewModel>, ViewModel> bindViewModels();
}
