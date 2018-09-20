package evovetech.sample;

import android.app.Activity;
import android.app.Application;
import android.support.v4.app.Fragment;
import codegraft.inject.BaseComponent;
import codegraft.inject.Plugins;
import codegraft.inject.android.ActivityInjectorComponent;
import codegraft.inject.android.ApplicationInjectorComponent;
import codegraft.inject.android.SupportFragmentInjectorComponent;
import codegraft.inject.android.ViewModelComponent;
import codegraft.inject.extension.crashlytics.Crashes;
import codegraft.inject.extension.crashlytics.CrashesComponent;
import codegraft.inject.extension.okhttp3.OkhttpComponent;
import codegraft.inject.extension.okhttp3.OkhttpModule;
import codegraft.inject.extension.okhttp3.Okhttp_Module;
import codegraft.inject.extension.realm.RealmComponent;
import codegraft.inject.extension.realm.RealmModule;
import dagger.Component;
import dagger.android.AndroidInjector;
import evovetech.blog.medium.MediumActivity_Module;
import evovetech.blog.medium.MediumComponent;
import evovetech.blog.medium.MediumModule;
import evovetech.blog.medium.ui.medium.MediumFragment_Module;
import evovetech.blog.medium.ui.medium.MediumViewModel_Module;
import evovetech.finance.plaid.PlaidActivity_Module;
import evovetech.finance.plaid.PlaidComponent;
import evovetech.finance.plaid.PlaidModule;
import evovetech.finance.plaid.ui.plaid.PlaidFragment_Module;
import evovetech.finance.plaid.ui.plaid.PlaidViewModel_Module;
import evovetech.sample.instant.ui.main.MainFragment_Module;
import evovetech.sample.instant.ui.main.MainViewModel_Module;
import java.lang.Override;
import javax.inject.Singleton;

@Singleton
@Component(
    modules = {
        AppComponent_BootData.class,
        MainActivity_Module.class,
        evovetech.sample.github.MainActivity_Module.class,
        App_Module.class,
        PlaidFragment_Module.class,
        PlaidActivity_Module.class,
        MediumFragment_Module.class,
        MediumActivity_Module.class,
        MainFragment_Module.class,
        evovetech.sample.instant.MainActivity_Module.class,
        PlaidViewModel_Module.class,
        MediumViewModel_Module.class,
        MainViewModel_Module.class,
        Okhttp_Module.class
    }
)
public interface AppComponent extends ApplicationInjectorComponent, ActivityInjectorComponent, SupportFragmentInjectorComponent, BaseComponent {
  @Override
  AndroidInjector<Application> getApplicationInjector();

  @Override
  AndroidInjector<Activity> activityInjector();

  @Override
  AndroidInjector<Fragment> supportFragmentInjector();

  @Override
  Application getApplication();

  @Override
  Plugins getPlugins();

  RealmComponent getRealmComponent();

  OkhttpComponent getOkhttpComponent();

  PlaidComponent getPlaidComponent();

  MediumComponent getMediumComponent();

  CrashesComponent getCrashesComponent();

  ViewModelComponent getViewModelComponent();

  @Component.Builder
  interface Builder {
    Builder realmModule(RealmModule realmModule);

    Builder okhttpModule(OkhttpModule okhttpModule);

    Builder plaidModule(PlaidModule plaidModule);

    Builder mediumModule(MediumModule mediumModule);

    Builder crashes(Crashes crashes);

    Builder bootData(AppComponent_BootData bootData);

    AppComponent build();
  }
}
