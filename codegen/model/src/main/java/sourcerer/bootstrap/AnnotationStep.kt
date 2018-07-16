package sourcerer.bootstrap

import sourcerer.AnnotationElements
import sourcerer.AnnotationType
import sourcerer.processor.ProcessingEnv
import sourcerer.processor.ProcessingEnv.Option
import javax.annotation.processing.RoundEnvironment
import javax.inject.Inject

abstract
class AnnotationStep {
    private lateinit
    var env: ProcessingEnv

    @Inject
    fun setEnv(env: ProcessingEnv) {
        this.env = env
    }

    protected abstract
    fun ProcessingEnv.annotations(): Set<AnnotationType>

    protected abstract
    fun ProcessingEnv.process(
        annotationElements: AnnotationElements
    ): Outputs

    open
    fun supportedOptions(): Iterable<Option> {
        // subclass override
        return emptyList()
    }

    open
    fun postRound(roundEnv: RoundEnvironment): Unit {
        // subclass override
    }

    fun annotations(): Set<AnnotationType> = env.annotations()

    fun process(annotationElements: AnnotationElements): Outputs =
        env.process(annotationElements)
}
