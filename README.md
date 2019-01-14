Codegraft
=========

[ ![Download](https://api.bintray.com/packages/evove-tech/maven/codegraft/images/download.svg) ](https://bintray.com/evove-tech/maven/codegraft/_latestVersion)

## Android Components & View Models

Usually wiring up android components takes a lot of boilerplate. With Codegraft, you can skip the mundane and focus on the code that really matters.

```kotlin
// View Model
@BindViewModel
class MediumViewModel
@Inject constructor(
    val client: MediumClient
) : ViewModel() {
    // TODO: Implement the ViewModel
}

// Fragment with a view model
@AndroidInject
class MediumFragment : Fragment() {
    @Inject lateinit
    var viewModels: ViewModelInstanceProvider

    private
    val viewModel: MediumViewModel by ::viewModels.delegate()

    override
    fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.medium_fragment, container, false)
    }

    override
    fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.apply {
            Log.d("MediumFragment", "medium view model = $this, medium client = $client}")
        }
        // TODO: Use the ViewModel
    }

    companion object {
        fun newInstance() = MediumFragment()
    }
}
```

## Gradle Setup

example project structure:
```
project/
    app/
        src/
        build.gradle
    lib/
        src/
        build.gradle
    build.gradle
    settings.gradle
```

project/build.gradle
```gradle
buildscript {
    ext {
        codegraftVersion = '0.8.7'
    }
    dependencies {
        classpath "evovetech.codegraft:inject-plugin:${codegraftVersion}"
    }
}
```

app/build.gradle
```gradle
apply plugin: 'com.android.application'
// ...
apply plugin: 'codegraft.inject.android'

// optional
codegraft {
    // turn off incremental transform
    incremental = false
}
```

lib/build.gradle

```gradle
apply plugin: 'com.android.library'
// ...
apply plugin: 'codegraft.inject.android'

// optional extensions
dependencies {
    // Crashlytics extension
    api "evovetech.codegraft:inject-extension-crashlytics:${codegraftVersion}"
    
    // Okhttp extension
    api "evovetech.codegraft:inject-extension-okhttp3:${codegraftVersion}"
    
    // Retrofit extension
    api "evovetech.codegraft:inject-extension-retrofit2:${codegraftVersion}"
    
    // Realm extension
    api "evovetech.codegraft:inject-extension-realm:${codegraftVersion}"
}
```

## Basics

Codgraft uses dagger in a way that allows you to compose multiple kotlin modules together into a generated component for use in the application and also in tests. This was built specifically for android and its needs.

```kotlin
// App.kt
@AndroidInject
class App : Application(), BootApplication<AppComponent> {
    @Inject lateinit
    var fabric: Fabric

    override
    val bootstrap = bootstrap {
        fabricBuilderFunction1(Fabric.Builder::bootstrap)
        realmConfigurationBuilderFunction1(RealmConfiguration.Builder::bootstrap)
        okHttpClientApplicationBuilderFunction2(OkHttpClient.Builder::bootstrap)
        this@App
    }

    override
    fun onCreate() {
        super.onCreate()
        logStartup("onCreate")
    }

    fun logStartup(tag: String) {
        Log.d(tag, "startup")
        fabric.kits.forEach {
            Log.d(tag, "app -- fabric kit=$it")
        }
    }
}

fun Fabric.Builder.bootstrap(): Fabric {
    return kits(Crashlytics())
            .build()
}

fun RealmConfiguration.Builder.bootstrap(): RealmConfiguration {
    return name("app.realm")
            .schemaVersion(1)
            .build()
}

fun OkHttpClient.Builder.bootstrap(app: AndroidApplication): OkHttpClient {
    // TODO
    return build()
}
```

Behind the scenes, this code uses annotations to either generate source code or modify bytecode so that most of the boilerplate that is usually necessary is done for you.

In the case above, the boilerplate connections are added to the bytecode so that it just works.

```java
// App.class -- decompiled
@AndroidInject
public final class App extends Application implements BootApplication, HasApplicationInjector, HasActivityInjector, HasSupportFragmentInjector {
   @Inject
   @NotNull
   public Fabric fabric;
   @NotNull
   private final Bootstrap bootstrap = Bootstrap_GenKt.bootstrap((Function1)(new Function1() {
      // $FF: synthetic method
      // $FF: bridge method
      public Object invoke(Object var1) {
         return this.invoke((Builder)var1);
      }

      @NotNull
      public final App invoke(@NotNull Builder $receiver) {
         Intrinsics.checkParameterIsNotNull($receiver, "$receiver");
         $receiver.fabricBuilderFunction1((Function1)null.INSTANCE);
         $receiver.realmConfigurationBuilderFunction1((Function1)null.INSTANCE);
         $receiver.okHttpClientApplicationBuilderFunction2((Function2)null.INSTANCE);
         return App.this;
      }
   }));

   @NotNull
   public final Fabric getFabric() {
      Fabric var10000 = this.fabric;
      if (this.fabric == null) {
         Intrinsics.throwUninitializedPropertyAccessException("fabric");
      }

      return var10000;
   }

   public final void setFabric(@NotNull Fabric var1) {
      Intrinsics.checkParameterIsNotNull(var1, "<set-?>");
      this.fabric = var1;
   }

   @NotNull
   public Bootstrap getBootstrap() {
      return this.bootstrap;
   }

   public void onCreate() {
      super.onCreate();
      this.logStartup("onCreate");
   }

   public final void logStartup(@NotNull String tag) {
      Intrinsics.checkParameterIsNotNull(tag, "tag");
      Log.d(tag, "startup");
      Fabric var10000 = this.fabric;
      if (this.fabric == null) {
         Intrinsics.throwUninitializedPropertyAccessException("fabric");
      }

      Collection var8 = var10000.getKits();
      Intrinsics.checkExpressionValueIsNotNull(var8, "fabric.kits");
      Iterable $receiver$iv = (Iterable)var8;
      Iterator var3 = $receiver$iv.iterator();

      while(var3.hasNext()) {
         Object element$iv = var3.next();
         Kit it = (Kit)element$iv;
         Log.d(tag, "app -- fabric kit=" + it);
      }
   }

   public AndroidInjector getApplicationInjector() {
      return BootstrapMethods.getApplicationInjector(this);
   }

   public AndroidInjector activityInjector() {
      return BootstrapMethods.activityInjector(this);
   }

   public AndroidInjector supportFragmentInjector() {
      return BootstrapMethods.supportFragmentInjector(this);
   }

   public AppComponent getComponent() {
      return (AppComponent)this.bootstrap.getComponent();
   }
}

```

With the help of a content provider, we are able to enforce that the application is injected before onCreate() code is called.

```kotlin
// Content Provider
class BootstrapProvider : EmptyContentProvider() {
    override
    fun onCreate(): Boolean {
        val TAG = "BootstrapProvider"
        val app = context as Application
        when (app) {
            is BootApplication<*> -> {
                Log.d(TAG, "Bootstrapping!!")
                val component = app.bootstrap.component
                if (component is HasApplicationInjector) {
                    component.applicationInjector.inject(app)
                }
            }
            else -> {
                Log.d(TAG, "NO Bootstraps :(")
            }
        }
        return true
    }
}
```

## Usage

To enable Codegraft, we need a custom component `@BootstrapComponent` in order to allow the a single application component to be shared. Notice that we specify a dependency on `OkhttpComponent`. This actually lives in a separate library but we will still be able to incorporate it into our single application component without the normal hassle and with customization.

```kotlin
// MediumComponent.kt

@BootstrapComponent(
    bootstrapDependencies = [OkhttpComponent::class],
    applicationModules = [MediumModule::class]
)
interface MediumComponent {
    val client: MediumClient

    fun newUser(): MediumUserComponent.Builder
}
```

We can then use normal dagger to provide the functionality.

```kotlin
@Subcomponent(modules = [MediumUserModule::class])
interface MediumUserComponent {
    val user: MediumCall<User>

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun username(username: String): Builder

        fun build(): MediumUserComponent
    }
}

@Module
class MediumUserModule {
    @Provides
    fun provideUserCall(
        username: String,
        client: MediumClient
    ): MediumCall<User> {
        return client.user(username)
    }
}

// MediumModule.kt

private const
val AuthKey = "Bearer ${BuildConfig.API_KEY}"

@Module(subcomponents = [MediumUserComponent::class])
class MediumModule {
    @Provides
    @Singleton
    @Named("medium")
    fun provideOkhttp(
        app: AndroidApplication,
        okhttpBuilder: Builder
    ): OkHttpClient {
        return okhttpBuilder
                .addNetworkInterceptor { chain ->
                    val request = chain.request()
                            .newBuilder()
                            .header("Authorization", AuthKey)
                            .build()
                    chain.proceed(request)
                }
                .build()
    }

    @Provides
    @Named("medium")
    fun provideGson(): Gson {
        return GsonBuilder()
                // TODO:
                .create()
    }

    @Provides
    @Singleton
    @Named("medium")
    fun provideRetrofit(
        app: AndroidApplication,
        @Named("medium") client: OkHttpClient,
        @Named("medium") gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
                .client(client)
                .baseUrl("https://api.medium.com/v1/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
    }

    @Provides
    @Singleton
    fun provideMediumService(
        @Named("medium") retrofit: Retrofit
    ): MediumService {
        return retrofit.create(MediumService::class.java)
    }
}

```

Below is the okhttp library setup that enables the end application component to customize it before usage. We can then share the root okhttp configuration in an easier way than usual.

```kotlin
/// Okhttp extension library (example)

@BootstrapComponent(
    applicationModules = [OkhttpModule::class],
    bootstrapModules = [OkhttpBootstrapModule::class],
    autoInclude = false
)
interface OkhttpComponent {
    val okhttp: Okhttp
}

typealias OkHttpInit = OkHttpClient.Builder.(app: Application) -> OkHttpClient

@Module
class OkhttpBootstrapModule {
    @Provides
    @BootScope
    fun provideDefaultOkhttp(
        @BootScope app: Application,
        @Named("okhttp") init: OkHttpInit
    ): OkHttpClient {
        val builder = Builder()
        builder.init(app)
        return builder.build()
    }
}

@Module
class OkhttpModule {
    @Provides
    fun provideDefaultOkhttpBuilder(
        okhttp: OkHttpClient
    ): Builder {
        return okhttp.newBuilder()
    }
}

@Singleton
@BindPlugin
class Okhttp
@Inject constructor(
    private val okhttpProvider: Provider<OkHttpClient>,
    private val okhttpBuilderProvider: Provider<OkHttpClient.Builder>
) : Plugin {
    val client: OkHttpClient
        get() = okhttpProvider.get()

    val builder: OkHttpClient.Builder
        get() = okhttpBuilderProvider.get()
}
```

## Putting it together (codegen)

The ultimate outcome is a generated bootstrap component & builder that is intended to allow configuration (and sometime eager initialization in the case of crash framework, etc.) of the application component which will be a singleton.

#### (User bootstrap)
```kotlin
/// App.kt

@AndroidInject
class App : Application(), BootApplication<AppComponent> {
   // Run before onCreate()
    override
    val bootstrap = bootstrap {
        okHttpClientApplicationBuilderFunction2(OkHttpClient.Builder::bootstrap)
        this@App
    }
}

fun OkHttpClient.Builder.bootstrap(app: AndroidApplication): OkHttpClient {
    // TODO
    return build()
}
```

#### (Generated)
```kotlin
/// Generated
typealias BootstrapInit = BootComponent.Builder.() -> AndroidApplication

fun bootstrap(
    init: BootstrapInit
): Bootstrap<AppComponent> = Bootstrap {
    DaggerBootComponent.builder()
            .build(init)
}

private
fun BootComponent.Builder.build(
    init: BootstrapInit
): AppComponent = application(init())
        .build()
        .appComponent
```

```java
/// Generated
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
    Builder okHttpClientApplicationBuilderFunction2(
        @Named("okhttp") Function2<? super OkHttpClient.Builder, ? super Application, ? extends OkHttpClient> okHttpClientApplicationBuilderFunction2);

    @BindsInstance
    Builder okhttpModule(@Nullable OkhttpModule okhttpModule);

    @BindsInstance
    Builder mediumModule(@Nullable MediumModule mediumModule);

    Builder okhttpBootstrapModule(OkhttpBootstrapModule okhttpBootstrapModule);

    BootComponent build();
  }
}

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
  AppComponent provideComponent(@Nullable OkhttpModule okhttpModule,
      @Nullable MediumModule mediumModule,
      AppComponent_BootData bootData) {
    AppComponent.Builder builder = DaggerAppComponent.builder();
    if (okhttpModule != null) {
      builder.okhttpModule(okhttpModule);
    }
    if (mediumModule != null) {
      builder.mediumModule(mediumModule);
    }
    builder.bootData(bootData);
    return builder.build();
  }
}

@Singleton
@Component(
    modules = {
        AppComponent_BootData.class,
        MainActivity_Module.class,
        App_Module.class,
        MediumFragment_Module.class,
        MediumActivity_Module.class,
        MainFragment_Module.class,
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

  OkhttpComponent getOkhttpComponent();

  MediumComponent getMediumComponent();

  ViewModelComponent getViewModelComponent();

  @Component.Builder
  interface Builder {
    Builder okhttpModule(OkhttpModule okhttpModule);

    Builder mediumModule(MediumModule mediumModule);

    Builder bootData(AppComponent_BootData bootData);

    AppComponent build();
  }
}

@Module(
    includes = {
        OkhttpComponent_Module.class,
        MediumComponent_Module.class,
        AndroidInjectApplicationModule.class,
        ViewModelComponent_Module.class,
        AndroidInjectActivityModule.class,
        AndroidInjectSupportFragmentModule.class,
        PluginModule.class
    }
)
class AppComponent_BootData {
  private final OkhttpComponent_BootData okhttpComponent_BootData;

  private final BaseComponent_BootData baseComponent_BootData;

  @Inject
  AppComponent_BootData(OkhttpComponent_BootData okhttpComponent_BootData,
      BaseComponent_BootData baseComponent_BootData) {
    this.okhttpComponent_BootData = okhttpComponent_BootData;
    this.baseComponent_BootData = baseComponent_BootData;
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
}

```
