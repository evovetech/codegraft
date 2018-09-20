package evovetech.blog.medium;

import dagger.Binds;
import dagger.Module;

@Module(
    includes = MediumModule.class
)
public interface MediumComponent_Module {
  @Binds
  MediumComponent bindMediumComponent(
      MediumComponent_Implementation mediumComponent_Implementation);
}
