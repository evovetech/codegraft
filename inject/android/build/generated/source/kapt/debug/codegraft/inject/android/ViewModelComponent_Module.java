package codegraft.inject.android;

import dagger.Binds;
import dagger.Module;

@Module(
    includes = ViewModelModule.class
)
public interface ViewModelComponent_Module {
  @Binds
  ViewModelComponent bindViewModelComponent(
      ViewModelComponent_Implementation viewModelComponent_Implementation);
}
