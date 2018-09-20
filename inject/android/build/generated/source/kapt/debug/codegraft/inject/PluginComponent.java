package codegraft.inject;

@BootstrapComponent(
    applicationModules = PluginModule.class,
    autoInclude = false,
    flatten = true
)
public interface PluginComponent {
  Plugins getPlugins();
}
