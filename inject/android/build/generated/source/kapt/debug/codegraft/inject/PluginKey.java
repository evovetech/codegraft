package codegraft.inject;

import dagger.MapKey;
import java.lang.Class;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@MapKey
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface PluginKey {
  Class<? extends Plugin> value();
}
