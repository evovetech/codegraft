package codegraft.inject.extension.crashlytics;

import dagger.Binds;
import dagger.Module;

@Module(
    includes = Crashes.class
)
public interface CrashesComponent_Module {
  @Binds
  CrashesComponent bindCrashesComponent(
      CrashesComponent_Implementation crashesComponent_Implementation);
}
