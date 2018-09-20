package evovetech.finance.plaid;

import java.lang.Override;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public final class PlaidComponent_Implementation implements PlaidComponent {
  private final Provider<PlaidClient> plaidClientProvider;

  @Inject
  PlaidComponent_Implementation(Provider<PlaidClient> plaidClientProvider) {
    this.plaidClientProvider = plaidClientProvider;
  }

  @Override
  public PlaidClient getClient() {
    return plaidClientProvider.get();
  }
}
