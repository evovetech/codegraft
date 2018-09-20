package codegraft.inject.android;

import android.arch.lifecycle.ViewModel;
import codegraft.inject.ClassKeyProviderMap;
import java.lang.Class;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Provider;
import org.jetbrains.annotations.NotNull;

public final class ViewModels extends ClassKeyProviderMap<ViewModel> {
  @Inject
  ViewModels(@NotNull Map<Class<? extends ViewModel>, Provider<ViewModel>> providers) {
    super(providers);
  }
}
