package evovetech.sample;

import codegraft.inject.AppModule;
import codegraft.inject.BootScope;
import codegraft.inject.extension.crashlytics.Crashes;
import codegraft.inject.extension.crashlytics.CrashesBootstrapModule;
import codegraft.inject.extension.okhttp3.OkhttpBootstrapModule;
import codegraft.inject.extension.okhttp3.OkhttpModule;
import codegraft.inject.extension.realm.RealmBootstrapModule;
import codegraft.inject.extension.realm.RealmModule;
import dagger.Module;
import dagger.Provides;
import evovetech.blog.medium.MediumModule;
import evovetech.finance.plaid.PlaidModule;
import org.jetbrains.annotations.Nullable;

@Module(
    includes = {
        RealmBootstrapModule.class,
        OkhttpBootstrapModule.class,
        CrashesBootstrapModule.class,
        AppModule.class
    }
)
final class BootModule {
  @Provides
  @BootScope
  AppComponent provideComponent(@Nullable RealmModule realmModule,
      @Nullable OkhttpModule okhttpModule, @Nullable PlaidModule plaidModule,
      @Nullable MediumModule mediumModule, @Nullable Crashes crashes,
      AppComponent_BootData bootData) {
    AppComponent.Builder builder = DaggerAppComponent.builder();
    if (realmModule != null) {
      builder.realmModule(realmModule);
    }
    if (okhttpModule != null) {
      builder.okhttpModule(okhttpModule);
    }
    if (plaidModule != null) {
      builder.plaidModule(plaidModule);
    }
    if (mediumModule != null) {
      builder.mediumModule(mediumModule);
    }
    if (crashes != null) {
      builder.crashes(crashes);
    }
    builder.bootData(bootData);
    return builder.build();
  }
}
