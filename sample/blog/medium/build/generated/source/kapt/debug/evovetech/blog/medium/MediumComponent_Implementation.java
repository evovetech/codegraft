package evovetech.blog.medium;

import java.lang.Override;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public final class MediumComponent_Implementation implements MediumComponent {
  private final Provider<MediumClient> mediumClientProvider;

  private final Provider<MediumUserComponent.Builder> builderProvider;

  @Inject
  MediumComponent_Implementation(Provider<MediumClient> mediumClientProvider,
      Provider<MediumUserComponent.Builder> builderProvider) {
    this.mediumClientProvider = mediumClientProvider;
    this.builderProvider = builderProvider;
  }

  @Override
  public MediumClient getClient() {
    return mediumClientProvider.get();
  }

  @Override
  public MediumUserComponent.Builder newUser() {
    return builderProvider.get();
  }
}
