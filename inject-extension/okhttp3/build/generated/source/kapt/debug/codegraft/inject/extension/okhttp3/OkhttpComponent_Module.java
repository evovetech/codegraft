package codegraft.inject.extension.okhttp3;

import dagger.Binds;
import dagger.Module;

@Module(
    includes = OkhttpModule.class
)
public interface OkhttpComponent_Module {
  @Binds
  OkhttpComponent bindOkhttpComponent(
      OkhttpComponent_Implementation okhttpComponent_Implementation);
}
