package evovetech.sample;

import android.app.Application;
import codegraft.inject.BaseComponent_BootData;
import codegraft.inject.PluginModule;
import codegraft.inject.android.AndroidInjectActivityModule;
import codegraft.inject.android.AndroidInjectApplicationModule;
import codegraft.inject.android.AndroidInjectSupportFragmentModule;
import codegraft.inject.android.ViewModelComponent_Module;
import codegraft.inject.extension.crashlytics.CrashesComponent_BootData;
import codegraft.inject.extension.crashlytics.CrashesComponent_Module;
import codegraft.inject.extension.okhttp3.OkhttpComponent_BootData;
import codegraft.inject.extension.okhttp3.OkhttpComponent_Module;
import codegraft.inject.extension.realm.RealmComponent_BootData;
import codegraft.inject.extension.realm.RealmComponent_Module;
import dagger.Module;
import dagger.Provides;
import evovetech.blog.medium.MediumComponent_Module;
import evovetech.finance.plaid.PlaidComponent_Module;
import io.fabric.sdk.android.Fabric;
import io.realm.RealmConfiguration;
import javax.inject.Inject;
import javax.inject.Singleton;
import okhttp3.OkHttpClient;

@Module(
    includes = {
        RealmComponent_Module.class,
        OkhttpComponent_Module.class,
        PlaidComponent_Module.class,
        MediumComponent_Module.class,
        CrashesComponent_Module.class,
        AndroidInjectApplicationModule.class,
        ViewModelComponent_Module.class,
        AndroidInjectActivityModule.class,
        AndroidInjectSupportFragmentModule.class,
        PluginModule.class
    }
)
class AppComponent_BootData {
  private final RealmComponent_BootData realmComponent_BootData;

  private final OkhttpComponent_BootData okhttpComponent_BootData;

  private final CrashesComponent_BootData crashesComponent_BootData;

  private final BaseComponent_BootData baseComponent_BootData;

  @Inject
  AppComponent_BootData(RealmComponent_BootData realmComponent_BootData,
      OkhttpComponent_BootData okhttpComponent_BootData,
      CrashesComponent_BootData crashesComponent_BootData,
      BaseComponent_BootData baseComponent_BootData) {
    this.realmComponent_BootData = realmComponent_BootData;
    this.okhttpComponent_BootData = okhttpComponent_BootData;
    this.crashesComponent_BootData = crashesComponent_BootData;
    this.baseComponent_BootData = baseComponent_BootData;
  }

  @Provides
  @Singleton
  RealmConfiguration getRealmConfiguration() {
    return realmComponent_BootData.getRealmConfiguration();
  }

  @Provides
  @Singleton
  Application getApplication() {
    return realmComponent_BootData.getApplication();
  }

  @Provides
  @Singleton
  OkHttpClient getOkHttpClient() {
    return okhttpComponent_BootData.getOkHttpClient();
  }

  @Provides
  @Singleton
  Fabric getFabric() {
    return crashesComponent_BootData.getFabric();
  }
}
