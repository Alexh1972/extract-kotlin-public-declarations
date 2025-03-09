package org.declarations

import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.com.intellij.psi.PsiManager
import org.jetbrains.kotlin.com.intellij.testFramework.LightVirtualFile
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty
import java.io.File

/**
 * Utility class for processing Kotlin files and extracting public declarations.
 */
class KtUtils {
    companion object {
        private const val KOTLIN_FILE_EXTENSION = "kt"
        private const val TEMP_DIR_RELATIVE_PATH = "tmp/"

        /**
         * Generates an indentation string with the specified number of tabs.
         *
         * @param ident Number of tabs.
         * @return A string consisting of `ident` tab characters.
         */
        fun getIdent(ident: Int): String {
            return StringBuilder().repeat("\t", ident).toString()
        }

        /**
         * Processes a given Kotlin file to extract and print its public declarations.
         *
         * @param file The Kotlin file to process.
         * @param baseName The base directory for resolving relative paths.
         */
        fun processKotlinFile(file: File, baseName: File) {
            if (!file.isFile)
                return

            if (file.extension != KOTLIN_FILE_EXTENSION)
                return

            val ktFile = createKtFile(file.readText(), TEMP_DIR_RELATIVE_PATH + file.relativeTo(baseName))
            printPublicDeclarations(ktFile, file)
        }

        /**
         * Creates a Kotlin PSI file from the provided code string.
         *
         * @param codeString The source code of the Kotlin file.
         * @param fileName The name of the virtual file.
         * @return A parsed [KtFile] representation of the code.
         */
        fun createKtFile(codeString: String, fileName: String): KtFile {
            val environment = KotlinCoreEnvironment.createForProduction(
                Disposer.newDisposable(),
                CompilerConfiguration(),
                EnvironmentConfigFiles.JVM_CONFIG_FILES
            ).project

            return PsiManager.getInstance(environment)
                .findFile(
                    LightVirtualFile(fileName, KotlinFileType.INSTANCE, codeString)
                ) as KtFile
        }

        /**
         * Prints public declarations (classes, functions, properties) found in the given Kotlin file.
         *
         * @param ktFile The Kotlin PSI file to analyze.
         * @param originalFile The original file from which the PSI representation was created.
         */
        fun printPublicDeclarations(ktFile: KtFile, originalFile: File) {
            println(StringBuilder().repeat("=", 100))
            println("Declarations for " + originalFile.absolutePath)
            println()

            var counter = 0
            ktFile.children.forEach { decl ->
                when (decl) {
                    is KtClass -> {
                        println(KtElementAnalyser.process(decl, "\n"))
                        counter++
                    }

                    is KtNamedFunction -> {
                        println(KtElementAnalyser.process(decl, "\n"))
                        counter++
                    }

                    is KtProperty -> {
                        println(KtElementAnalyser.process(decl, "\n"))
                        counter++
                    }
                }
            }

            if (counter == 0) {
                println("No declaration found in " + originalFile.absolutePath)
            }
        }
    }
}