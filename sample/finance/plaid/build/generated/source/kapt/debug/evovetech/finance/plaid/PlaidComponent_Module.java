package evovetech.finance.plaid;

import dagger.Binds;
import dagger.Module;

@Module(
    includes = PlaidModule.class
)
public interface PlaidComponent_Module {
  @Binds
  PlaidComponent bindPlaidComponent(PlaidComponent_Implementation plaidComponent_Implementation);
}
