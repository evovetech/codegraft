package evovetech.gradle.transform

import evovetech.codegen.BootstrapMethods
import evovetech.gradle.transform.content.Output
import net.bytebuddy.ByteBuddy
import net.bytebuddy.ClassFileVersion
import net.bytebuddy.build.EntryPoint
import net.bytebuddy.description.type.TypeDescription
import net.bytebuddy.dynamic.ClassFileLocator
import net.bytebuddy.dynamic.DynamicType
import net.bytebuddy.dynamic.DynamicType.Builder
import net.bytebuddy.dynamic.DynamicType.Unloaded
import net.bytebuddy.dynamic.scaffold.inline.MethodNameTransformer.Suffixing
import net.bytebuddy.pool.TypePool
import net.bytebuddy.pool.TypePool.CacheProvider.Simple
import net.bytebuddy.pool.TypePool.ClassLoading
import net.bytebuddy.pool.TypePool.Default.ReaderMode.FAST
import net.bytebuddy.pool.TypePool.Default.WithLazyResolution

class TransformData(
    private val classFileLocator: ClassFileLocator,
    private val classFileVersion: ClassFileVersion = ClassFileVersion.JAVA_V7
) {
    val typePool: TypePool by lazy {
        WithLazyResolution(
            Simple(),
            classFileLocator,
            FAST,
            ClassLoading.ofBootPath()
        )
    }
    val methodTransformer = Suffixing("original")

    val Output.src: String
        get() = input.path
    val Output.typeName: String?
        get() = if (src.endsWith(CLASS_FILE_EXTENSION)) {
            src.replace('/', '.')
                    .substring(0, src.length - CLASS_FILE_EXTENSION.length)
        } else {
            null
        }
    val Output.typeDescription: TypeDescription?
        get() = typeName?.let { type -> typePool.describe(type).resolve() }

    fun EntryPoint.newByteBuddy(): ByteBuddy =
        byteBuddy(classFileVersion)

    fun EntryPoint.transform(
        typeDescription: TypeDescription
    ): Builder<*> = transform(
        typeDescription,
        newByteBuddy(),
        classFileLocator,
        methodTransformer
    )

    fun OutputWriter.canTransform(
        typeDescription: TypeDescription
    ): Boolean = try {
        this@TransformData.canTransform(typeDescription)
    } catch (_: Throwable) {
        false
    }

    fun OutputWriter.transform(
        typeDescription: TypeDescription
    ): Unloaded<*> {
        return this@TransformData.transform(typeDescription)
    }
}

inline
fun <reified T> TransformData.resolve(): TypeDescription =
    typePool.resolve<T>()

inline
fun <reified T> DynamicType.Builder<*>.addInjector(
    transformData: TransformData,
    componentType: TypeDescription.Generic
): DynamicType.Builder<*> {
    val hasInjectorType = transformData.resolve<T>()
    if (componentType.asErasure().isAssignableTo(hasInjectorType)) {
        println("$componentType is assignable to $hasInjectorType")
        return implement(hasInjectorType)
                .intercept(methodDelegation<BootstrapMethods>())
    }
    return this
}
