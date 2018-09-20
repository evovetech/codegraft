package codegraft.inject.android;

import codegraft.inject.BootstrapComponent;

@BootstrapComponent(
    applicationModules = ViewModelModule.class,
    autoInclude = false,
    flatten = false
)
public interface ViewModelComponent {
  ViewModels getViewModels();
}
