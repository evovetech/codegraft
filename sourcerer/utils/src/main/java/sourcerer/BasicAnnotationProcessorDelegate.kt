/*
 * Copyright (C) 2018 evove.tech
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package sourcerer

import com.google.auto.common.BasicAnnotationProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion

open
class BasicAnnotationProcessorDelegate(
    private val steps: (ProcessingEnvironment) -> Iterable<ProcessingStep>,
    private val supportedOptions: (ProcessingEnvironment) -> Set<String> = { emptySet() },
    private val supportedSourceVersion: () -> SourceVersion = { SourceVersion.latestSupported() },
    private val post: (RoundEnvironment) -> Unit = {}
) : BasicAnnotationProcessor() {
    override
    fun initSteps(): Iterable<ProcessingStep> {
        return steps(processingEnv)
    }

    override
    fun getSupportedOptions(): Set<String> {
        return supportedOptions(processingEnv)
    }

    override
    fun getSupportedSourceVersion(): SourceVersion {
        return supportedSourceVersion()
    }

    override
    fun postRound(roundEnv: RoundEnvironment) {
        super.postRound(roundEnv)
        post(roundEnv)
    }
}
