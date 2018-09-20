package evovetech.sample;

import android.app.Application;
import codegraft.inject.BootScope;
import codegraft.inject.extension.crashlytics.Crashes;
import codegraft.inject.extension.crashlytics.CrashesBootstrapModule;
import codegraft.inject.extension.okhttp3.OkhttpBootstrapModule;
import codegraft.inject.extension.okhttp3.OkhttpModule;
import codegraft.inject.extension.realm.RealmBootstrapModule;
import codegraft.inject.extension.realm.RealmModule;
import dagger.BindsInstance;
import dagger.Component;
import evovetech.blog.medium.MediumModule;
import evovetech.finance.plaid.PlaidModule;
import io.fabric.sdk.android.Fabric;
import io.realm.RealmConfiguration;
import javax.inject.Named;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.Nullable;

@BootScope
@Component(
    modules = BootModule.class
)
public interface BootComponent {
  AppComponent getAppComponent();

  @Component.Builder
  interface Builder {
    @BindsInstance
    Builder application(Application application);

    @BindsInstance
    Builder realmConfigurationBuilderFunction1(
        @Named("realmInit") Function1<? super RealmConfiguration.Builder, ? extends RealmConfiguration> realmConfigurationBuilderFunction1);

    @BindsInstance
    Builder okHttpClientApplicationBuilderFunction2(
        @Named("okhttp") Function2<? super OkHttpClient.Builder, ? super Application, ? extends OkHttpClient> okHttpClientApplicationBuilderFunction2);

    @BindsInstance
    Builder fabricBuilderFunction1(
        @Named("fabric") Function1<? super Fabric.Builder, ? extends Fabric> fabricBuilderFunction1);

    @BindsInstance
    Builder realmModule(@Nullable RealmModule realmModule);

    @BindsInstance
    Builder okhttpModule(@Nullable OkhttpModule okhttpModule);

    @BindsInstance
    Builder plaidModule(@Nullable PlaidModule plaidModule);

    @BindsInstance
    Builder mediumModule(@Nullable MediumModule mediumModule);

    @BindsInstance
    Builder crashes(@Nullable Crashes crashes);

    Builder realmBootstrapModule(RealmBootstrapModule realmBootstrapModule);

    Builder okhttpBootstrapModule(OkhttpBootstrapModule okhttpBootstrapModule);

    Builder crashesBootstrapModule(CrashesBootstrapModule crashesBootstrapModule);

    BootComponent build();
  }
}
