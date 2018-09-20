// Generated by Dagger (https://google.github.io/dagger).
package evovetech.blog.medium;

import dagger.internal.Factory;
import javax.inject.Provider;

public final class MediumClient_Factory implements Factory<MediumClient> {
  private final Provider<MediumService> serviceProvider;

  public MediumClient_Factory(Provider<MediumService> serviceProvider) {
    this.serviceProvider = serviceProvider;
  }

  @Override
  public MediumClient get() {
    return new MediumClient(serviceProvider.get());
  }

  public static MediumClient_Factory create(Provider<MediumService> serviceProvider) {
    return new MediumClient_Factory(serviceProvider);
  }
}